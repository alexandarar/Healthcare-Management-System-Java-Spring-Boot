package com.hms.repository;

import com.hms.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByUserEmail(String email);

    @Query("SELECT p FROM Patient p WHERE p.active = true AND " +
           "(LOWER(p.user.fullName) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR p.user.phoneNumber LIKE CONCAT('%', :term, '%'))")
    List<Patient> search(@Param("term") String term);
}
