package dev.kuku.authsome.core_service.project.internal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * User created for projects
 */
@Entity(name = "project_users")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @Column(unique = true, nullable = false, name = "username")
    public String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "fk_project_id", referencedColumnName = "id")
    public ProjectEntity project;
}