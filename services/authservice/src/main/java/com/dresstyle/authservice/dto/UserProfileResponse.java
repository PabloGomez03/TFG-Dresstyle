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
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    // Campos de compatibilidad para frontends que aun consumen direccion simple.
    private String address;
    private String city;
    private String zipCode;
    private String country;
    private List<ProfileAddressDto> addresses;
}
