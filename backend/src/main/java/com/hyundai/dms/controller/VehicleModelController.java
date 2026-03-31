package com.hyundai.dms.controller;

import com.hyundai.dms.entity.VehicleModel;
import com.hyundai.dms.entity.VehicleVariant;
import com.hyundai.dms.repository.VehicleModelRepository;
import com.hyundai.dms.repository.VehicleVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelRepository modelRepository;
    private final VehicleVariantRepository variantRepository;

    /** All models as {id, name} */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<Map<String, Object>> result = modelRepository.findAll()
                .stream()
                .map(m -> Map.<String, Object>of("id", m.getId(), "name", m.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** Variants for a given model */
    @GetMapping("/{modelId}/variants")
    public ResponseEntity<List<Map<String, Object>>> getVariants(@PathVariable Long modelId) {
        List<Map<String, Object>> result = variantRepository.findByModelId(modelId)
                .stream()
                .map(v -> Map.<String, Object>of("id", v.getId(), "name", v.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
