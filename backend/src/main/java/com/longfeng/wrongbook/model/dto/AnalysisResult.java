package com.longfeng.wrongbook.model.dto;

public record AnalysisResult(
        String subject,
        String questionText,
        String wrongAnswer,
        String correctAnswer,
        String analysis,
        String knowledgePoint,
        String difficulty
) {}
