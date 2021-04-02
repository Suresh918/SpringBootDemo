package com.example.springBootDemo;

import com.example.springBootDemo.core.annotations.SecureCaseAction;
import com.example.springBootDemo.core.config.DemoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MainService {
    private DemoConfiguration demoConfiguration;

    MainService(DemoConfiguration demoConfiguration) {
        this.demoConfiguration = demoConfiguration;
    }

    @SecureCaseAction("CASE-ACTION-VALUE")
    public String testAop(String caseAction) {
        return "AOP - Success";
    }


    public DemoConfiguration getConfiguration() {
        return demoConfiguration;
    }
}
