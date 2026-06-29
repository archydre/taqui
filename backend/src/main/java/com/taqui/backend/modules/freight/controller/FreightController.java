package com.taqui.backend.modules.freight.controller;

import com.taqui.backend.modules.freight.dto.FreightOptionDTO;
import com.taqui.backend.modules.freight.dto.FreightQuoteRequestDTO;
import com.taqui.backend.modules.freight.dto.ProductFreightQuoteRequestDTO;
import com.taqui.backend.modules.freight.service.FreightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/freight")
@RequiredArgsConstructor
public class FreightController {

    private final FreightService freightService;

    @PostMapping("/quote")
    public ResponseEntity<List<FreightOptionDTO>> quote(@Valid @RequestBody FreightQuoteRequestDTO request) {
        return ResponseEntity.ok(freightService.quote(request));
    }

    @PostMapping("/quote/product/{productId}")
    public ResponseEntity<List<FreightOptionDTO>> quoteForProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductFreightQuoteRequestDTO request) {
        return ResponseEntity.ok(
                freightService.quoteForProduct(productId, request.toPostalCode(), request.quantity()));
    }
}
