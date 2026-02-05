package com.app.dangdoanhtoai2280603283.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueResponse {

    private Integer year;
    private List<MonthlyRevenue> monthlyRevenues;
    private Double totalRevenue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private Integer month;
        private String monthName;
        private Double revenue;
        private Integer orderCount;
    }
}
