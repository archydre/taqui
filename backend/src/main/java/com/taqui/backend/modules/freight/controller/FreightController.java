package com.taqui.backend.modules.freight.controller;

import com.taqui.backend.modules.freight.dto.FreightOptionDTO;
import com.taqui.backend.modules.freight.dto.FreightQuoteRequestDTO;
import com.taqui.backend.modules.freight.service.FreightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/freight")
@RequiredArgsConstructor
public class FreightController {

    private final FreightService freightService;

    @PostMapping("/quote")
    public ResponseEntity<List<FreightOptionDTO>> quote(@Valid @RequestBody FreightQuoteRequestDTO request) {
        return ResponseEntity.ok(freightService.quote(request));
    }
}
