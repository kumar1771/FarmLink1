package com.example.Farmer.Service.Service;

import com.example.Farmer.Service.Dto.FarmerProfileRequest;
import com.example.Farmer.Service.Exception.DuplicateResourceException;
import com.example.Farmer.Service.Exception.ResourceNotFoundException;
import com.example.Farmer.Service.Model.FarmerProfile;
import com.example.Farmer.Service.Repositery.FarmerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmerServiceTest {

    @Mock private FarmerRepository farmerRepository;
    @InjectMocks private FarmerService farmerService;

    private FarmerProfileRequest request;
    private FarmerProfile existingProfile;
    private final String userId = "john@example.com";

    @BeforeEach
    void setUp() {
        request = new FarmerProfileRequest();
        request.setFarmName("Green Farm");
        request.setFarmType("organic");
        request.setLocation("Yorkshire");
        request.setDescription("Fresh produce");

        existingProfile = FarmerProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .farmName("Green Farm")
                .farmType("organic")
                .location("Yorkshire")
                .description("Fresh produce")
                .build();
    }

    @Test
    void createProfile_success() {
        when(farmerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(farmerRepository.save(any(FarmerProfile.class))).thenReturn(existingProfile);

        FarmerProfile result = farmerService.createProfile(userId, request);

        assertEquals(userId, result.getUserId());
        assertEquals("Green Farm", result.getFarmName());
        verify(farmerRepository, times(1)).save(any(FarmerProfile.class));
    }

    @Test
    void createProfile_alreadyExists_throwsException() {
        when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));

        assertThrows(DuplicateResourceException.class,
                () -> farmerService.createProfile(userId, request));
        verify(farmerRepository, never()).save(any());
    }

    @Test
    void getMyProfile_success() {
        when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));

        FarmerProfile result = farmerService.getMyProfile(userId);

        assertEquals(userId, result.getUserId());
        assertEquals("Green Farm", result.getFarmName());
    }

    @Test
    void getMyProfile_notFound_throwsException() {
        when(farmerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> farmerService.getMyProfile(userId));
    }

    @Test
    void updateProfile_success() {
        FarmerProfileRequest updateRequest = new FarmerProfileRequest();
        updateRequest.setFarmName("Updated Farm");
        updateRequest.setFarmType("regular");
        updateRequest.setLocation("London");
        updateRequest.setDescription("Updated description");

        when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));
        when(farmerRepository.save(any(FarmerProfile.class))).thenAnswer(i -> i.getArgument(0));

        FarmerProfile result = farmerService.updateProfile(userId, updateRequest);

        assertEquals("Updated Farm", result.getFarmName());
        assertEquals("regular", result.getFarmType());
        assertEquals("London", result.getLocation());
        verify(farmerRepository, times(1)).save(existingProfile);
    }

    @Test
    void updateProfile_notFound_throwsException() {
        when(farmerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> farmerService.updateProfile(userId, request));
        verify(farmerRepository, never()).save(any());
    }

    @Test
    void deleteProfile_success() {
        when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));

        farmerService.deleteProfile(userId);

        verify(farmerRepository, times(1)).delete(existingProfile);
    }

    @Test
    void deleteProfile_notFound_throwsException() {
        when(farmerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> farmerService.deleteProfile(userId));
        verify(farmerRepository, never()).delete(any());
    }
}
