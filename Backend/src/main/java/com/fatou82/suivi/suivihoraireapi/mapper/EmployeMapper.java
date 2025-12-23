package com.fatou82.suivi.suivihoraireapi.mapper;

import com.fatou82.suivi.suivihoraireapi.dto.EmployeDTO; 
import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.entities.Role; 
import com.fatou82.suivi.suivihoraireapi.repositories.RoleRepository;
import com.fatou82.suivi.suivihoraireapi.enums.RoleType; 

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set; 
import java.util.stream.Collectors; 

@Mapper(componentModel = "spring") 
public abstract class EmployeMapper { 

    // Injection du RoleRepository pour la recherche des entités Role (nécessaire pour DTO -> Entity)
    @Autowired 
    protected RoleRepository roleRepository; 

    // Conversion Entité vers DTO 
    @Mapping(target = "posteNom", source = "poste.nom") 
    @Mapping(target = "roles", 
             expression = "java(employe.getRoles().stream().map(r -> r.getNom().name()).collect(java.util.stream.Collectors.toList()))") 
    @Mapping(target = "motDePasse", ignore = true) 
    public abstract EmployeDTO toDto(Employe employe);
    
    // Conversion DTO vers Entité (utilise automatiquement mapRolesToEntity)
    public abstract Employe toEntity(EmployeDTO employeDTO);
    
    // Mappage de liste
    public abstract List<EmployeDTO> toDtoList(List<Employe> employes);
    
    // -------------------------------------------------------------------------
    // MÉTHODE CUSTOM MAPSTRUCT POUR LA CONVERSION List<String> -> Set<Role>
    // -------------------------------------------------------------------------

    protected Set<Role> mapRolesToEntity(List<String> roleNames) {
        if (roleNames == null || roleRepository == null) {
            return Set.of();
        }
        
        return roleNames.stream()
                .map(RoleType::valueOf) // Convertit la chaîne en Enum RoleType
                .map(roleRepository::findByNom) // Cherche l'Optional<Role> dans la BDD
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toSet());
    }
}