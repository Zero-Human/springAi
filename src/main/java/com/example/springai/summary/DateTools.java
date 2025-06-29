package com.example.springai.summary;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

public class DateTools {
    @Tool(description = "현재 날짜와 시간을 알려주는 도구")
    LocalDateTime getCurrentDateTime() {
        System.out.println(LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()));
        return LocalDateTime.now();
    }
    @Tool(description = "알람을 설정하는 도구")
    void setAlarm(@ToolParam(description = "Time in ISO-8601 format") String time) {
        System.out.println("Alarm set for " + time);
    }
}
