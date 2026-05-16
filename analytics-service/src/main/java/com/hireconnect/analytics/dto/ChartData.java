package com.hireconnect.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ChartData {
    private String chartType; // line, bar, pie, doughnut
    private String title;
    private List<String> labels;
    private List<Number> values;
    private Map<String, Object> options;
    private String period; // daily, weekly, monthly, yearly
}
