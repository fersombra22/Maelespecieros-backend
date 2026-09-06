package com.maelespecieros.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AIInsightsResponse {
    private String insight;
    private boolean systemAltered;
}
