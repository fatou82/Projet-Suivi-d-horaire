package com.fatou82.suivi.suivihoraireapi.dto;

import com.fatou82.suivi.suivihoraireapi.enums.PointageType;
import lombok.Data;

@Data
public class PointageRequest {
        private Long employeId;
        private PointageType type;
}
