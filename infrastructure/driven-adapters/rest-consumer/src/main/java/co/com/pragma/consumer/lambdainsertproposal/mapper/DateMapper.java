package co.com.pragma.consumer.lambdainsertproposal.mapper;

import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Named("stringToLocalDate")
    public static LocalDate stringToLocalDate(String date) {
        if (date == null || date.isEmpty()) return null;
        return LocalDate.parse(date, FORMATTER);
    }

    @Named("localDateToString")
    public static String localDateToString(LocalDate date) {
        if (date == null) return null;
        return date.format(FORMATTER);
    }

    @Named("sumDaysToLimitDay")
    public static LocalDate sumDaysToLimitDay(Integer limit) {
        if (limit == null) return null;
        return LocalDate.now().plusMonths(limit);
    }
}
