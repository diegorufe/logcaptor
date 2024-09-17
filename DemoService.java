package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DemoService {

    public void demo(){
        log.info("Entra");

        log.warn("Sale");
    }
}
