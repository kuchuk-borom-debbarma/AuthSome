package dev.kuku.authsome.core_service.authsome.impl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Entity(name = "refresh_tokens")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthsomeUserRefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;
    @JoinColumn(name = "fk_user_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    public AuthsomeUserEntity user;
    @Column(nullable = false, updatable = false, name = "created_at")
    public Long createdAt;
    @Column(nullable = false, name = "updated_at")
    public Long updatedAt;
    @Column(name = "expires_at", nullable = false)
    public int expiresAt;
    @Column(name = "expires_time_unit")
    @Enumerated(EnumType.STRING)
    public TimeUnit expiresTimeUnit;
    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    public Map<String, Object> metadata;
}
