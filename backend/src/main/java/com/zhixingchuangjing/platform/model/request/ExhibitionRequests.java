package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class ExhibitionRequests {

    private ExhibitionRequests() {
    }

    public record CreateExhibitionRequest(
            Long taskId,
            @NotBlank(message = "展厅标题不能为空")
            @Size(max = 128, message = "展厅标题长度不能超过128个字符")
            String title,
            @Size(max = 500, message = "摘要长度不能超过500个字符")
            String summary,
            @Size(max = 255, message = "封面地址长度不能超过255个字符")
            String coverUrl,
            String visibility,
            @Size(max = 128, message = "小组名称长度不能超过128个字符")
            String groupName,
            String templateCode
    ) {
    }

    public record UpdateExhibitionRequest(
            @NotBlank(message = "展厅标题不能为空")
            @Size(max = 128, message = "展厅标题长度不能超过128个字符")
            String title,
            @Size(max = 500, message = "摘要长度不能超过500个字符")
            String summary,
            @Size(max = 255, message = "封面地址长度不能超过255个字符")
            String coverUrl,
            String visibility
    ) {
    }

    public record AddExhibitionMembersRequest(
            @NotNull(message = "成员列表不能为空")
            @Size(min = 1, message = "至少选择一个成员")
            List<Long> memberUserIds,
            @NotBlank(message = "成员角色不能为空")
            String role
    ) {
    }

    public record CanvasConfigRequest(
            @NotNull(message = "画布宽度不能为空")
            Integer width,
            @NotNull(message = "画布高度不能为空")
            Integer height,
            String background,
            @NotNull(message = "缩放比例不能为空")
            Double zoom
    ) {
    }

    public record SaveExhibitionVersionRequest(
            @NotBlank(message = "保存类型不能为空")
            String saveType,
            @Size(max = 255, message = "版本说明长度不能超过255个字符")
            String versionNote,
            @NotNull(message = "画布配置不能为空")
            @Valid
            CanvasConfigRequest canvasConfig,
            @NotNull(message = "版本数据不能为空")
            Object versionData
    ) {
    }

    public record PublishExhibitionRequest(
            @NotNull(message = "版本号不能为空")
            Integer versionNo,
            @NotBlank(message = "可见性不能为空")
            String visibility
    ) {
    }

    public record StoryTimelineItemRequest(
            String anchorCode,
            Integer startSecond,
            Integer endSecond,
            String content
    ) {
    }

    public record UpsertDigitalHumanRequest(
            @NotBlank(message = "数字人名称不能为空")
            @Size(max = 64, message = "数字人名称长度不能超过64个字符")
            String name,
            @Size(max = 255, message = "2D形象地址长度不能超过255个字符")
            String avatar2dUrl,
            @Size(max = 255, message = "3D模型地址长度不能超过255个字符")
            String model3dUrl,
            @Size(max = 255, message = "角色设定长度不能超过255个字符")
            String persona,
            @Size(max = 64, message = "音色类型长度不能超过64个字符")
            String voiceType,
            String storyScript,
            List<StoryTimelineItemRequest> storyTimeline
    ) {
    }

    public record BindEquipmentRequest(
            @NotBlank(message = "槽位编码不能为空")
            @Size(max = 64, message = "槽位编码长度不能超过64个字符")
            String slotCode,
            @NotNull(message = "文博资源ID不能为空")
            Long museumResourceId,
            Integer displayOrder,
            @Size(max = 64, message = "故事锚点编码长度不能超过64个字符")
            String anchorCode
    ) {
    }
}
