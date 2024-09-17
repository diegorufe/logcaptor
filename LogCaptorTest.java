package com.example.demo;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class LogCaptorTest {

    @InjectMocks
    private DemoService demoService;

    @Test
    void test(){
       try(LogCaptor logCaptor = LogCaptor.capture(DemoService.class)){
           demoService.demo();

           assertTrue(logCaptor.verify(Level.INFO).contains("Ent"));

           assertTrue(logCaptor.verify(Level.WARN).equals("Sale"));
       }
    }

}
