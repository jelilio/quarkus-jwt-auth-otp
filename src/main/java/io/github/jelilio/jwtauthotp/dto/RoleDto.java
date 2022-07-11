package io.github.jelilio.jwtauthotp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

import static java.util.Collections.emptySet;

public record RoleDto(
    @Size(max = 100)
    @NotNull @NotBlank
    @Pattern(regexp = ROLE_NAME_REGEX_PATTERN)
    String name,
    Set<String> permissions
) {
  private static final String ROLE_NAME_REGEX_PATTERN = "^(?=[a-zA-Z_]{2,20}$)";

  public RoleDto(String name, Set<String> permissions) {
    this.name = name;
    this.permissions = permissions != null? permissions : emptySet();
  }
}
