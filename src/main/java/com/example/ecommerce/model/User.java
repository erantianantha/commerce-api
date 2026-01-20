package com.example.ecommerce.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    private String username;

    private String email;

    /**
     * Optional field. In a real app this will come from auth.
     * For assignment, keep it simple.
     */
    private String role;
}
