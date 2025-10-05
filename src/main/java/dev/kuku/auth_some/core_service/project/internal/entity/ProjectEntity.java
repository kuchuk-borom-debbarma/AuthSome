package dev.kuku.auth_some.core_service.project.internal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Projects that are created by authsome user
 */
@Entity(name = "projects")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String id;
    @Column(nullable = false, unique = true, name = "name")
    public String name;
    @Column(nullable = false, name = "fk_authsome_user")
    public String authsomeUser;
    @Column(nullable = false, name = "created_at")
    public long createdAt;
    @Column(nullable = false, name = "updated_at")
    private long updatedAt;
}
