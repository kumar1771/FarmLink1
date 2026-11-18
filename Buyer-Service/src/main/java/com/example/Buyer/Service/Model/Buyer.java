package com.example.Buyer.Service.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buyers", indexes = {
        @Index(name = "idx_buyer_user_id", columnList = "userId", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    private String name;

    private String email;

    private String phone;

    private String location;
}
