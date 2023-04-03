package io.frebigbird.example.charon.helloworld;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/")
public class HelloWorldController {
    private int count = 0;

    @GetMapping("hello")
    public String hello(
        @RequestHeader("Authorization") String authorization
    ) {
        log.info("Authorization Header: " + authorization);
        return "hello !!!";
    }

    @GetMapping("world")
    public String world(
        @RequestHeader("Authorization") String authorization
    ) {
        log.info("Authorization Header: " + authorization);
        return "world !!!";
    }

    @GetMapping("hello/unauthorized")
    public String helloIntermittentUnauthorized(
        @RequestHeader("Authorization") String authorization,
        HttpServletResponse response
    ) {
        log.info("Authorization Header: " + authorization);
        if (++count % 5 == 0) {
            response.setStatus(401);
        }
        return "hello !!!";
    }

    @GetMapping("status")
    public String status(HttpServletResponse response, @RequestParam int code) {
        response.setStatus(code);
        return "hello world!!! status code: " + code;
    }
}
