package com.taqui.backend.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressDTO(
        @NotBlank String recipientName,
        @NotBlank @Pattern(regexp = "\\d{8}") String postalCode,
        @NotBlank String street,
        @NotBlank String number,
        String complement,
        @NotBlank String district,
        @NotBlank String city,
        @NotBlank @Size(min = 2, max = 2) String state
) {}
