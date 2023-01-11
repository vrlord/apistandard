package com.syngenta.apistandard.helper;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DateRange {
    private LocalDate fromdate;
    private LocalDate todate;
    private String loggersn;
}
