package com.example.microservice.source;

import com.example.microservice.source.interfaces.IDataSource;
import jakarta.persistence.*;
import lombok.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("FILE")
@NoArgsConstructor
@Getter
@Setter
public class FileDataSource extends BaseDataSource implements IDataSource {
    private String filePathString;

    @Transient
    private Path filePath;

    public FileDataSource(String filePath) {
        this.filePathString = filePath;
        this.filePath = Path.of(filePath);
    }

    @PostLoad
    private void initPath() {
        if (filePath == null && filePathString != null) {
            this.filePath = Path.of(filePathString);
        }
    }

    @Override
    public List<String> fetchData() {
        if (filePath == null) {
            initPath();
        }
        try {
            if (Files.exists(filePath)) {
                return Files.readAllLines(filePath);
            } else {
                return Collections.emptyList();
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}

