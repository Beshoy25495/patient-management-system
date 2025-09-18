package com.bwagih.patientservice.service;

import billing.BillingResponse;
import com.bwagih.patientservice.dto.PatientRequestDTO;
import com.bwagih.patientservice.dto.PatientResponseDTO;
import com.bwagih.patientservice.exception.EmailAlreadyExistsException;
import com.bwagih.patientservice.grpc.BillingServiceGrpcClient;
import com.bwagih.patientservice.kafka.KafkaProducer;
import com.bwagih.patientservice.mapper.PatientMapper;
import com.bwagih.patientservice.model.Patient;
import com.bwagih.patientservice.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();

        return patients.stream().map(PatientMapper::toDTO).toList();
    }


    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (isExistsByEmail(patientRequestDTO)) {
            throw new EmailAlreadyExistsException(MessageFormat.format("email: {0} already in use with another patient", patientRequestDTO.getEmail()));
        }
        Patient patient = PatientMapper.toModel(patientRequestDTO);
        patient = patientRepository.save(patient);

        BillingResponse billingAccount = billingServiceGrpcClient.createBillingAccount(patient.getId().toString(),
                patient.getName(), patient.getEmail(), 0.0);

        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .build();

        kafkaProducer.sendEvent(patientEvent, "PATIENT_CREATED", "patient");

        return PatientMapper.toDTO(patient);
    }

    public boolean isExistsByEmail(PatientRequestDTO patientRequestDTO) {
        return patientRepository.existsByEmail(patientRequestDTO.getEmail());
    }


    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {

        Patient patient = getPatient(id);

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException(MessageFormat.format("email: {0} already in use with another patient", patientRequestDTO.getEmail()));
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }

    protected Patient getPatient(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format("Patient not found with ID: {0} ", id)));
    }

    public PatientResponseDTO getPatientById(UUID id) {
        return PatientMapper.toDTO(getPatient(id));
    }

    public void deletePatient(UUID id) {
        Patient patient = getPatient(id);
        patientRepository.deleteById(id);
    }


}
