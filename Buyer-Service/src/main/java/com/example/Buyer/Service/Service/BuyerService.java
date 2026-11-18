package com.example.Buyer.Service.Service;

import com.example.Buyer.Service.Dto.BuyerRequest;
import com.example.Buyer.Service.Exception.DuplicateResourceException;
import com.example.Buyer.Service.Exception.ResourceNotFoundException;
import com.example.Buyer.Service.Model.Buyer;
import com.example.Buyer.Service.Repositery.BuyerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerService {

    private final BuyerRepository buyerRepository;

    @Transactional
    public Buyer createBuyer(String userId, BuyerRequest request) {
        if (buyerRepository.findByUserId(userId).isPresent()) {
            throw new DuplicateResourceException("Buyer profile already exists");
        }
        Buyer buyer = Buyer.builder()
                .userId(userId)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .location(request.getLocation())
                .build();
        log.info("Creating buyer profile for userId: {}", userId);
        return buyerRepository.save(buyer);
    }

    public Buyer getMyProfile(String userId) {
        return buyerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));
    }

    @Transactional
    public Buyer updateProfile(String userId, BuyerRequest request) {
        Buyer buyer = buyerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));
        buyer.setName(request.getName());
        buyer.setEmail(request.getEmail());
        buyer.setPhone(request.getPhone());
        buyer.setLocation(request.getLocation());
        log.info("Updating buyer profile for userId: {}", userId);
        return buyerRepository.save(buyer);
    }

    @Transactional
    public void deleteProfile(String userId) {
        Buyer buyer = buyerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));
        log.info("Deleting buyer profile for userId: {}", userId);
        buyerRepository.delete(buyer);
    }
}
