package com.fonepay.devportal.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.user.document.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByRoleName(String roleName);
}
