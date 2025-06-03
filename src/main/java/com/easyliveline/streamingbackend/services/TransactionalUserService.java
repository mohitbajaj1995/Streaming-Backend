package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.interfaces.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionalUserService {

    private final UserRepository userRepository;

    /**
     * Updates the last seen timestamp for the user.
     * This method must be called via a Spring proxy to activate @Transactional.
     */
    @Transactional
    public void updateLastSeen(Long userId, long epochMillis) {
        log.debug("Updating last seen for user ID: {} at epoch: {}", userId, epochMillis);
        try {
            userRepository.updateLastSeenByIdNative(userId, epochMillis);
            // OR:
            // userRepository.updateLastSeenById(userId, epochMillis);
        } catch (Exception e) {
            log.error("Failed to update last seen for user ID: {}", userId, e);
            throw e;
        }
    }
}