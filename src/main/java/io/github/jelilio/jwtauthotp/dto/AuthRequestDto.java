package io.github.jelilio.jwtauthotp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record AuthRequestDto(
    @NotNull @NotBlank
    String username,
    @NotNull @NotBlank
    String password
) {
}
