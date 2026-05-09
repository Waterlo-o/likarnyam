package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Поиск по фамилии (для строки поиска
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
}