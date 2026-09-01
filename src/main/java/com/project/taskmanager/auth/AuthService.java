package com.project.taskmanager.auth;

import com.project.taskmanager.organization.OrgLookupResult;
import com.project.taskmanager.organization.Organization;
import com.project.taskmanager.organization.OrganizationService;
import com.project.taskmanager.security.JwtService;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.user.Role;
import com.project.taskmanager.user.User;
import com.project.taskmanager.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationService organizationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, OrganizationService organizationService,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.organizationService = organizationService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        OrgLookupResult result = organizationService.findOrCreateOrganization(request.getOrganizationName());
        Organization organization = result.getOrganization();

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                organization
        );
        user.setRole(result.isNewlyCreated() ? Role.ADMIN : Role.MEMBER);
        User savedUser = userRepository.save(user);
        UserPrincipal userPrincipal = new UserPrincipal(savedUser);
        String token = jwtService.generateToken(userPrincipal);

        return new AuthResponse(savedUser.getEmail(), savedUser.getRole().name(), token);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String token = jwtService.generateToken(userPrincipal);

        return new AuthResponse(userPrincipal.getUser().getEmail(), userPrincipal.getUser().getRole().name(), token);
    }
}