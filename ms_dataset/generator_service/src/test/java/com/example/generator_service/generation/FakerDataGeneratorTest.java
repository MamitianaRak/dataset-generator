package com.example.generator_service.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.generator_service.dto.entity.AttributeDTO;
import com.example.generator_service.dto.model.constraints.EnumConstraintDTO;
import com.example.generator_service.dto.model.constraints.MinMaxConstraintDTO;
import com.example.generator_service.dto.model.types.IntegerTypeDTO;
import com.example.generator_service.dto.model.types.StringTypeDTO;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Explicit constraints win over what an attribute's name suggests. */
class FakerDataGeneratorTest {

    private final FakerDataGenerator generator = new FakerDataGenerator("fr");

    @Test
    void aRangeOnANameTheGeneratorRecognisesIsRespected() {
        // "age" matches a name strategy: before the fix, a live export produced 16 and 1 for a 18-90 range.
        MinMaxConstraintDTO range = new MinMaxConstraintDTO();
        range.setMin(18);
        range.setMax(90);
        AttributeDTO age = AttributeDTO.builder().name("age").type(new IntegerTypeDTO()).constraints(List.of(range)).build();

        List<Integer> values = IntStream.range(0, 2_000).mapToObj(i -> (Integer) generator.generateFromDTO(age)).toList();

        assertThat(values).allSatisfy(v -> assertThat(v).isBetween(18, 90));
        // The upper bound is reachable: Datafaker's numberBetween excludes it, hence the + 1.
        assertThat(values).contains(90);
    }

    @Test
    void allowedValuesAreTheOnlyValuesGenerated() {
        EnumConstraintDTO allowed = new EnumConstraintDTO();
        allowed.setAllowed(List.of("ADMIN", "EDITOR"));
        AttributeDTO role = AttributeDTO.builder().name("email").type(new StringTypeDTO()).constraints(List.of(allowed)).build();

        assertThat(IntStream.range(0, 200).mapToObj(i -> generator.generateFromDTO(role)))
                .allSatisfy(v -> assertThat(v).isIn("ADMIN", "EDITOR"));
    }

    @Test
    void withoutConstraintTheNameStillPicksARealisticValue() {
        AttributeDTO email = AttributeDTO.builder().name("email").type(new StringTypeDTO()).build();

        assertThat((String) generator.generateFromDTO(email)).contains("@");
    }
}
