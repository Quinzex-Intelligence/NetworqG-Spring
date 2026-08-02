package com.networq.repo;

import com.networq.entity.ServiceImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceImageRepository extends JpaRepository<ServiceImage, String> {

    List<ServiceImage> findByServiceIdOrderByDisplayOrderAsc(String serviceId);

    long countByServiceId(String serviceId);

    void deleteByServiceId(String serviceId);

}