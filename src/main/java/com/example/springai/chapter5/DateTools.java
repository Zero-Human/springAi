package com.example.springai.chapter5;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTools {
    @Tool(description = "현재 날짜와 시간을 알려주는 도구")
    String getCurrentDateTime() {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    @Tool(description = "알람을 설정하는 도구")
    String setAlarm(@ToolParam(description = "yyyy-mm-dd hh:mi:ss") String time) {
        System.out.println("Alarm set for " + time);
        return time;
    }
}
