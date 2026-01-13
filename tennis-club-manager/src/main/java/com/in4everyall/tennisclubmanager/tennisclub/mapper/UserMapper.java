package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.helper.UserMappingHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = { UserMappingHelper.class }
)
public interface UserMapper {

    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "role", ignore = true) // Se asigna automáticamente en el servicio
    @Mapping(target = "licenseNumber", ignore = true) // Se genera automáticamente en el servicio
    @Mapping(target = "passwordHash", ignore = true) // Se hashea en el servicio
    UserEntity toEntity(SignUpRequest req);

    UserResponse toResponse(UserEntity user);

    @Mapping(target = "token", expression = "java(token)")
    UserResponse toResponse(UserEntity user, String token);
}
