package com.dresstyle.dto; // El paquete debe ser igual en ambos si es posible

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent implements Serializable {
    private String email;
    private String nombre;
    private String userId;
}