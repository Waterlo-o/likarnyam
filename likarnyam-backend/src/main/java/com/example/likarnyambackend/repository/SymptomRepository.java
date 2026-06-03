package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {
}