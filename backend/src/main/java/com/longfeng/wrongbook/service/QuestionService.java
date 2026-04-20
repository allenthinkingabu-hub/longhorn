package com.longfeng.wrongbook.service;

import com.longfeng.wrongbook.model.dto.QuestionResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class QuestionService {

    private final AtomicLong counter = new AtomicLong(1);
    private final Map<Long, QuestionResponse> store = new ConcurrentHashMap<>();

    public long nextId() {
        return counter.getAndIncrement();
    }

    public void store(QuestionResponse response) {
        store.put(response.id(), response);
    }

    public Optional<QuestionResponse> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<QuestionResponse> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparingLong(QuestionResponse::id).reversed())
                .toList();
    }
}
