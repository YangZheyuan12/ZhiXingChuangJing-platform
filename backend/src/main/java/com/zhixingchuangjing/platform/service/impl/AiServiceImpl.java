package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.model.request.AiRequests;
import com.zhixingchuangjing.platform.model.response.AiResponses;
import com.zhixingchuangjing.platform.service.AiService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 讲解词生成模拟实现：使用模板拼装生成带语言风格的讲解词。
 * <p>
 * 当前版本无外部 LLM 调用，确保离线可用；后续可通过策略模式接入真实 LLM。
 */
@Service
public class AiServiceImpl implements AiService {

    private static final int DEFAULT_MAX_LENGTH = 600;
    private static final int MIN_LENGTH_SAFEGUARD = 120;

    @Override
    public AiResponses.GenerateNarrationResponse generateNarration(AiRequests.GenerateNarrationRequest request) {
        String title = safeTrim(request.exhibitTitle());
        String description = safeTrim(request.exhibitDescription());
        List<String> knowledgePoints = sanitizeKnowledgePoints(request.knowledgePoints());
        String style = normalizeStyle(request.style());
        String grade = safeTrim(request.targetGrade());
        int maxLength = resolveMaxLength(request.maxLength());

        String narration = composeNarration(title, description, knowledgePoints, style, grade, maxLength);
        List<String> suggestions = composeSuggestions(title, knowledgePoints);

        return new AiResponses.GenerateNarrationResponse(narration, suggestions);
    }

    // ────────────────────────────────────────────────────────────────
    //  主体文案生成
    // ────────────────────────────────────────────────────────────────

    private String composeNarration(String title,
                                    String description,
                                    List<String> knowledgePoints,
                                    String style,
                                    String grade,
                                    int maxLength) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildOpening(title, grade, style));
        sb.append("\n\n");

        if (!description.isEmpty()) {
            sb.append(buildDescriptionParagraph(description, style));
            sb.append("\n\n");
        }

        if (!knowledgePoints.isEmpty()) {
            sb.append(buildKnowledgeSection(knowledgePoints, style));
            sb.append("\n\n");
        }

        sb.append(buildClosing(title, style));

        return truncate(sb.toString().trim(), maxLength);
    }

    private String buildOpening(String title, String grade, String style) {
        String audience = grade.isEmpty() ? "同学们" : grade + "的同学们";
        return switch (style) {
            case "academic" ->
                    "本展品《" + title + "》是本次展览的重要组成部分。下面将从历史背景、结构特征与文化价值三个维度，向" + audience + "进行系统讲解。";
            case "storytelling" ->
                    audience + "，请跟着我的脚步，走进《" + title + "》的故事——它静静伫立在这里，等待被你发现。";
            case "conversational" ->
                    "嘿，" + audience + "，来看看这个《" + title + "》吧！它看起来普通，但每一处细节都藏着秘密。";
            default ->
                    audience + "好，欢迎来到《" + title + "》展位前。接下来，让我为你娓娓道来这件展品背后的故事。";
        };
    }

    private String buildDescriptionParagraph(String description, String style) {
        return switch (style) {
            case "academic" -> "据资料记载：" + description;
            case "storytelling" -> "它是这样的——" + description;
            case "conversational" -> "先说说它的样子：" + description;
            default -> description;
        };
    }

    private String buildKnowledgeSection(List<String> knowledgePoints, String style) {
        StringBuilder sb = new StringBuilder();
        sb.append(switch (style) {
            case "academic" -> "【核心知识点】";
            case "storytelling" -> "在这件展品身上，我们能读到这些细节：";
            case "conversational" -> "你需要记住三件事：";
            default -> "请留意以下几点：";
        });
        sb.append("\n");
        int idx = 1;
        for (String point : knowledgePoints) {
            sb.append(idx++).append(". ").append(point).append("\n");
        }
        return sb.toString().trim();
    }

    private String buildClosing(String title, String style) {
        return switch (style) {
            case "academic" -> "综上所述，《" + title + "》承载着独特的文化语义，值得我们反复品读与探究。";
            case "storytelling" -> "就是这样一件《" + title + "》，它的故事还有很多，等待你亲自去发现。";
            case "conversational" -> "怎么样？是不是发现《" + title + "》其实一点都不简单？下次带上好朋友一起来看吧！";
            default -> "这就是《" + title + "》带给我们的启示，愿你在参观中收获思考与感动。";
        };
    }

    // ────────────────────────────────────────────────────────────────
    //  追问提示生成
    // ────────────────────────────────────────────────────────────────

    private List<String> composeSuggestions(String title, List<String> knowledgePoints) {
        List<String> suggestions = new ArrayList<>(4);
        suggestions.add("《" + title + "》最让你印象深刻的细节是什么？");
        if (!knowledgePoints.isEmpty()) {
            suggestions.add("如果要向同学介绍「" + knowledgePoints.get(0) + "」，你会怎么说？");
        } else {
            suggestions.add("它和你以往学过的内容有哪些共同点？");
        }
        suggestions.add("你觉得它在今天还有什么现实意义？");
        suggestions.add("把今天的观察记录下来，你能写成一篇 300 字的微型导览词吗？");
        return suggestions;
    }

    // ────────────────────────────────────────────────────────────────
    //  工具方法
    // ────────────────────────────────────────────────────────────────

    private String normalizeStyle(String raw) {
        if (raw == null || raw.isBlank()) {
            return "narrative";
        }
        String trimmed = raw.trim();
        return switch (trimmed) {
            case "narrative", "academic", "storytelling", "conversational" -> trimmed;
            default -> "narrative";
        };
    }

    private String safeTrim(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private List<String> sanitizeKnowledgePoints(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private int resolveMaxLength(Integer raw) {
        if (raw == null || raw <= 0) {
            return DEFAULT_MAX_LENGTH;
        }
        return Math.max(raw, MIN_LENGTH_SAFEGUARD);
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 1) + "…";
    }
}
