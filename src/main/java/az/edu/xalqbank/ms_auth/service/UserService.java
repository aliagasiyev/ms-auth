package az.edu.xalqbank.ms_auth.service;

import az.edu.xalqbank.ms_auth.dto.request.RegisterRequest;
import az.edu.xalqbank.ms_auth.dto.response.RegisterResponse;
import az.edu.xalqbank.ms_auth.enums.Role;

import java.util.List;

public interface UserService {

    RegisterResponse registerUser(RegisterRequest user, String creatorEmail);

    List<RegisterResponse> getUsersByRole(Role role);

    List<RegisterResponse> getAllUsers();

    String deleteUser(Long userId, String requestEmail);

}
