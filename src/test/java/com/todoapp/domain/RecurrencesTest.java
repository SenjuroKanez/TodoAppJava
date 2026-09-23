package com.todoapp.domain;

import com.todoapp.domain.Models.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RecurrencesTest {
    private Task task(String date,Recurrence rule){return new Task("id","project","Repeat","","OPEN","MEDIUM","",List.of(),date,"","UTC",0,"",rule,List.of(),List.of(),List.of(),List.of(),0,0,"","","","","");}
    private Clock at(String date){return Clock.fixed(Instant.parse(date+"T12:00:00Z"),ZoneOffset.UTC);}
    @Test void skipsOverdueDailyOccurrences(){assertEquals(LocalDate.of(2026,3,11),Recurrences.next(task("2026-03-01",new Recurrence("DAILY",1,List.of(),"",0)),at("2026-03-10")).orElseThrow());}
    @Test void monthlyClampsAtMonthEnd(){assertEquals(LocalDate.of(2026,2,28),Recurrences.next(task("2026-01-31",new Recurrence("MONTHLY",1,List.of(),"",0)),at("2026-01-31")).orElseThrow());}
    @Test void weeklyHonorsSelectedDaysAndInterval(){assertEquals(LocalDate.of(2026,3,18),Recurrences.next(task("2026-03-02",new Recurrence("WEEKLY",2,List.of(1,3),"",0)),at("2026-03-16")).orElseThrow());}
    @Test void endDateAndCountStopSeries(){assertTrue(Recurrences.next(task("2026-03-01",new Recurrence("DAILY",1,List.of(),"2026-03-05",0)),at("2026-03-05")).isEmpty());assertTrue(Recurrences.next(task("2026-03-01",new Recurrence("DAILY",1,List.of(),"",1)),at("2026-03-01")).isEmpty());}
    @Test void yearlyHandlesLeapDay(){assertEquals(LocalDate.of(2025,2,28),Recurrences.next(task("2024-02-29",new Recurrence("YEARLY",1,List.of(),"",0)),at("2024-02-29")).orElseThrow());}
}
