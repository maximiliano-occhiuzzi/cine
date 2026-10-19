package com.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record MercadoPagoPreferenceRequest(
    List<Item> items,
    @JsonProperty("external_reference") String externalReference,
    @JsonProperty("notification_url") String notificationUrl,
    @JsonProperty("back_urls") BackUrls backUrls,
    @JsonProperty("auto_return") String autoReturn
) {
    public record Item(
        String title,
        Integer quantity,
        @JsonProperty("unit_price") BigDecimal unitPrice
    ) {}

    public record BackUrls(
        String success,
        String failure,
        String pending
    ) {}
}
