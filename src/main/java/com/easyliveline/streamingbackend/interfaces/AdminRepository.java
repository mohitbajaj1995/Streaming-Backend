package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
