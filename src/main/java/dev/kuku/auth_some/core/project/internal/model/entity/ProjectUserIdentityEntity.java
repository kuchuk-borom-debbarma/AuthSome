package dev.kuku.auth_some.core.project.internal.model.entity;

import dev.kuku.auth_some.core.project.api.dto.IdentityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "project_user_identities")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectUserIdentityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @JoinColumn(name = "fk_project_user_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    public ProjectUserEntity projectUser;

    @Column(nullable = false, updatable = false, name = "identity_type")
    @Enumerated(EnumType.STRING)
    public IdentityType identityType;

    @Column(nullable = false, name = "identity")
    public String identity;

    @Column(name = "created_at", nullable = false)
    public long createdAt;
    @Column(name = "updated_at", nullable = false)
    public long updatedAt;
}
