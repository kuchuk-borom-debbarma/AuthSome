package dev.kuku.authsome.core_service.project.impl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "users")
@Table(
        schema = "authsome_project",
        indexes = {
                @Index(name = "idx_project", columnList = "fk_project_id"),
        }
)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @JoinColumn(name = "fk_project_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.LAZY)
    public ProjectEntity project;

    @Column(name = "hashed_password", nullable = false)
    public String hashedPassword;
    @Column(name = "created_at", nullable = false, updatable = false)
    public Long createdAt;
    @Column(name = "updated_at", nullable = false)
    public Long updatedAt;
}
