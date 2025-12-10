package com.fatou82.suivi.suivihoraireapi.services;

import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.repositories.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final EmployeRepository employeRepository;

    /**
     * Charge les détails de l'utilisateur (Employe) à partir de l'email pour l'authentification.
     * C'est la méthode clé utilisée par Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche l'employé dans la base de données
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        
        // Convertit les rôles de l'entité Employe en autorités Spring Security
        Collection<SimpleGrantedAuthority> authorities = employe.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNom().name())) // Ex: ROLE_ADMINISTRATEUR
                .collect(Collectors.toList());
        
        // Retourne un objet UserDetails standard de Spring Security
        return new User(employe.getEmail(), employe.getMotDePasse(), authorities);
    }
}