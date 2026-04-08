package com.hyundai.dms.repository;

import com.hyundai.dms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    List<User> findAllByDealerId(Long dealerId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.dealerId = :dealerId AND r.name = 'ROLE_EMPLOYEE' AND u.isActive = true")
    List<User> findEmployeesByDealerId(@Param("dealerId") Long dealerId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.dealerId = :dealerId AND u.isActive = true")
    List<User> findByRoleNameAndDealerId(@Param("roleName") String roleName, @Param("dealerId") Long dealerId);

    @Query(value = "SELECT DISTINCT u FROM User u JOIN u.roles r " +
           "WHERE (:dealerId IS NULL OR u.dealerId = :dealerId) AND " +
           "(:roleName IS NULL OR r.name = :roleName) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r " +
           "WHERE (:dealerId IS NULL OR u.dealerId = :dealerId) AND " +
           "(:roleName IS NULL OR r.name = :roleName) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<User> findAllWithSearch(
            @Param("dealerId") Long dealerId, 
            @Param("roleName") String roleName,
            @Param("search") String search, 
            org.springframework.data.domain.Pageable pageable);
}
