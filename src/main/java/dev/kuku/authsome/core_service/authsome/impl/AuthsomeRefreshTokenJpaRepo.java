package dev.kuku.authsome.core_service.authsome.impl;

import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserRefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthsomeRefreshTokenJpaRepo extends JpaRepository<AuthsomeUserRefreshTokenEntity, String>, QueryByExampleExecutor<AuthsomeUserRefreshTokenEntity> {
}
