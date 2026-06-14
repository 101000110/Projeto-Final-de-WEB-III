package com.example.msuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestCodeDto {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Informe um email válido")
    private String email;
}
