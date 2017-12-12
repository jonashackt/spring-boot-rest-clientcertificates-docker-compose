package de.jonashackt.controller;

import de.jonashackt.client.ServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BobController {

    public static final String RESPONSE = "Hello Rest-User, here´s Alice!";

    @Autowired
    private ServerClient serverClient;

    @RequestMapping(path="/secretservers", method=RequestMethod.GET)
    public String servercalls() {
        System.out.println("Calling Alice secretely!");
        String serverAliceResponse = serverClient.callServerAlice();

        System.out.println("Calling Tom secretely!");
        String serverTomResponse = serverClient.callServerTom();

        return String.format("Both Servers called - Alice said '%s' & Tom replied '%s'.", serverAliceResponse, serverTomResponse);
    }
}
