package com.syngenta.apistandard.controller;

import com.syngenta.apistandard.helper.DateRange;
import com.syngenta.apistandard.helper.HoboData;
import com.syngenta.apistandard.interfaces.SensorHourQuery;
import com.syngenta.apistandard.repository.TempsRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@CrossOrigin
@RequestMapping(path="/temps")
public class TempsController {
    private final static Logger log = Logger.getLogger("com.syngenta.apistandar.controller.TempsController");
    @Autowired
    TempsRepository tempsRepository;

    @GetMapping("/gethourlytemps")
    public @ResponseBody ResponseEntity<Map<String, Object>> getHourlyTemps(HttpServletRequest request) {
        Map<String, Object> rtn = new LinkedHashMap<>();

        List<SensorHourQuery> hourQueries = tempsRepository.getSensorsDataByHourAndDate();
        //return "get temperatures by hour since 09-12-2022";
        rtn.put("data", hourQueries);

        log.info("IP requesting: " + request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.OK).body(rtn);
    }

    @PostMapping("/getnewdata")
    public @ResponseBody ResponseEntity<Map<String, Object>> getNewData(@RequestBody DateRange dateRange) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        Map<String, Object> rtn = new LinkedHashMap<>();

        LocalDate fromDate = dateRange.getFromdate();
        LocalDate toDate = dateRange.getTodate() == null ? LocalDate.now() : dateRange.getTodate();
        String loggerSn = dateRange.getLoggersn();

        log.info("looking date range from: " + fromDate + ", to date: " + toDate + ", loggersn: " + loggerSn);

        if(fromDate != null || loggerSn != null){
            /*
                21317982 => Arriendo
                20993658 => Syngenta Arica, Sta Gema
                20758903 => Syngenta Arica´s Site
            */

            //2022-12-27

            HoboData.GetNewData(fromDate, toDate, loggerSn);
        }

        log.info("Finished");
        return ResponseEntity.status(HttpStatus.OK).body(rtn);
    }

//    @GetMapping();
}
