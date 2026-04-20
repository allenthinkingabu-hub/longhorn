package com.longfeng.wrongbook.model.dto;

public record QuestionResponse(
        Long id,
        String subject,
        String questionText,
        String wrongAnswer,
        String correctAnswer,
        String analysis,
        String knowledgePoint,
        String difficulty,
        String status,
        String imageUrl,
        String createdAt
) {}
