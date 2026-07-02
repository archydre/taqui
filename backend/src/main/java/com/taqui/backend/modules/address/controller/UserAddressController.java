package com.taqui.backend.modules.address.controller;

import com.taqui.backend.modules.address.dto.SavedAddressDTO;
import com.taqui.backend.modules.address.service.UserAddressService;
import com.taqui.backend.modules.order.dto.AddressDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<List<SavedAddressDTO>> findMyAddresses(@AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        List<SavedAddressDTO> body = userAddressService.findMyAddresses(ownerId).stream()
                .map(SavedAddressDTO::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<SavedAddressDTO> createAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        SavedAddressDTO body = SavedAddressDTO.from(userAddressService.createAddress(addressDTO, ownerId));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        userAddressService.deleteAddress(addressId, ownerId);
        return ResponseEntity.noContent().build();
    }
}
