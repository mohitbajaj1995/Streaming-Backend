package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.interfaces.UserRepository;
import com.easyliveline.streamingbackend.models.CustomAuthUser;
import com.easyliveline.streamingbackend.models.Role;
import com.easyliveline.streamingbackend.projections.UserLoginView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserAuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;


    public UserAuthService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws RuntimeException {

        // Fetch user details from the database
        UserLoginView userLoginMetaData = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));


        Role role;
        try {
            role = roleService.getRoleDetails(userLoginMetaData.getRole());
        } catch (IllegalArgumentException e) {
            log.error("Error retrieving role for user {}: {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("Error retrieving role: " + e.getMessage(), e);
        }

        if (role.getPermissions() == null) {
            log.error("Permissions not found for user: {}", username);
            throw new UsernameNotFoundException("Permissions not found for user");
        }

        // Map permissions to GrantedAuthority
        List<SimpleGrantedAuthority> authorities = role.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission))
                .collect(Collectors.toList());

        // Add role as a GrantedAuthority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getType()));

        // Return the UserDetails object
        return new CustomAuthUser(
                userLoginMetaData.getUsername(),
                userLoginMetaData.getPassword(),
                userLoginMetaData.isEnabled(),
                accountExpirationCheck(userLoginMetaData),
                true,
                true,
                authorities,
                role.getType(),
                userLoginMetaData.getId(),
                "royal"
//                TenantUtil.resolveTenantFromRequest()
        );
    }

    private boolean accountExpirationCheck(UserLoginView userLoginMetaData) {
        if(userLoginMetaData.getRole() == RoleType.SUBSCRIBER) {
            log.info("Host account expiration check");
        }
        log.info("Account expiration check for user: {}", userLoginMetaData.getUsername());
        return true;
    }
}
