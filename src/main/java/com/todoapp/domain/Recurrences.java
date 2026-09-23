package com.todoapp.domain;

import com.todoapp.domain.Models.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public final class Recurrences {
    private Recurrences() {}
    public static void validate(Task t) {
        try {
            ZoneId.of(t.timezone());
            if(!t.dueDate().isEmpty())LocalDate.parse(t.dueDate());
            if(!t.dueTime().isEmpty()) {LocalTime.parse(t.dueTime());if(t.dueDate().isEmpty())throw new IllegalArgumentException();}
            if(!t.myDay().isEmpty())LocalDate.parse(t.myDay());
            for(String r:t.reminders())Instant.parse(r);
            if(!t.recurrence().endDate().isEmpty())LocalDate.parse(t.recurrence().endDate());
        } catch(Exception e) {throw new Problem("Check the date, time, reminders, and timezone.");}
        var r=t.recurrence();
        if(!Set.of("NONE","DAILY","WEEKLY","MONTHLY","YEARLY").contains(r.frequency()))throw new Problem("Invalid recurrence.");
        if(!r.frequency().equals("NONE") && t.dueDate().isEmpty())throw new Problem("Recurring tasks need a due date.");
        if(r.weekdays().stream().anyMatch(d->d==null||d<1||d>7))throw new Problem("Invalid weekdays.");
        if(!Double.isFinite(t.position()))throw new Problem("Invalid task position.");
    }
    /** Returns the first future scheduled date, not a backlog of missed occurrences. */
    public static Optional<LocalDate> next(Task task, Clock clock) {
        var rule=task.recurrence();
        if(rule.frequency().equals("NONE")||rule.remaining()==1||task.dueDate().isEmpty())return Optional.empty();
        LocalDate anchor=LocalDate.parse(task.dueDate()), today=LocalDate.now(clock.withZone(ZoneId.of(task.timezone())));
        LocalDate after=anchor.isAfter(today)?anchor:today;
        LocalDate result;
        if(rule.frequency().equals("WEEKLY")) {
            var days=rule.weekdays().isEmpty()?List.of(anchor.getDayOfWeek().getValue()):rule.weekdays();
            LocalDate week=anchor.minusDays(anchor.getDayOfWeek().getValue()-1);
            result=after.plusDays(1);
            while(!(days.contains(result.getDayOfWeek().getValue()) && ChronoUnit.WEEKS.between(week,result.minusDays(result.getDayOfWeek().getValue()-1))%rule.interval()==0))result=result.plusDays(1);
        } else {
            long count=1;result=advance(anchor,rule.frequency(),rule.interval());
            while(!result.isAfter(after)) {count++;result=advance(anchor,rule.frequency(),count*rule.interval());}
        }
        if(!rule.endDate().isEmpty()&&result.isAfter(LocalDate.parse(rule.endDate())))return Optional.empty();
        return Optional.of(result);
    }
    private static LocalDate advance(LocalDate from,String frequency,long interval) {
        return switch(frequency){case "DAILY"->from.plusDays(interval);case "MONTHLY"->from.plusMonths(interval);case "YEARLY"->from.plusYears(interval);default->throw new Problem("Invalid recurrence.");};
    }
}
