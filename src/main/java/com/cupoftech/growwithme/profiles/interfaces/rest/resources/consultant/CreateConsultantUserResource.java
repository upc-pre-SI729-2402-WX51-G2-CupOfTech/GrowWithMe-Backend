package com.cupoftech.growwithme.profiles.interfaces.rest.resources.consultant;

public record CreateConsultantUserResource(
        String firstName,
        String lastName,
        String email,
        String password,
        String dni,
        String phone
) {
}
