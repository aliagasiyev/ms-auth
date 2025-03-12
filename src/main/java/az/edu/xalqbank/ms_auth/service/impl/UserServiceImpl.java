package az.edu.xalqbank.ms_auth.service.impl;
import az.edu.xalqbank.ms_auth.config.RoleBasedAccessConfig;
import az.edu.xalqbank.ms_auth.dto.request.RegisterRequest;
import az.edu.xalqbank.ms_auth.dto.response.RegisterResponse;
import az.edu.xalqbank.ms_auth.entity.UserEntity;
import az.edu.xalqbank.ms_auth.enums.Role;
import az.edu.xalqbank.ms_auth.exceptions.CustomAccessDeniedException;
import az.edu.xalqbank.ms_auth.exceptions.UserAlreadyExistsException;
import az.edu.xalqbank.ms_auth.exceptions.UserNotFoundException;
import az.edu.xalqbank.ms_auth.mapper.UserMapper;
import az.edu.xalqbank.ms_auth.repository.UserRepository;
import az.edu.xalqbank.ms_auth.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse registerUser(RegisterRequest request, String creatorEmail) {
        Optional<UserEntity> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists.");
        }

        UserEntity creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new UserNotFoundException("Creator user not found"));

        Role creatorRole = creator.getRole();
        Role targetRole = request.getRole();

        if (!RoleBasedAccessConfig.getPermissions(creatorRole).canCreate(targetRole)) {
            log.error("Access denied for creatorRole: {} to create targetRole: {}", creatorRole, targetRole);
            throw new CustomAccessDeniedException(
                    "Role '" + creatorRole.name() + "' does not have permission to create role '" + targetRole.name() + "'"
            );
        }


        UserEntity userEntity = UserMapper.INSTANCE.toEntity(request);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setCreatedAt(LocalDateTime.now());

        UserEntity savedUser = userRepository.save(userEntity);

        String message = targetRole.name() + " registered successfully";

        RegisterResponse response = UserMapper.INSTANCE.toDto(savedUser);
        response.setMessage(message);

        return response;
    }

    @Override
    public List<RegisterResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(UserMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegisterResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    RegisterResponse response = UserMapper.INSTANCE.toDto(user);
                    response.setMessage(user.getRole().name() + " registered successfully");
                    return response;
                })
                .collect(Collectors.toList());
    }


    @Override
    public String deleteUser(Long userId, String requestEmail) {
        UserEntity requestUser = userRepository.findByEmail(requestEmail)
                .orElseThrow(() -> new UserNotFoundException("Requesting user not found"));

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User to delete not found"));

        if (!RoleBasedAccessConfig.getPermissions(requestUser.getRole()).canDelete(targetUser.getRole())) {
            throw new CustomAccessDeniedException("You do not have permission to delete this user");
        }

        userRepository.delete(targetUser);
        return requestUser.getRole().name() + " deleted " + targetUser.getRole().name() + " successfully";
    }


}
