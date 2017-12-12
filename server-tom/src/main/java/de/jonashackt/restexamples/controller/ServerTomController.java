package de.jonashackt.restexamples.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerTomController {

    public static final String RESPONSE = "Hello Rest-User, here´s Tom!";

    @RequestMapping(path="/hello", method=RequestMethod.GET)
    public String helloWorld() {
        System.out.println("Rocking REST!");
    	return RESPONSE;
    }
}
