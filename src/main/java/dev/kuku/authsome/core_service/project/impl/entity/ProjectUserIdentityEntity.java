package dev.kuku.authsome.core_service.project.impl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "user_identities")
@Table(
        schema = "authsome_project"
)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectUserIdentityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @JoinColumn(name = "fk_project_user_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    public ProjectUserEntity user;
    @JoinColumn(name = "fk_project_identity_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    public GlobalProjectIdentityEntity identity;

    @Column(name = "is_verified", nullable = false)
    public Boolean isVerified;
    @Column(name = "is_primary", nullable = false)
    public Boolean isPrimary;
    @Column(name = "created_at", nullable = false, updatable = false)
    public Long createdAt;
    @Column(name = "updated_at", nullable = false)
    public Long updatedAt;
}
