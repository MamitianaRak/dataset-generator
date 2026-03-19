package com.example.microservice.source;

import com.example.microservice.source.interfaces.IDataSource;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@DiscriminatorValue("STATIC_LIST")
@NoArgsConstructor
@Getter
@Setter
public class StaticListSource extends BaseDataSource implements IDataSource {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "static_list_source_data", joinColumns = @JoinColumn(name = "source_id"))
    @Column(name = "data_value")
    private List<String> data = new ArrayList<>();

    public StaticListSource(List<String> data) {
        this.data = data == null ? Collections.emptyList() : new ArrayList<>(data);
    }

    public StaticListSource(String... values) {
        this.data = values == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(values));
    }

    @Override
    public List<String> fetchData() {
        return data;
    }
}

