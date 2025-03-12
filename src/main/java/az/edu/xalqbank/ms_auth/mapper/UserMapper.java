package az.edu.xalqbank.ms_auth.mapper;


import az.edu.xalqbank.ms_auth.dto.request.RegisterRequest;
import az.edu.xalqbank.ms_auth.dto.response.RegisterResponse;
import az.edu.xalqbank.ms_auth.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "accountLocked", constant = "false")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "failedLoginAttempts", constant = "0")
    UserEntity toEntity(RegisterRequest registerRequest);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(target = "message", expression = "java(\"User registered successfully\")")
    RegisterResponse toDto(UserEntity userEntity);
}