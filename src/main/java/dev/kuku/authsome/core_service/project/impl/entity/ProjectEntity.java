package dev.kuku.authsome.core_service.project.impl.entity;

import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "projects")
@Table(
        schema = "authsome_project",
        indexes = {
                @Index(name = "idx_authsome_user", columnList = "fk_authsome_user_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @JoinColumn(name = "fk_authsome_user_id", referencedColumnName = "id", updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    public AuthsomeUserEntity user;

    @Column(name = "name", nullable = false)
    public String name;
    @Column(name = "description")
    public String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Long createdAt;
    @Column(name = "updated_at", nullable = false)
    public Long updatedAt;
}
