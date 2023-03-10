# 모듈 설명

* `gateway` 토큰 발급 서버
* `helloworld` API 서버
* `proxy` proxy 서버

# Charon Spring Boot Starter

Charon is a reverse proxy implementation. It automatically forwards HTTP requests from one HTTP server to another and 
sends back the received HTTP response to the client. There are some alternative reverse proxy implementations like Zuul 
or Smiley's HTTP Proxy Servlet. Zuul is highly bounded to Spring Cloud Netflix, Smiley's HTTP Proxy Servlet is a simple 
one, without advanced features. Charon is a universal Spring Boot tool. It already has a lot of features implemented and 
its architecture provides an easy way to add new ones.

* https://github.com/mkopylec/charon-spring-boot-starter