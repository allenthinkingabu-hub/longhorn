package com.longfeng.wrongbook.controller;

import com.longfeng.wrongbook.model.dto.AnalysisResult;
import com.longfeng.wrongbook.model.dto.QuestionResponse;
import com.longfeng.wrongbook.service.ImageAnalysisService;
import com.longfeng.wrongbook.service.QuestionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final ImageAnalysisService imageAnalysisService;
    private final QuestionService questionService;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public QuestionController(ImageAnalysisService imageAnalysisService,
                               QuestionService questionService) {
        this.imageAnalysisService = imageAnalysisService;
        this.questionService = questionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
        }

        long id = questionService.nextId();

        try {
            // 保存文件到磁盘
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            String originalFilename = file.getOriginalFilename() != null
                    ? file.getOriginalFilename() : "image.jpg";
            String fileName = id + "_" + originalFilename;
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath);

            // 调用 AI 分析
            FileSystemResource imageResource = new FileSystemResource(filePath);
            AnalysisResult result = imageAnalysisService.analyzeImage(imageResource, file.getContentType());

            // 构建响应
            String imageUrl = "/uploads/" + fileName;
            QuestionResponse response = new QuestionResponse(
                    id,
                    result.subject(),
                    result.questionText(),
                    result.wrongAnswer(),
                    result.correctAnswer(),
                    result.analysis(),
                    result.knowledgePoint(),
                    result.difficulty(),
                    "COMPLETED",
                    imageUrl,
                    LocalDateTime.now().toString()
            );

            questionService.store(response);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "文件保存失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI 分析失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return questionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> list() {
        return ResponseEntity.ok(questionService.findAll());
    }
}
