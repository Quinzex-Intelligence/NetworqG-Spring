package com.networq.service;

import com.networq.dto.CreateServiceRequest;
import com.networq.dto.ServiceImageResponse;
import com.networq.dto.ServiceResponse;
import com.networq.dto.UpdateServiceRequest;
import com.networq.entity.ServiceImage;
import com.networq.repo.ServiceImageRepository;
import com.networq.repo.ServiceRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompanyServices {
    private final ServiceRepository serviceRepository;
    private final ServiceImageRepository serviceImageRepository;
    private final S3Service s3Service;

    public String createService(CreateServiceRequest request, List<MultipartFile> images) throws IOException {
        log.info("Creating Service. title={}", request.getTitle());
        validateImages(images);
        com.networq.entity.Service service =  new com.networq.entity.Service();
        service.setTitle(request.getTitle());
        service.setShortDescription(request.getShortDescription());
        service.setLongDescription(request.getLongDescription());
        service.setActive(request.getActive());
        service.setDisplayOrder(request.getDisplayOrder());
        com.networq.entity.Service savedService = serviceRepository.save(service);
        AtomicInteger order = new AtomicInteger(1);
        log.info("Uploading Images. serviceId={}, imageCount={}", savedService.getId(), images.size());
        for (MultipartFile image : images) {
            String imageKey = s3Service.uploadImage(image);
            ServiceImage serviceImage = new ServiceImage();
            serviceImage.setService(savedService);
            serviceImage.setImageKey(imageKey);
            serviceImage.setDisplayOrder(order.getAndIncrement());
            serviceImageRepository.save(serviceImage);
        }
        log.info("Service created successfully. serviceId={}, title={}", savedService.getId(), savedService.getTitle());
        return "Service created successfully.";
    }

    public String updateService(String serviceId, UpdateServiceRequest updatedService, List<MultipartFile> images) throws IOException {
        log.info("Updating Service. serviceId={}", serviceId);
        com.networq.entity.Service service = serviceRepository.findById(serviceId).orElseThrow(()->new EntityNotFoundException("Service not found"));
        service.setTitle(updatedService.getTitle());
        service.setShortDescription(updatedService.getShortDescription());
        service.setLongDescription(updatedService.getLongDescription());
        service.setActive(updatedService.getActive());
        service.setDisplayOrder(updatedService.getDisplayOrder());
        serviceRepository.save(service);
        if (images != null && !images.isEmpty()){
           validateImages(images);

           List<String> uploadedKeys = new ArrayList<>();
           log.info("Uploading Images. serviceId={}, imageCount={}", serviceId, images.size());
           for (MultipartFile image : images) {
               uploadedKeys.add(s3Service.uploadImage(image));
           }
            List<ServiceImage> existingImages = serviceImageRepository.findByServiceIdOrderByDisplayOrderAsc(serviceId);

            log.info("Replacing service images. serviceId={}, existingImageCount={}, newImageCount={}",
                    serviceId,
                    existingImages.size(),
                    uploadedKeys.size());
            existingImages.forEach(existingImage -> {
               s3Service.deleteImage(existingImage.getImageKey());
           });
           serviceImageRepository.deleteByServiceId(serviceId);
           AtomicInteger order = new AtomicInteger(1);
           uploadedKeys.forEach(key -> {


                  ServiceImage serviceImage = new ServiceImage();
                  serviceImage.setService(service);
                  serviceImage.setImageKey(key);
                  serviceImage.setDisplayOrder(order.getAndIncrement());
                  serviceImageRepository.save(serviceImage);

           });
       }
        log.info("Service updated successfully. serviceId={}", serviceId);
        return "Service updated successfully.";
    }

    public String deleteService(String serviceId) throws IOException {
        log.info("Deleting Service. serviceId={}", serviceId);
        com.networq.entity.Service service = serviceRepository.findById(serviceId).orElseThrow(()->new EntityNotFoundException("Service not found"));
        List<ServiceImage> images = serviceImageRepository.findByServiceIdOrderByDisplayOrderAsc(serviceId);
        images.forEach(image -> {
            s3Service.deleteImage(image.getImageKey());
        });
        serviceRepository.delete(service);
        log.info("Service deleted successfully. serviceId={}, deletedImageCount={}", serviceId, images.size());
        return "Service deleted successfully.";
    }
    @Transactional(readOnly = true)
    public Page<ServiceResponse> getAllServices(Pageable pageable) {
        return serviceRepository.findAll(pageable).map(s->mapToResponse(s));
    }
    @Transactional(readOnly = true)
    public List<ServiceResponse> getActiveServices() {
        return serviceRepository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(s->mapToResponse(s)).toList();
    }

    private void validateImages(List<MultipartFile> images) throws IOException {

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required.");
        }
        if(images.size()>3){
            throw new IllegalArgumentException("Maximum 3 images are allowed.");
        }
    }

    private ServiceResponse mapToResponse(com.networq.entity.Service service) {
       List<ServiceImageResponse> images = service.getImages()
               .stream().sorted((a,b)->a.getDisplayOrder().compareTo(b.getDisplayOrder())).map(image-> new ServiceImageResponse(image.getId(),s3Service.generatePresignedUrl(image.getImageKey()), image.getDisplayOrder())).toList();
      return new ServiceResponse(service.getId(),service.getTitle(),service.getShortDescription(),service.getLongDescription(),service.getActive(),service.getDisplayOrder(),images);
    }
}
