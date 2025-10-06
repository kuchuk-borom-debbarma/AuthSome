package dev.kuku.authsome.core_service.authsome.impl;

import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthsomeUserIdentityJpaRepo extends JpaRepository<AuthsomeUserIdentityEntity, String>, QueryByExampleExecutor<AuthsomeUserIdentityEntity> {
}
