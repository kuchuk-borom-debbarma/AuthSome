package dev.kuku.auth_some.core_service.project.internal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "project_user_passwords")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectUserPasswordEntity {
    @Id
    public String id;
    @JoinColumn(name = "fk_user_id", referencedColumnName = "id", unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    public ProjectUserEntity user;
    @Column(name = "password", nullable = false)
    public String password;
    @Column(name = "created_at", nullable = false)
    public long createdAt;
    @Column(name = "updated_at", nullable = false)
    public long updatedAt;
}
