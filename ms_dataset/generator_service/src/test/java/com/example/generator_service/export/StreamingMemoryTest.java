package com.example.generator_service.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * The export's memory does not grow with the row count. Each run is a separate JVM limited to 64 MB of heap, so
 * the limit is real: a million rows stream through it, while building them in memory first, as exports did
 * before, does not fit.
 */
class StreamingMemoryTest {

    private static final int ROWS = 1_000_000;
    private static final String HEAP = "-Xmx64m";

    @Test
    void aMillionRowsStreamThroughA64MbHeap() throws Exception {
        Run run = probe("streamed");

        assertThat(run.exitCode()).as(run.output()).isZero();
        assertThat(run.output()).contains("exported " + ROWS + " rows");
    }

    @Test
    void theSameRowsBuiltInMemoryFirstDoNotFit() throws Exception {
        Run run = probe("in-memory");

        assertThat(run.exitCode()).isNotZero();
        assertThat(run.output()).contains("OutOfMemoryError");
    }

    private record Run(int exitCode, String output) {
    }

    private static Run probe(String mode) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                HEAP, "-cp", System.getProperty("java.class.path"),
                StreamingMemoryProbe.class.getName(), mode, String.valueOf(ROWS))
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        System.out.printf("%n[%s, %s, %,d rows] exit %d: %s%n", mode, HEAP, ROWS, exitCode,
                output.lines().filter(line -> line.contains("exported") || line.contains("OutOfMemoryError"))
                        .findFirst().orElse("(no result line)"));
        return new Run(exitCode, output);
    }
}
