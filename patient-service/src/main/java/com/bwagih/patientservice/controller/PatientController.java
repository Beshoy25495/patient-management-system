package com.bwagih.patientservice.controller;

import com.bwagih.patientservice.dto.APIBusinessLogicResponse;
import com.bwagih.patientservice.dto.PatientRequestDTO;
import com.bwagih.patientservice.dto.PatientResponseDTO;
import com.bwagih.patientservice.dto.validators.CreatePatientValidationGroup;
import com.bwagih.patientservice.service.PatientService;
import com.bwagih.patientservice.utils.APIResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing Patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }


    @Operation(summary = "Get Patients")
    @GetMapping({"/", ""})
    public ResponseEntity<APIBusinessLogicResponse<List<PatientResponseDTO>>> getPatients() {
        List<PatientResponseDTO> patients = patientService.getPatients();
        return APIResponseHandler.createResponse(
                HttpStatus.OK,
                APIBusinessLogicResponse.success(patients)
        );
    }

    @Operation(summary = "Create a new Patient")
    @PostMapping({"/", ""})
    public ResponseEntity<APIBusinessLogicResponse<String>> createPatient(
            @Validated({Default.class, CreatePatientValidationGroup.class})
            @RequestBody PatientRequestDTO patientRequestDTO) {

        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);

        return APIResponseHandler.createResponse(
                HttpStatus.ACCEPTED,
                APIBusinessLogicResponse.success(patientResponseDTO.getId())
        );
    }


    @Operation(summary = "Update a new Patient")
    @PutMapping("/{id}")
    public ResponseEntity<APIBusinessLogicResponse<UUID>> updatePatient(@PathVariable UUID id,
                                                                        @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {

        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);

        return APIResponseHandler.createResponse(
                HttpStatus.OK,
                APIBusinessLogicResponse.success(id)
        );
    }

    @Operation(summary = "Delete a Patient")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIBusinessLogicResponse<Void>> deletePatient(@PathVariable UUID id) {

        patientService.deletePatient(id);

        return APIResponseHandler.createResponse(
                HttpStatus.OK,
                APIBusinessLogicResponse.success("Patient deleted successfully", null)
        );
    }

    @Operation(summary = "Get a Patient")
    @GetMapping("/{id}")
    public ResponseEntity<APIBusinessLogicResponse<PatientResponseDTO>> getPatient(@PathVariable UUID id) {
        PatientResponseDTO patientResponseDTO = patientService.getPatientById(id);
        return APIResponseHandler.createResponse(
                HttpStatus.OK,
                APIBusinessLogicResponse.success(patientResponseDTO)
        );
    }


}
