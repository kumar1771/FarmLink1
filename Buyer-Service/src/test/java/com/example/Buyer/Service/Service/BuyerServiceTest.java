package com.example.Buyer.Service.Service;

import com.example.Buyer.Service.Dto.BuyerRequest;
import com.example.Buyer.Service.Exception.DuplicateResourceException;
import com.example.Buyer.Service.Exception.ResourceNotFoundException;
import com.example.Buyer.Service.Model.Buyer;
import com.example.Buyer.Service.Repositery.BuyerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyerServiceTest {

    @Mock private BuyerRepository buyerRepository;
    @InjectMocks private BuyerService buyerService;

    private BuyerRequest request;
    private Buyer existingBuyer;
    private final String userId = "buyer@example.com";

    @BeforeEach
    void setUp() {
        request = new BuyerRequest();
        request.setName("Jane");
        request.setEmail("jane@example.com");
        request.setPhone("9876543210");
        request.setLocation("London");

        existingBuyer = Buyer.builder()
                .id(1L)
                .userId(userId)
                .name("Jane")
                .email("jane@example.com")
                .phone("9876543210")
                .location("London")
                .build();
    }

    // ─── createBuyer ──────────────────────────────────────────────────────────

    @Test
    void createBuyer_success() {
        when(buyerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(buyerRepository.save(any(Buyer.class))).thenReturn(existingBuyer);

        Buyer result = buyerService.createBuyer(userId, request);

        assertEquals(userId, result.getUserId());
        assertEquals("Jane", result.getName());
        verify(buyerRepository, times(1)).save(any(Buyer.class));
    }

    @Test
    void createBuyer_duplicate_throwsException() {
        when(buyerRepository.findByUserId(userId)).thenReturn(Optional.of(existingBuyer));

        assertThrows(DuplicateResourceException.class,
                () -> buyerService.createBuyer(userId, request));
        verify(buyerRepository, never()).save(any());
    }

    // ─── getMyProfile ─────────────────────────────────────────────────────────

    @Test
    void getMyProfile_success() {
        when(buyerRepository.findByUserId(userId)).thenReturn(Optional.of(existingBuyer));

        Buyer result = buyerService.getMyProfile(userId);

        assertEquals(userId, result.getUserId());
        assertEquals("Jane", result.getName());
    }

    @Test
    void getMyProfile_notFound_throwsException() {
        when(buyerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> buyerService.getMyProfile(userId));
    }

    // ─── updateProfile ────────────────────────────────────────────────────────

    @Test
    void updateProfile_success() {
        BuyerRequest updateRequest = new BuyerRequest();
        updateRequest.setName("Jane Updated");
        updateRequest.setEmail("janeupdated@example.com");
        updateRequest.setPhone("1111111111");
        updateRequest.setLocation("Manchester");

        when(buyerRepository.findByUserId(userId)).thenReturn(Optional.of(existingBuyer));
        when(buyerRepository.save(any(Buyer.class))).thenAnswer(i -> i.getArgument(0));

        Buyer result = buyerService.updateProfile(userId, updateRequest);

        assertEquals("Jane Updated", result.getName());
        assertEquals("Manchester", result.getLocation());
        verify(buyerRepository, times(1)).save(existingBuyer);
    }

    @Test
    void updateProfile_notFound_throwsException() {
        when(buyerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> buyerService.updateProfile(userId, request));
        verify(buyerRepository, never()).save(any());
    }

    // ─── deleteProfile ────────────────────────────────────────────────────────

    @Test
    void deleteProfile_success() {
        when(buyerRepository.findByUserId(userId)).thenReturn(Optional.of(existingBuyer));

        buyerService.deleteProfile(userId);

        verify(buyerRepository, times(1)).delete(existingBuyer);
    }

    @Test
    void deleteProfile_notFound_throwsException() {
        when(buyerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> buyerService.deleteProfile(userId));
        verify(buyerRepository, never()).delete(any());
    }
}
