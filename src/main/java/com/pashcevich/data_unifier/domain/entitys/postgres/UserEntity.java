package com.pashcevich.data_unifier.domain.entitys.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    public UserEntity(String name, String email) {
        this.name = name;
        this.email = email;
        this.registrationDate = LocalDateTime.now();
    }
}
