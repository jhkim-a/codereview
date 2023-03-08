package io.frebigbird.example.charon.helloworld;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloWorldController {
    private int count = 0;

    @GetMapping("hello")
    public String hello(
        @RequestHeader("Authorization") String authorization,
        HttpServletResponse response
    ) {
        log.info("Authorization Header: " + authorization);
        if (count++ % 5 == 0) {
            response.setStatus(401);
        }
        return "hello world!!!";
    }

    @GetMapping("helloworld")
    public String helloWorld(
        @RequestHeader("Authorization") String authorization
    ) {
        log.info("Authorization Header: " + authorization);
        return "hello world!!!";
    }

    @GetMapping("status")
    public String status(HttpServletResponse response, @RequestParam int code) {
        response.setStatus(code);
        return "status code: " + code;
    }
}
