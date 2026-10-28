package com.example.generator_service.service;

import com.example.generator_service.dataset.RowSink;
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.generation.PreviewResponseDTO;
import java.util.List;

public interface DataGenerationService {

    /** A few rows per table, enough to check the model: the preview never generates a full dataset. */
    PreviewResponseDTO generatePreview(Long projectId);

    /** The project's entities, fetched before an export starts writing its response. */
    List<DataEntityResponseDTO> loadEntities(Long projectId);

    /** Generates every row and hands them to {@code sink} as they are produced. */
    void generate(List<DataEntityResponseDTO> entities, RowSink sink);
}
