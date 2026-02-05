package com.app.dangdoanhtoai2280603283.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private T content;
    private int page;
    private int limit;
    private long total;
    private int totalPages;
}
