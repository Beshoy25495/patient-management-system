package com.bwagih.patientservice.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponseDTO implements Serializable {

    private String id;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;

}
