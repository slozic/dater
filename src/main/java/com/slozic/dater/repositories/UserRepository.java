package com.slozic.dater.repositories;

import com.slozic.dater.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findOneByEmail(String email);
    Optional<User> findOneById(UUID id);
}
