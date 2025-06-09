package com.securelogwatcher.mapper;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}
