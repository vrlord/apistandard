package com.syngenta.apistandard.repository;

import com.syngenta.apistandard.entity.Users;
import com.syngenta.apistandard.interfaces.SensorHourQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TempsRepository extends JpaRepository<Users, Integer> {
    @Query(value = """
        SELECT
            sensor_sn,
            AVG(si_value) AS average_si_value,
            sensor_measurement_type,
            date_trunc('hour', timestamp) AS date
            FROM syngenta.sensors.sensors
        WHERE sensor_sn IN ('20960300-1', '20960300-2') --RH (RELATIVE HUMIDITY)
            AND timestamp BETWEEN '2022-12-09' AND now()
        GROUP BY date_trunc('hour', timestamp), sensor_measurement_type, sensor_sn
    """, nativeQuery = true)
    List<SensorHourQuery> getSensorsDataByHourAndDate();
}
