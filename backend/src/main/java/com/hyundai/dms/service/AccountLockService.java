package com.hyundai.dms.service;

import com.hyundai.dms.entity.User;
import com.hyundai.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountLockService {

    private final UserRepository userRepository;

    public void loginFailed(String username) {
        // Account lock feature disabled
    }

    public void loginSucceeded(String username) {
        // Account lock feature disabled
    }

    public boolean isLocked(User user) {
        return false;
    }

    public boolean isExpired(User user) {
        return false;
    }
}
