package com.dresstyle.authservice.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Address { 

    private String street;
    private String city;
    private String zipCode;
    private String country;

}
