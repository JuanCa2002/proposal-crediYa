package co.com.pragma.api.external.authentication.users.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private String id;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String identificationNumber;
    private String secondLastName;
    private String email;
    private String address;
    private String phone;
    private LocalDate birthDate;
    private Double baseSalary;
}
