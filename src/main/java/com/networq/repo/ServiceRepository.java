package com.networq.repo;

import com.networq.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, String> {

    List<Service> findByActiveTrueOrderByDisplayOrderAsc();

}