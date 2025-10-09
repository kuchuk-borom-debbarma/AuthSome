package dev.kuku.authsome.core_service.project.impl.entity;

import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "global_identities")
@Table(
        schema = "authsome_project",
        indexes = {
                @Index(name = "idx_identity_type_identity", columnList = "identity_type, identity", unique = true)
        }
)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GlobalProjectIdentityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;
    @Enumerated(EnumType.STRING)

    @Column(name = "identity_type", nullable = false)
    public ProjectUserIdentityType identityType;
    @Column(name = "identity", nullable = false)
    public String identity;
    @Column(name = "created_at", nullable = false, updatable = false)
    public Long createdAt;
    @Column(name = "created_at", nullable = false)
    public Long updatedAt;
}
