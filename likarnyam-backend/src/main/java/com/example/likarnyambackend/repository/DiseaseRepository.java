package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {

    @Query("SELECT d FROM Disease d JOIN d.specializations s WHERE s.id = :specializationId")
    List<Disease> findBySpecializationId(@Param("specializationId") Long specializationId);
}