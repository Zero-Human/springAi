package com.example.springai.summary;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
public class Controller {

    private final ServiceV1 serviceV1;

    @GetMapping("/summary")
    public String summary(@RequestParam String text){
        System.out.println("start");
        String result =  serviceV1.summary(text);
        System.out.println("end");
        return result;

    }
    @PostMapping("/summary")
    public String summary2(@RequestBody String text){
        System.out.println("start");
        String result =  serviceV1.summary(text);
        System.out.println("end");
        return result;

    }
}
