package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {
}