package com.aegamesi.squeebsserver;

import fi.iki.elonen.NanoHTTPD;

import java.util.Map;

public class WebInterface extends NanoHTTPD {

    public WebInterface(int port) {
        super(port);
        Logger.log("Opening web server on port " + port);
    }


    @Override
    public Response serve(IHTTPSession session) {
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