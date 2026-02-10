package com.sisferrete;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

  @GetMapping("/ping")
  public Map<String, Boolean> ping() {
    return Map.of("ok", true);
  }
}