package one.stayfocused.backend.mapper;

import one.stayfocused.backend.dto.OAuthRegisterResponseDto;
import one.stayfocused.backend.dto.UserRegisterResponseDto;
import one.stayfocused.backend.dto.UserResponseDto;
import one.stayfocused.backend.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);

    UserRegisterResponseDto toRegisterResponseDto(User user);

    OAuthRegisterResponseDto  toOAuthResponseDto(User user);

    User toEntity(UserResponseDto userResponseDto);
}