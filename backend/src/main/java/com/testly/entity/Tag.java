package com.testly.entity;

import jakarta.persistence.*;
import lombok.*;

// A reusable topic label a teacher can (optionally) attach to a question,
// e.g. "Arrays", "OOP", "DBMS". Normalized so topic-wise stats stay consistent.
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
