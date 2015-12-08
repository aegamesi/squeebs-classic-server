package com.aegamesi.squeebsserver.ui;

import com.aegamesi.squeebsserver.util.Logger;

public class WebInterface extends BasicAuthHTTPD {

    public WebInterface(int port) {
        super(port, "Squeebs");
        Logger.log("Opening web server on port " + port);
    }

    @Override
    public boolean authenticateCredentials(String username, String password) {
        return username.equalsIgnoreCase("u") && password.equalsIgnoreCase("p");
    }

    @Override
    public Response serve(IHTTPSession session) {
        if(!requireAuthentication(session))
            return generateAuthenticationResponse();

        String msg = "<html><head><title>Squeebs Server Web Interface</title></head><body>\n";
        /*Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }*/

        for(String line : Logger.logHistory) {
            msg += line + "<br>";
        }

        return newFixedLengthResponse(msg + "</body></html>\n");
    }
}