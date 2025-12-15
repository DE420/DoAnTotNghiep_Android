package com.example.fitnessapp.model.response.common;

import java.time.LocalDate;

public class ChartDataResponse {
    private String date;
    private double value1;
    private double value2;

    public String getDate() {
        return date;
    }

    public double getValue1() {
        return value1;
    }

    public double getValue2() {
        return value2;
    }

    @Override
    public String toString() {
        return "ChartDataResponse{" +
                "date='" + date + '\'' +
                ", value1=" + value1 +
                ", value2=" + value2 +
                '}';
    }
}
