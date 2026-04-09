package com.constitution.backend.repository;

import com.constitution.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByRole(User.Role role);
    long countByRole(User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.verified = true")
    long countVerified();
}
