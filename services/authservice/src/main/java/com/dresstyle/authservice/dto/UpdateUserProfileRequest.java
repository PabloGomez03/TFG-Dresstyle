package com.dresstyle.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    // Compatibilidad con payload antiguo de direccion unica.
    private String address;
    private String city;
    private String zipCode;
    private String country;
    private List<ProfileAddressDto> addresses;
}
