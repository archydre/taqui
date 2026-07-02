package com.taqui.backend.modules.address.dto;

import com.taqui.backend.modules.address.entity.UserAddress;
import com.taqui.backend.modules.order.entity.Address;

import java.util.UUID;

public record SavedAddressDTO(
        UUID id,
        String recipientName,
        String postalCode,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state
) {
    public static SavedAddressDTO from(UserAddress entity) {
        Address a = entity.getAddress();
        return new SavedAddressDTO(
                entity.getAddressId(),
                a.getRecipientName(),
                a.getPostalCode(),
                a.getStreet(),
                a.getNumber(),
                a.getComplement(),
                a.getDistrict(),
                a.getCity(),
                a.getState());
    }
}
