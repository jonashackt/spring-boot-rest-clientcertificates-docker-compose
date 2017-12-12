package de.jonashackt.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerAliceController {

    public static final String RESPONSE = "Alice answering!";

    @RequestMapping(path="/hello", method=RequestMethod.GET)
    public String helloWorld() {
        System.out.println("Alice´ Server was called");
    	return RESPONSE;
    }
}
