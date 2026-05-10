package com.example.handling_large_dataset.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "posts",
        indexes = {
                @Index(name = "idx_post_dt", columnList = "post_dt"),
                @Index(name = "idx_post_by", columnList = "post_by")
        })
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String postBy;
    private LocalDate postDt;
    private String postDetails;
}
