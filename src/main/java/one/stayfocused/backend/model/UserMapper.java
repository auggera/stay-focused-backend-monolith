package one.stayfocused.backend.model;

import one.stayfocused.backend.dto.OAuthRegisterResponseDto;
import one.stayfocused.backend.dto.UserRegisterResponseDto;
import one.stayfocused.backend.dto.UserResponseDto;
import one.stayfocused.backend.dto.UserUpdateRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserDto(User user);

    UserRegisterResponseDto toRegisterDto(User user);

    OAuthRegisterResponseDto  toOAuthDto(User user);

    User toEntity(UserResponseDto userResponseDto);
}
