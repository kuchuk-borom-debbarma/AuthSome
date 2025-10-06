package dev.kuku.authsome.core_service.authsome.impl.entity;

import dev.kuku.authsome.core_service.project.api.dto.IdentityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "authsome_user_identities")
@Table(
        indexes = {
                //To Check if identity is already in use, identity & it's type is unique.
                @Index(name = "idx_identityType_identityVal", unique = true, columnList = "identity_type, identity"),
                //To Get identities of a user
                @Index(name = "idx_fk_user_id", columnList = "fk_user_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthsomeUserIdentityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @JoinColumn(name = "fk_user_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    public AuthsomeUserEntity user;

    @Column(name = "identity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    public IdentityType identityType;

    @Column(nullable = false, name = "identity")
    public String identity;

    @Column(name = "created_at", nullable = false)
    public Long createdAt;
    @Column(name = "updated_at", nullable = false)
    public Long updatedAt;
}
