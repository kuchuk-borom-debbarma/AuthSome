package dev.kuku.auth_some.util_service.otp.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//Repository handles database operations and we just need to create interface for the repo and extend from JpaRepo and provide the proper entity type and id type
//since its marked as repo spring handles the life cycle of this class and we can directly use it in our other annotated class without having to create objects manually
@Repository
public interface SimpleOtpRepo extends JpaRepository<OtpEntity, String> {
}
