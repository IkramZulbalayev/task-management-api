package com.project.taskmanager.service;

import com.project.taskmanager.auth.AuthResponse;
import com.project.taskmanager.auth.AuthService;
import com.project.taskmanager.auth.LoginRequest;
import com.project.taskmanager.auth.RegisterRequest;
import com.project.taskmanager.organization.OrgLookupResult;
import com.project.taskmanager.organization.Organization;
import com.project.taskmanager.organization.OrganizationService;
import com.project.taskmanager.security.JwtService;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.user.Role;
import com.project.taskmanager.user.User;
import com.project.taskmanager.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock OrganizationService organizationService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    @Test
    void register_newOrganization_makesUserAdmin() {
        Organization org = mock(Organization.class);
        when(org.getName()).thenReturn("Acme");
        OrgLookupResult lookup = mock(OrgLookupResult.class);
        when(lookup.getOrganization()).thenReturn(org);
        when(lookup.isNewlyCreated()).thenReturn(true);

        when(organizationService.findOrCreateOrganization("Acme")).thenReturn(lookup);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(
                new RegisterRequest("Jane", "Doe", "jane@acme.com", "secret", "Acme"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("jane@acme.com");
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getOrganizationName()).isEqualTo("Acme");
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void register_existingOrganization_makesUserMember() {
        Organization org = mock(Organization.class);
        OrgLookupResult lookup = mock(OrgLookupResult.class);
        when(lookup.getOrganization()).thenReturn(org);
        when(lookup.isNewlyCreated()).thenReturn(false);

        when(organizationService.findOrCreateOrganization("Acme")).thenReturn(lookup);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(
                new RegisterRequest("Bob", "Lee", "bob@acme.com", "secret", "Acme"));

        assertThat(response.getRole()).isEqualTo("MEMBER");
    }

    @Test
    void login_validCredentials_returnsTokenAndUserInfo() {
        Organization org = mock(Organization.class);
        when(org.getName()).thenReturn("Acme");
        User user = new User("Jane", "Doe", "jane@acme.com", "hashed", org);
        user.setRole(Role.ADMIN);

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getUser()).thenReturn(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(principal)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("jane@acme.com", "secret"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getOrganizationName()).isEqualTo("Acme");
    }

    @Test
    void login_wrongPassword_throws() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("jane@acme.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
