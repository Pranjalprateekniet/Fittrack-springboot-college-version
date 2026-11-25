package com.pranjal.fittrackv2.repository;

import com.pranjal.fittrackv2.entity.RefreshToken;
import com.pranjal.fittrackv2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}

