package com.dresstyle.authservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Document(collection = "users")
public class User {
    @Id
    private String userId; // MongoDB generará el ID automáticamente

    private String name;

    @Indexed(unique = true) // Garantiza que no haya correos duplicados
    private String email;

    private String passwordHash;

    private Set<String> roles;

    private String phone;

    private List<Address> addresses;

    // Campo para el servicio de fidelización
    private boolean activeSubscription;

}
