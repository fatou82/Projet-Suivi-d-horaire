package com.fatou82.suivi.suivihoraireapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // S'applique sur les méthodes
@Retention(RetentionPolicy.RUNTIME) // Disponible à l'exécution
public @interface LogAction {
    String actionType(); // Ex: "CREATION_POSTE"
    String entite();     // Ex: "Poste"
}