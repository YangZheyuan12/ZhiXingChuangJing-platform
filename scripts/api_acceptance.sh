#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080/api/v1}"

LAST_BODY=""

log_step() {
  printf '\n[%s] %s\n' "$(date '+%H:%M:%S')" "$1"
}

json_pick() {
  local json="$1"
  local path="$2"

  printf '%s' "$json" | node -e '
    const fs = require("fs");
    const path = process.argv[1];
    const raw = fs.readFileSync(0, "utf8");
    const valueAt = (root, expr) => {
      let current = root;
      for (const segment of expr.split(".")) {
        if (!segment) {
          continue;
        }
        const match = segment.match(/^([^\[]+)(?:\[(\d+)\])?$/);
        if (!match) {
          throw new Error(`Unsupported path segment: ${segment}`);
        }
        const key = match[1];
        current = current == null ? undefined : current[key];
        if (match[2] !== undefined) {
          const index = Number(match[2]);
          current = Array.isArray(current) ? current[index] : undefined;
        }
      }
      return current;
    };

    const data = JSON.parse(raw);
    const result = valueAt(data, path);
    if (result === undefined) {
      process.exit(2);
    }
    if (result === null) {
      console.log("null");
    } else if (typeof result === "object") {
      console.log(JSON.stringify(result));
    } else {
      console.log(String(result));
    }
  ' "$path"
}

assert_api_success() {
  local response="$1"
  local api_code
  local message

  api_code="$(json_pick "$response" "code")"
  message="$(json_pick "$response" "message")"
  if [[ "$api_code" != "0" ]]; then
    printf 'API business error: code=%s message=%s\n%s\n' "$api_code" "$message" "$response" >&2
    exit 1
  fi
}

request_json() {
  local method="$1"
  local url="$2"
  local token="${3:-}"
  local body="${4:-}"
  local tmp status
  local -a cmd

  tmp="$(mktemp)"
  cmd=(curl -sS -o "$tmp" -w "%{http_code}" -X "$method" "$url")
  if [[ -n "$token" ]]; then
    cmd+=(-H "Authorization: Bearer $token")
  fi
  if [[ -n "$body" ]]; then
    cmd+=(-H "Content-Type: application/json" --data "$body")
  fi

  status="$("${cmd[@]}")"
  LAST_BODY="$(cat "$tmp")"
  rm -f "$tmp"

  if [[ "$status" -lt 200 || "$status" -ge 300 ]]; then
    printf 'HTTP error: %s %s -> %s\n%s\n' "$method" "$url" "$status" "$LAST_BODY" >&2
    exit 1
  fi

  assert_api_success "$LAST_BODY"
  printf '%s' "$LAST_BODY"
}

request_upload() {
  local url="$1"
  local token="$2"
  local file_path="$3"
  local tmp status

  tmp="$(mktemp)"
  status="$(curl -sS -o "$tmp" -w "%{http_code}" \
    -X POST "$url" \
    -H "Authorization: Bearer $token" \
    -F "file=@${file_path}" \
    -F "folder=acceptance" \
    -F "bizType=task_material")"
  LAST_BODY="$(cat "$tmp")"
  rm -f "$tmp"

  if [[ "$status" -lt 200 || "$status" -ge 300 ]]; then
    printf 'HTTP error: POST %s -> %s\n%s\n' "$url" "$status" "$LAST_BODY" >&2
    exit 1
  fi

  assert_api_success "$LAST_BODY"
  printf '%s' "$LAST_BODY"
}

login_token() {
  local account="$1"
  local password="$2"
  local response

  response="$(request_json POST "$BASE_URL/auth/login" "" "{\"account\":\"${account}\",\"password\":\"${password}\"}")"
  json_pick "$response" "data.token"
}

log_step "登录并获取三类角色 token"
teacher_token="$(login_token "teacher001" "123456")"
student_token="$(login_token "student001" "123456")"
admin_token="$(login_token "admin" "123456")"

log_step "认证与用户资料接口"
request_json GET "$BASE_URL/auth/me" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/users/me" "$teacher_token" >/dev/null
request_json PUT "$BASE_URL/users/me/profile" "$teacher_token" '{
  "nickname":"李老师-验收",
  "avatarUrl":"https://example.com/avatar/teacher001.png",
  "bio":"接口验收资料更新"
}' >/dev/null

log_step "教师端概览与班级接口"
request_json GET "$BASE_URL/dashboard/overview?role=teacher" "$teacher_token" >/dev/null
teacher_classes="$(request_json GET "$BASE_URL/classes" "$teacher_token")"
class_id="$(json_pick "$teacher_classes" "data[0].id")"
request_json GET "$BASE_URL/classes/${class_id}" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/classes/${class_id}/members" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/classes/${class_id}/announcements" "$teacher_token" >/dev/null
request_json POST "$BASE_URL/classes/${class_id}/announcements" "$teacher_token" '{
  "title":"接口验收公告",
  "content":"用于验证班级公告发布接口。",
  "pinned":true
}' >/dev/null

log_step "教师创建并更新任务"
teacher_tasks_before="$(request_json GET "$BASE_URL/tasks" "$teacher_token")"
task_create_response="$(request_json POST "$BASE_URL/tasks" "$teacher_token" "{
  \"title\":\"长征精神数字展陈任务\",
  \"coverUrl\":\"https://example.com/task/cover.png\",
  \"description\":\"围绕长征精神完成教育创作展厅。\",
  \"backgroundMaterials\":[
    {
      \"title\":\"长征图像资料\",
      \"materialType\":\"image\",
      \"url\":\"https://example.com/materials/long-march.png\",
      \"description\":\"用于验收的背景资料\"
    }
  ],
  \"evaluationCriteria\":\"主题表达、叙事结构与互动设计\",
  \"startTime\":\"2026-04-19T09:00:00\",
  \"dueTime\":\"2026-05-20T18:00:00\",
  \"targetClassIds\":[${class_id}]
}")"
task_id="$(json_pick "$task_create_response" "data.id")"
request_json PUT "$BASE_URL/tasks/${task_id}" "$teacher_token" "{
  \"title\":\"长征精神数字展陈任务-更新\",
  \"coverUrl\":\"https://example.com/task/cover-updated.png\",
  \"description\":\"围绕长征精神完成教育创作展厅，并补充互动环节。\",
  \"backgroundMaterials\":[
    {
      \"title\":\"长征图像资料\",
      \"materialType\":\"image\",
      \"url\":\"https://example.com/materials/long-march.png\",
      \"description\":\"用于验收的背景资料\"
    },
    {
      \"title\":\"口述史音频\",
      \"materialType\":\"audio\",
      \"url\":\"https://example.com/materials/history.mp3\",
      \"description\":\"补充音频素材\"
    }
  ],
  \"evaluationCriteria\":\"主题表达、叙事结构、互动设计与史料引用\",
  \"startTime\":\"2026-04-19T09:00:00\",
  \"dueTime\":\"2026-05-21T18:00:00\",
  \"targetClassIds\":[${class_id}]
}" >/dev/null
request_json GET "$BASE_URL/tasks" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/tasks/${task_id}" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/tasks/${task_id}/excellent-exhibitions" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/tasks/${task_id}/progress" "$teacher_token" >/dev/null

log_step "学生创建展厅并完成版本、发布、数字人链路"
student_tasks="$(request_json GET "$BASE_URL/tasks" "$student_token")"
request_json GET "$BASE_URL/dashboard/overview?role=student" "$student_token" >/dev/null
exhibition_create_response="$(request_json POST "$BASE_URL/exhibitions" "$student_token" "{
  \"taskId\":${task_id},
  \"title\":\"长征路线数字展厅\",
  \"summary\":\"通过数字展陈讲述长征路线与精神。\",
  \"coverUrl\":\"https://example.com/exhibition/cover.png\",
  \"visibility\":\"public\",
  \"groupName\":\"红星小组\"
}")"
exhibition_id="$(json_pick "$exhibition_create_response" "data.id")"
request_json PUT "$BASE_URL/exhibitions/${exhibition_id}" "$student_token" '{
  "title":"长征路线数字展厅-更新",
  "summary":"通过数字展陈讲述长征路线、关键人物与精神传承。",
  "coverUrl":"https://example.com/exhibition/cover-updated.png",
  "visibility":"public"
}' >/dev/null
request_json GET "$BASE_URL/exhibitions/my" "$student_token" >/dev/null
request_json GET "$BASE_URL/exhibitions/${exhibition_id}" "$student_token" >/dev/null
request_json GET "$BASE_URL/exhibitions/${exhibition_id}/members" "$student_token" >/dev/null
request_json POST "$BASE_URL/exhibitions/${exhibition_id}/members" "$student_token" '{
  "memberUserIds":[2,4],
  "role":"editor"
}' >/dev/null
request_json GET "$BASE_URL/exhibitions/${exhibition_id}/versions" "$student_token" >/dev/null
version_response="$(request_json POST "$BASE_URL/exhibitions/${exhibition_id}/versions" "$student_token" '{
  "saveType":"manual",
  "versionNote":"验收初版",
  "canvasConfig":{
    "width":1920,
    "height":1080,
    "background":"#f8f1eb",
    "zoom":1.0
  },
  "versionData":{
    "canvasConfig":{
      "width":1920,
      "height":1080,
      "background":"#f8f1eb",
      "zoom":1.0
    },
    "elements":[
      {
        "componentType":"image",
        "x":120,
        "y":80,
        "width":560,
        "height":320,
        "props":{
          "src":"https://example.com/materials/long-march.png",
          "title":"长征路线图"
        }
      },
      {
        "componentType":"text",
        "x":760,
        "y":120,
        "width":640,
        "height":220,
        "props":{
          "content":"长征不仅是军事壮举，也是理想信念的教育资源。"
        }
      }
    ]
  }
}')"
version_no="$(json_pick "$version_response" "data.versionNo")"
request_json POST "$BASE_URL/exhibitions/${exhibition_id}/publish" "$student_token" "{
  \"versionNo\":${version_no},
  \"visibility\":\"public\"
}" >/dev/null
request_json GET "$BASE_URL/exhibitions/${exhibition_id}/viewer" "$student_token" >/dev/null
request_json GET "$BASE_URL/exhibitions/${exhibition_id}/digital-human" "$student_token" >/dev/null
digital_human_response="$(request_json PUT "$BASE_URL/exhibitions/${exhibition_id}/digital-human" "$student_token" '{
  "name":"小知",
  "avatar2dUrl":"https://example.com/dh/avatar2d.png",
  "model3dUrl":"https://example.com/dh/model.glb",
  "persona":"红色文化讲解员",
  "voiceType":"warm_female",
  "storyScript":"欢迎来到长征路线数字展厅。",
  "storyTimeline":[
    {
      "anchorCode":"start",
      "startSecond":0,
      "endSecond":30,
      "content":"欢迎介绍"
    }
  ]
}')"
digital_human_id="$(json_pick "$digital_human_response" "data.id")"

log_step "文博资源与数字人装备接口"
request_json GET "$BASE_URL/museum/resources" "$teacher_token" >/dev/null
request_json POST "$BASE_URL/museum/resources" "$teacher_token" '{
  "providerCode":"yanan_memorial",
  "category":"红色文化",
  "keyword":"长征"
}' >/dev/null
museum_list_response="$(request_json GET "$BASE_URL/museum/resources?providerCode=yanan_memorial&page=1&pageSize=10" "$teacher_token")"
museum_resource_id="$(json_pick "$museum_list_response" "data.list[0].id")"
request_json GET "$BASE_URL/museum/resources/${museum_resource_id}" "$teacher_token" >/dev/null
equipment_response="$(request_json POST "$BASE_URL/digital-humans/${digital_human_id}/equipments" "$student_token" "{
  \"slotCode\":\"hand-held\",
  \"museumResourceId\":${museum_resource_id},
  \"displayOrder\":1,
  \"anchorCode\":\"start\"
}")"
equipment_id="$(json_pick "$equipment_response" "data.id")"
request_json DELETE "$BASE_URL/digital-humans/${digital_human_id}/equipments/${equipment_id}" "$student_token" >/dev/null

log_step "素材上传与素材库接口"
temp_file="$(mktemp)"
printf 'acceptance asset file\n' > "$temp_file"
upload_response="$(request_upload "$BASE_URL/assets/upload" "$teacher_token" "$temp_file")"
rm -f "$temp_file"
asset_id="$(json_pick "$upload_response" "data.assetId")"
request_json GET "$BASE_URL/assets?page=1&pageSize=10" "$teacher_token" >/dev/null

log_step "任务提交与点评链路"
submission_response="$(request_json POST "$BASE_URL/tasks/${task_id}/submit" "$student_token" "{
  \"exhibitionId\":${exhibition_id},
  \"submitRemark\":\"接口验收提交\"
}")"
submission_id="$(json_pick "$submission_response" "data.id")"
request_json GET "$BASE_URL/tasks/${task_id}/submissions" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/submissions/${submission_id}" "$teacher_token" >/dev/null
request_json POST "$BASE_URL/submissions/${submission_id}/reviews" "$teacher_token" '{
  "score":95,
  "commentText":"结构清晰，史料组织较完整。",
  "commentAudioUrl":"https://example.com/reviews/review.mp3",
  "isPublic":true
}' >/dev/null
request_json GET "$BASE_URL/tasks/${task_id}/progress" "$teacher_token" >/dev/null

log_step "社区公开接口与互动链路"
request_json GET "$BASE_URL/community/exhibitions?page=1&pageSize=10" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/community/exhibitions/${exhibition_id}" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/community/exhibitions/${exhibition_id}/comments" "$teacher_token" >/dev/null
request_json POST "$BASE_URL/community/exhibitions/${exhibition_id}/comments" "$teacher_token" '{
  "content":"这个展厅的叙事节奏很好，适合课堂展示。"
}' >/dev/null
request_json POST "$BASE_URL/community/exhibitions/${exhibition_id}/like" "$student_token" >/dev/null
request_json DELETE "$BASE_URL/community/exhibitions/${exhibition_id}/like" "$student_token" >/dev/null
request_json POST "$BASE_URL/community/exhibitions/${exhibition_id}/favorite" "$student_token" >/dev/null
request_json DELETE "$BASE_URL/community/exhibitions/${exhibition_id}/favorite" "$student_token" >/dev/null
request_json POST "$BASE_URL/community/exhibitions/${exhibition_id}/share" "$student_token" '{
  "channel":"wechat"
}' >/dev/null
request_json POST "$BASE_URL/community/exhibitions/${exhibition_id}/feature" "$admin_token" '{
  "featured":true,
  "featuredReason":"适合作为教育创作平台公开示例。"
}' >/dev/null

log_step "个人主页、作品集与通知接口"
request_json GET "$BASE_URL/users/3/homepage" "$teacher_token" >/dev/null
request_json GET "$BASE_URL/users/me/portfolio" "$student_token" >/dev/null
notifications_response="$(request_json GET "$BASE_URL/notifications?page=1&pageSize=20" "$student_token")"
notification_id="$(json_pick "$notifications_response" "data.list[0].id")"
request_json GET "$BASE_URL/notifications?readStatus=unread&page=1&pageSize=20" "$student_token" >/dev/null
request_json POST "$BASE_URL/notifications/read" "$student_token" "{
  \"notificationIds\":[${notification_id}]
}" >/dev/null

log_step "密码修改接口验收并恢复原密码"
request_json PUT "$BASE_URL/users/me/password" "$student_token" '{
  "oldPassword":"123456",
  "newPassword":"1234567"
}' >/dev/null
student_token_changed="$(login_token "student001" "1234567")"
request_json PUT "$BASE_URL/users/me/password" "$student_token_changed" '{
  "oldPassword":"1234567",
  "newPassword":"123456"
}' >/dev/null
login_token "student001" "123456" >/dev/null

printf '\n验收完成。\n'
printf 'task_id=%s\n' "$task_id"
printf 'exhibition_id=%s\n' "$exhibition_id"
printf 'version_no=%s\n' "$version_no"
printf 'submission_id=%s\n' "$submission_id"
printf 'digital_human_id=%s\n' "$digital_human_id"
printf 'museum_resource_id=%s\n' "$museum_resource_id"
printf 'asset_id=%s\n' "$asset_id"
