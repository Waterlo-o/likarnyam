package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allergies")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyRepository allergyRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAllergies() {
        return ResponseEntity.ok(
                allergyRepository.findAll().stream()
                        .map(a -> {
                            Map<String, Object> map = new java.util.HashMap<>();
                            map.put("id",   a.getId());
                            map.put("name", a.getName());
                            map.put("icon", a.getIcon());
                            return map;
                        })
                        .toList()
        );
    }
}