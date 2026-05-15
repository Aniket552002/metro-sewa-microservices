package com.metrosewa.route_service.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "stations")
public class StationDocument {

    @Id
    private String id;

    private String stationName;

    private String stationCode;
}