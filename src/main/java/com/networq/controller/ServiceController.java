package com.networq.controller;

import com.networq.dto.CreateServiceRequest;
import com.networq.dto.ServiceResponse;
import com.networq.dto.UpdateServiceRequest;
import com.networq.service.CompanyServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final CompanyServices companyServices;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createService(
            @ModelAttribute @Valid CreateServiceRequest request,
            @RequestPart("images") List<MultipartFile> images) throws IOException {

        return ResponseEntity.ok(
                companyServices.createService(request, images));
    }

    @PutMapping(value = "/{serviceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateService(
            @PathVariable String serviceId,
            @ModelAttribute @Valid UpdateServiceRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        return ResponseEntity.ok(
                companyServices.updateService(serviceId, request, images));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<String> deleteService(@PathVariable String serviceId) throws IOException {
        return ResponseEntity.ok(companyServices.deleteService(serviceId));
    }

    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(Pageable pageable) {
        return ResponseEntity.ok(companyServices.getAllServices(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ServiceResponse>> getActiveServices() {
        return ResponseEntity.ok(companyServices.getActiveServices());
    }
}