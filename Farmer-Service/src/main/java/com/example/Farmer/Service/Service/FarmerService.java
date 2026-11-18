package com.example.Farmer.Service.Service;

import com.example.Farmer.Service.Dto.FarmerProfileRequest;
import com.example.Farmer.Service.Exception.DuplicateResourceException;
import com.example.Farmer.Service.Exception.ResourceNotFoundException;
import com.example.Farmer.Service.Model.FarmerProfile;
import com.example.Farmer.Service.Repositery.FarmerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmerService {

    private final FarmerRepository farmerRepository;

    @Transactional
    public FarmerProfile createProfile(String userId, FarmerProfileRequest request) {
        if (farmerRepository.findByUserId(userId).isPresent()) {
            throw new DuplicateResourceException("Farmer profile already exists");
        }
        FarmerProfile farmer = FarmerProfile.builder()
                .userId(userId)
                .farmName(request.getFarmName())
                .farmType(request.getFarmType())
                .location(request.getLocation())
                .description(request.getDescription())
                .build();
        log.info("Creating farmer profile for userId: {}", userId);
        return farmerRepository.save(farmer);
    }

    public FarmerProfile getMyProfile(String userId) {
        return farmerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Transactional
    public FarmerProfile updateProfile(String userId, FarmerProfileRequest request) {
        FarmerProfile farmer = getMyProfile(userId);
        farmer.setFarmName(request.getFarmName());
        farmer.setFarmType(request.getFarmType());
        farmer.setLocation(request.getLocation());
        farmer.setDescription(request.getDescription());
        log.info("Updating farmer profile for userId: {}", userId);
        return farmerRepository.save(farmer);
    }

    @Transactional
    public void deleteProfile(String userId) {
        FarmerProfile farmer = getMyProfile(userId);
        log.info("Deleting farmer profile for userId: {}", userId);
        farmerRepository.delete(farmer);
    }
}
