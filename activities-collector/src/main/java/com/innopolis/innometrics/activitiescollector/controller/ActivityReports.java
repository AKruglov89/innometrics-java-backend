package com.innopolis.innometrics.activitiescollector.controller;

import com.innopolis.innometrics.activitiescollector.DTO.*;
import com.innopolis.innometrics.activitiescollector.DTO.Report;
import com.innopolis.innometrics.activitiescollector.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping(value = "/V1/Reports", produces = MediaType.APPLICATION_JSON_VALUE)
public class ActivityReports {
    @Autowired
    ActivityService activityService;

    @GetMapping("/activitiesReport")
    public ResponseEntity<ActivitiesReportByUserResponse> getReportActivitiesByUser(@RequestParam(required = false) String projectID,
                                                                                    @RequestParam(required = false) String email,
                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date min_Date,
                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date max_Date) {

        projectID = projectID == "" ? null : projectID;
        email = email == "" ? null : email;
        ActivitiesReportByUserRequest request = new ActivitiesReportByUserRequest(projectID, email, min_Date, max_Date);
        ActivitiesReportByUserResponse myReport = activityService.getActivitiesReportByUser(request);
        return ResponseEntity.ok(myReport);
    }

    @GetMapping("/timeReport")
    public ResponseEntity<TimeReportResponse> getTimeReport(@RequestParam(required = false) String projectID,
                                                            @RequestParam(required = false) String email,
                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date min_Date,
                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date max_Date) {
        projectID = projectID == "" ? null : projectID;
        email = email == "" ? null : email;
        TimeReportRequest request = new TimeReportRequest(projectID, email, min_Date, max_Date);
        TimeReportResponse myReport = activityService.getTimeReportByUser(request);
        return ResponseEntity.ok(myReport);
    }

    @GetMapping("/cumulativeReport")
    public ResponseEntity<CumulativeReportResponse> getCumulativeReport(@RequestParam(required = false) String email,
                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date min_Date,
                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date max_Date) {
        CumulativeReportResponse myReport = activityService.getCumulativeReportByEmail(email, min_Date, max_Date);
        return ResponseEntity.ok(myReport);
    }
}
