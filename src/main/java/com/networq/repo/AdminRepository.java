package com.networq.repo;

import com.networq.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,String> {


    Optional<Admin> findByGoogleSub(String googleSub);

    Optional<Admin> findByEmail(String email);

    boolean existsByGoogleSub(String googleSub);

    boolean existsByEmail(String email);
}
