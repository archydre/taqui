package com.taqui.backend.modules.user.entity;

import com.taqui.backend.modules.post.entity.Post;
import com.taqui.backend.modules.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @NotBlank
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @OneToMany(mappedBy = "owner")
    private List<Product> products;

    @OneToMany(mappedBy = "owner")
    private List<Post> posts;

    @NotBlank
    @Column(name = "display_name")
    private String displayName;

    @Column(name = "whatsapp")
    private String whatsapp;

    @Column(name = "pix_key")
    private String pixKey;

    @Column(name = "postal_code")
    private String postalCode;
}
