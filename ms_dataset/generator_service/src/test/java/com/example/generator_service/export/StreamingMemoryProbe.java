package com.example.generator_service.export;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dto.entity.AttributeDTO;
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.model.types.FloatTypeDTO;
import com.example.generator_service.dto.model.types.IntegerTypeDTO;
import com.example.generator_service.dto.model.types.StringTypeDTO;
import com.example.generator_service.export.interfaces.IDataExporter.TableWriter;
import com.example.generator_service.generation.FakerDataGenerator;
import com.example.generator_service.generation.RelationalDatasetBuilder;
import java.io.BufferedWriter;
import java.io.Writer;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Exports {@code rows} rows as SQL to a writer that discards them, either streamed (the export path) or built in
 * memory first (how exports worked before). Run in its own JVM by {@link StreamingMemoryTest} with a small heap.
 */
public final class StreamingMemoryProbe {

    public static void main(String[] args) throws Exception {
        boolean streamed = args[0].equals("streamed");
        int rows = Integer.parseInt(args[1]);
        DataEntityResponseDTO table = DataEntityResponseDTO.builder().name("measure").quantity(rows)
                .attributes(List.of(
                        AttributeDTO.builder().name("amount").type(new FloatTypeDTO()).build(),
                        AttributeDTO.builder().name("score").type(new IntegerTypeDTO()).build(),
                        AttributeDTO.builder().name("label").type(new StringTypeDTO()).build()))
                .build();
        RelationalDatasetBuilder builder = new RelationalDatasetBuilder(new FakerDataGenerator("en"), new SplittableRandom(1));

        try (Writer out = new BufferedWriter(Writer.nullWriter())) {
            SQLExporter exporter = new SQLExporter();
            if (streamed) {
                TableWriter writer = exporter.open(out);
                builder.generate(List.of(table), Integer.MAX_VALUE, writer);
                writer.finish();
            } else {
                Dataset dataset = builder.build(List.of(table));
                exporter.export(dataset, out);
            }
        }
        System.out.println("exported " + rows + " rows");
    }

    private StreamingMemoryProbe() {
    }
}
