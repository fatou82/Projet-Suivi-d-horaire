package com.fatou82.suivi.suivihoraireapi.exceptions;

// Exception pour les ressources non trouvées (Employé, Poste, Rôle)
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s non trouvé avec %s : '%s'", resourceName, fieldName, fieldValue));
    }
}