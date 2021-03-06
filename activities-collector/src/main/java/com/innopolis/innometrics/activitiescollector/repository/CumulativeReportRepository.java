package com.innopolis.innometrics.activitiescollector.repository;

import com.innopolis.innometrics.activitiescollector.DTO.ITimeReportByUser;
import com.innopolis.innometrics.activitiescollector.entity.CumulativeReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface CumulativeReportRepository extends JpaRepository<CumulativeReport, Integer > {
    List<CumulativeReport> getAllByEmail(String email);




    @Query( value ="select *\n" +
            "         from innometrics.cumlativerepactivity\n" +
            "        where executable_name in (\n" +
            "           select executable_name\n" +
            "             from (select executable_name, sum(used_time), email\n" +
            "                     from innometrics.cumlativerepactivity\n" +
            "                    where email = COALESCE(cast(:email as text), email)\n" +
            "                      and date_trunc('day', captureddate) BETWEEN date(COALESCE(cast(:min_date as text), '1/1/1900')) and date(COALESCE(cast(:max_date as text), '12/31/2999'))\n" +
            "                    group by email, executable_name\n" +
            "                    order by 2 desc) s\n" +
            "             limit 5)\n" +
            "          and email = COALESCE(cast(:email as text), email)\n" +
            "          and date_trunc('day', captureddate) BETWEEN date(COALESCE(cast(:min_date as text), '1/1/1900')) and date(COALESCE(cast(:max_date as text), '12/31/2999'));", nativeQuery = true)
    List<CumulativeReport> getCumulativeReport(@Param("email") String email, @Param("min_date") Date min_date, @Param("max_date") Date max_date);
}
