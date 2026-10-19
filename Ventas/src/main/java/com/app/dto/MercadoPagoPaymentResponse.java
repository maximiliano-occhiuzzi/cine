package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoPaymentResponse(
    Long id,
    String status,
    @JsonProperty("external_reference") String externalReference
) {
    public boolean aprobado() {
        return "approved".equalsIgnoreCase(status);
    }
}
