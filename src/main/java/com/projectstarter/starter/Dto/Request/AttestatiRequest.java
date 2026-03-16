package com.projectstarter.starter.Dto.Request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AttestatiRequest {

    @NotEmpty
    private List<Long> atletaIds;

    private String commissioneTecnica;

    private Map<String, String> valutazioni;
}
