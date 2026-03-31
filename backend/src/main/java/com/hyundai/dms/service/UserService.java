package com.hyundai.dms.service;

import com.hyundai.dms.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDto> getAllUsers(Long dealerId, String role, String search, Pageable pageable);
    UserDto getUserById(Long id, Long dealerId);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(Long id, UserDto userDto, Long dealerId);
    void deleteUser(Long id, Long dealerId);
}
