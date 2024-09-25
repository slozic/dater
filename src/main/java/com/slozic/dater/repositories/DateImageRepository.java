package com.slozic.dater.repositories;

import com.slozic.dater.models.DateImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DateImageRepository extends JpaRepository<DateImage, UUID> {
    List<DateImage> findAllByDateId(UUID dateId);
}
