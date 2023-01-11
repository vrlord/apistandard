package com.syngenta.apistandard.interfaces;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public interface SensorHourQuery {
    String getSensor_sn();

    Double getAverage_si_value();

    String getSensor_measurement_type();
//    @JsonFormat(pattern="MM/dd/yyyy hh:mm a")
    @JsonFormat(pattern="MM/dd/yyyy HH:mm:ss")
    LocalDateTime getDate();


}
