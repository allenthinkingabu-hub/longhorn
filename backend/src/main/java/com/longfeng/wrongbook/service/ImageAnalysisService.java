package com.longfeng.wrongbook.service;

import com.longfeng.wrongbook.model.dto.AnalysisResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

@Service
public class ImageAnalysisService {

    private final ChatClient chatClient;

    @Value("${app.mock-ai:false}")
    private boolean mockAi;

    public ImageAnalysisService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public AnalysisResult analyzeImage(Resource imageResource, String contentType) {
        if (mockAi) {
            return mockResult();
        }

        MimeType mimeType = StringUtils.hasText(contentType)
                ? MimeType.valueOf(contentType)
                : MimeTypeUtils.IMAGE_JPEG;

        String prompt = """
                你是一位资深教育专家。请仔细分析这张错题图片，识别以下信息并严格以 JSON 格式返回（不要有其他文字）：
                {
                  "subject": "学科名称（如：数学、语文、英语、物理、化学等）",
                  "questionText": "题目原文（尽量完整）",
                  "wrongAnswer": "图中学生写的错误答案",
                  "correctAnswer": "正确答案",
                  "analysis": "详细的解题过程和错误原因分析",
                  "knowledgePoint": "涉及的核心知识点",
                  "difficulty": "难度等级（简单/中等/困难）"
                }
                """;

        return chatClient.prompt()
                .user(u -> u.text(prompt).media(mimeType, imageResource))
                .call()
                .entity(AnalysisResult.class);
    }

    private AnalysisResult mockResult() {
        return new AnalysisResult(
                "数学",
                "解方程：2x + 3 = 7",
                "x = 3",
                "x = 2",
                "将常数项移到等号右边：2x = 7 - 3 = 4，两边除以2得 x = 2。" +
                "错误原因：同学在移项时符号处理有误，7-3 误算为 7+3=10，导致 x = 5/2 ≈ 3。" +
                "建议：移项时注意变号，多做练习巩固。",
                "一元一次方程",
                "简单"
        );
    }
}
