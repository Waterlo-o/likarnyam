package com.example.likarnyambackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "specializations")
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
}