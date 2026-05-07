package com.dresstyle.catalogservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PagedProductResponse {
    List<ProductResponse> content;
    @JsonProperty("totalElements")
    int totalElements;
    @JsonProperty("totalPages")
    int totalPages;
    @JsonProperty("currentPage")
    int currentPage;
    @JsonProperty("pageSize")
    int pageSize;
    boolean hasNext;
    boolean hasPrevious;
}
