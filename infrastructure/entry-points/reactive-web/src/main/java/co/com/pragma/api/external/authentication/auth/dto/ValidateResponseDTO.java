package co.com.pragma.api.external.authentication.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateResponseDTO {

    private String userName;
    private String role;
    private String expirationDate;
}
