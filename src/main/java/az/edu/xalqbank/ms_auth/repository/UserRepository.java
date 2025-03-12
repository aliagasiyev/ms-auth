package az.edu.xalqbank.ms_auth.repository;

import az.edu.xalqbank.ms_auth.entity.UserEntity;
import az.edu.xalqbank.ms_auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);


    Optional<UserEntity> findByRole(Role role);


}
