package com.bwagih.patientservice.repository;

import com.bwagih.patientservice.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    @Meta(comment = "Check if a patient with the given email exists")
    boolean existsByEmail(String email);

    @Meta(comment = "Check if a patient with the given email exists and the id is not the same")
    boolean existsByEmailAndIdNot(String email, UUID id);
}