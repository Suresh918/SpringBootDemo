package com.example.springBootDemo;

import com.example.springBootDemo.core.config.DemoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller    // This means that this class is a Controller
@RequestMapping(path = "/demo") // This means URL's start with /demo (after Application path)
public class MainController {

    private UserRepository userRepository;

    private MainService mainService;

    MainController(UserRepository userRepository, MainService mainService) {
        this.userRepository = userRepository;
        this.mainService = mainService;
    }

    @PostMapping(path = "/add") // Map ONLY POST Requests
    public @ResponseBody
    String addNewUser(@RequestParam String name
            , @RequestParam String email) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        User n = new User();
        n.setName(name);
        n.setEmail(email);
        userRepository.save(n);
        return "Saved";
    }

    @GetMapping(path = "/all")
    public @ResponseBody
    Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }

    @GetMapping(path = "/aop-test/{case-action}")
    @ResponseBody
    public String testAop(@PathVariable("case-action") String caseAction) {
        return mainService.testAop(caseAction);
    }

    @GetMapping(path = "/config")
    @ResponseBody
    public DemoConfiguration getConfiguration() {
        return mainService.getConfiguration();
    }
}
