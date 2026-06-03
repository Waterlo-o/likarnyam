package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.repository.DiseaseRepository;
import com.example.likarnyambackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseRepository diseaseRepository;
    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getDiseases(
            Principal principal,
            @RequestParam(required = false) Long specializationId) {

        var diseases = specializationId != null
                ? diseaseRepository.findBySpecializationId(specializationId)
                : diseaseRepository.findAll();

        return ResponseEntity.ok(
                diseases.stream()
                        .map(d -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id",          d.getId());
                            map.put("name",        d.getName());
                            map.put("icdCode",     d.getIcdCode());
                            map.put("description", d.getDescription());
                            return map;
                        })
                        .toList()
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyDiseases(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> {
                    Long specId = doctor.getSpecialization() != null
                            ? doctor.getSpecialization().getId() : null;
                    var diseases = specId != null
                            ? diseaseRepository.findBySpecializationId(specId)
                            : diseaseRepository.findAll();

                    return ResponseEntity.ok(
                            diseases.stream()
                                    .map(d -> {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("id",          d.getId());
                                        map.put("name",        d.getName());
                                        map.put("icdCode",     d.getIcdCode());
                                        map.put("description", d.getDescription());
                                        return map;
                                    })
                                    .toList()
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }
}