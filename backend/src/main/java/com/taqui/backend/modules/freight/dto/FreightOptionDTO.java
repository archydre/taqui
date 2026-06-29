package com.taqui.backend.modules.freight.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FreightOptionDTO(
        Integer id,
        String name,
        String price,
        @JsonProperty("custom_price") String customPrice,
        @JsonProperty("delivery_time") Integer deliveryTime,
        Company company
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Company(Integer id, String name, String picture) {}
}