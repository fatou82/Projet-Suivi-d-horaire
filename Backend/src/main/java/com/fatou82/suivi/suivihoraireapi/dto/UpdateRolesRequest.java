package com.fatou82.suivi.suivihoraireapi.dto;

import java.util.List;
import lombok.Data;

@Data
public class UpdateRolesRequest {
    private List<String> roleNames;
    
}
