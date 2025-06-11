package com.securelogwatcher.mapper;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true) // ID is auto-generated
    @Mapping(target = "password", ignore = true) // Password is encoded in AuthService
    @Mapping(target = "role", ignore = true) // Role is set by AuthService (e.g., ROLE_USER)
    @Mapping(target = "totpSecret", ignore = true) // Set during MFA setup
    @Mapping(target = "mfaVerified", ignore = true) // Set during MFA setup
    @Mapping(target = "createdAt", ignore = true) // Handled by @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true) // Handled by @UpdateTimestamp
    @Mapping(target = "deleted", ignore = true) // Has @Builder.Default, not from DTO
    @Mapping(target = "forceLoggedOut", ignore = true) // Has @Builder.Default, not from DTO
    @Mapping(target = "enabled", ignore = true)
    User toEntity(UserDto userDto);
}
