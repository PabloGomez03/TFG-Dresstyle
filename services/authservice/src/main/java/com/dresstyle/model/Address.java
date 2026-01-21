package com.dresstyle.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Address { //Esta clase no tendra id propio en nuestra base de datos, ya que vivirá dentro de User

    private String street;
    private String city;
    private String zipCode;
    private String country;

}
