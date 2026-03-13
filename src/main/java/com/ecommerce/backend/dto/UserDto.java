package com.ecommerce.backend.dto;

import com.ecommerce.backend.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User profile information")
public class UserDto {
    @Schema(description = "Unique identifier of the user", example = "u1234")
    private String id;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "Email address of the user", example = "john@example.com")
    private String email;

    @Schema(description = "Role assigned to the user")
    private UserRole role;
}
