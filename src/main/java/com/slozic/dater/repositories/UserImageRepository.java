package com.slozic.dater.repositories;

import com.slozic.dater.models.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserImageRepository extends JpaRepository<UserImage, UUID> {
    List<UserImage> findAllByUserId(UUID userId);
}
