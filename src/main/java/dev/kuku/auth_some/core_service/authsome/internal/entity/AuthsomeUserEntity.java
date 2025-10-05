package dev.kuku.auth_some.core_service.authsome.internal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "authsome_users")
@Table(
        indexes = {
                @Index(name = "idx_username", unique = true, columnList = "username"),
        }
)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthsomeUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @Column(nullable = false, unique = true, length = 100, name = "username")
    public String username;

    @Column(nullable = false, length = 100, name = "hashed_password")
    public String hashedPassword;

    @Column(nullable = false, name = "created_at")
    public Long createdAt;
    @Column(nullable = false, name = "updated_at")
    public Long updatedAt;
}
