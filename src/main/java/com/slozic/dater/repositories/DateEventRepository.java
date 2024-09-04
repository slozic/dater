package com.slozic.dater.repositories;

import com.slozic.dater.models.Date;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DateEventRepository extends JpaRepository<Date, UUID> {

}