package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoWebhookDTO(
    String type,
    Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String id) {}
}
