package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.repository.SymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomRepository symptomRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllSymptoms() {
        return ResponseEntity.ok(
                symptomRepository.findAll().stream()
                        .map(s -> {
                            Map<String, Object> map = new java.util.HashMap<>();
                            map.put("id",       s.getId());
                            map.put("name",     s.getName());
                            map.put("icon",     s.getIcon());
                            map.put("category", s.getCategory());
                            return map;
                        })
                        .toList()
        );
    }
}