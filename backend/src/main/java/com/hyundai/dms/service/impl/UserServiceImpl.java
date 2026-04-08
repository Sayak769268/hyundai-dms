package com.hyundai.dms.service.impl;

import com.hyundai.dms.dto.UserDto;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.repository.UserRepository;
import com.hyundai.dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Long dealerId, String role, String search, Pageable pageable) {
        return userRepository.findAllWithSearch(dealerId, role, search, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id, Long dealerId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (dealerId != null && !dealerId.equals(user.getDealerId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied to user from another dealership");
        }
        
        return mapToDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .fullName(userDto.getFullName())
                .dealerId(userDto.getDealerId())
                .isActive(userDto.getIsActive() != null ? userDto.getIsActive() : true)
                .passwordHash("temp_hash") 
                .build();
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto, Long dealerId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (dealerId != null && !dealerId.equals(user.getDealerId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setIsActive(userDto.getIsActive());

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, Long dealerId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (dealerId != null && !dealerId.equals(user.getDealerId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        
        userRepository.delete(user);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .dealerId(user.getDealerId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(user.getRoles().stream().map(com.hyundai.dms.entity.Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
