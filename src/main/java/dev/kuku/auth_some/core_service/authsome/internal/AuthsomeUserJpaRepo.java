package dev.kuku.auth_some.core_service.authsome.internal;

import dev.kuku.auth_some.core_service.authsome.internal.entity.AuthsomeUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthsomeUserJpaRepo extends JpaRepository<AuthsomeUserEntity, String>, QueryByExampleExecutor<AuthsomeUserEntity> {
}
