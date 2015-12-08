package com.aegamesi.squeebsserver.ui;

import com.aegamesi.squeebsserver.util.Logger;
import com.aegamesi.squeebsserver.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class WebInterface extends BasicAuthHTTPD {
    public Gson gson;

    public WebInterface(int port) {
        super(port, "Squeebs");
        Logger.log("Opening web server on port " + port);

        gson = new Gson();
    }

    @Override
    public boolean authenticateCredentials(String username, String password) {
        return username.equalsIgnoreCase("u") && password.equalsIgnoreCase("p");
    }

    @Override
    public Response serve(IHTTPSession session) {
        if(!requireAuthentication(session))
            return generateAuthenticationResponse();

        try {
            /* STATIC */
            if (session.getUri().equalsIgnoreCase("/")) {
                InputStream is = getClass().getClassLoader().getResourceAsStream("web/index.html");
                return newFixedLengthResponse(Response.Status.OK, "text/html", Util.inputStreamToString(is, "UTF-8"));
            }
            if (session.getUri().equalsIgnoreCase("/style.css")) {
                InputStream is = getClass().getClassLoader().getResourceAsStream("web/style.css");
                return newFixedLengthResponse(Response.Status.OK, "text/css", Util.inputStreamToString(is, "UTF-8"));
            }
            if (session.getUri().equalsIgnoreCase("/script.js")) {
                InputStream is = getClass().getClassLoader().getResourceAsStream("web/script.js");
                return newFixedLengthResponse(Response.Status.OK, "text/javascript", Util.inputStreamToString(is, "UTF-8"));
            }

            /* DYNAMIC */
            if (session.getUri().equalsIgnoreCase("/api/poll")) {
                try {
                    int start = -20;
                    if (session.getParms().containsKey("start"))
                        start = Integer.parseInt(session.getParms().get("start"));
                    if(start < 0)
                        start = Math.max(0, start + Logger.logHistory.size());

                    int count = Logger.logHistory.size() - start;
                    JsonArray linesArray = new JsonArray();
                    for (int i = 0; i < count; i++)
                        linesArray.add(Logger.logHistory.get(start + i));

                    JsonObject responseObject = new JsonObject();
                    responseObject.addProperty("start", Logger.logHistory.size());
                    responseObject.addProperty("count", count);
                    responseObject.add("lines", linesArray);
                    return newFixedLengthResponse(responseObject.toString());
                } catch (NumberFormatException e) {
                }
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "<h1>404 Not Found</h1>");
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", "<h1>500 Server Error</h1>" + e.getMessage());
        }
    }
}