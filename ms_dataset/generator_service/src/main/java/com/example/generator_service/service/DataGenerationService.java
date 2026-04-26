package com.example.generator_service.service;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dto.generation.PreviewResponseDTO;

public interface DataGenerationService {

    PreviewResponseDTO generatePreview(Long projectId);

    Dataset generateDataset(Long projectId);
}
