package com.aegamesi.squeebsserver.ui;

import com.aegamesi.squeebsserver.Main;
import com.aegamesi.squeebsserver.squeebs.GameWebSocket;
import com.aegamesi.squeebsserver.util.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qmetric.spark.authentication.AuthenticationDetails;
import com.qmetric.spark.authentication.BasicAuthenticationFilter;
import spark.Service;
import spark.staticfiles.StaticFilesConfiguration;

public class WebInterface {
    public static void start(int port) {
        Logger.log("Opening web server on port " + port);
        Service service = Service.ignite();
        service.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });
        service.port(port);

        service.webSocket("/game", GameWebSocket.class);

        service.before("/admin/*", new BasicAuthenticationFilter(new AuthenticationDetails(
                Main.config.web_interface_username,
                Main.config.web_interface_password
        )));

        // https://stackoverflow.com/a/41152103/437550
        StaticFilesConfiguration staticHandler = new StaticFilesConfiguration();
        staticHandler.configure("/web");
        service.before((request, response) ->
                staticHandler.consume(request.raw(), response.raw()));

        service.get("/admin/api/poll", (request, response) -> {
            int start = Integer.parseInt(request.queryParamOrDefault("start", "-50"));
            int end = Integer.parseInt(request.queryParamOrDefault("end", "0"));

            JsonObject responseObject = new JsonObject();
            responseObject.add("log", generateLogObject(start, end));
            return responseObject.toString();
        });

        service.get("/admin/api/command", (request, response) -> {
            String command = request.queryParams("cmd");
            if (command != null && command.trim().length() != 0) {
                Logger.handleCommand(command);
            }

            JsonObject responseObject = new JsonObject();
            responseObject.addProperty("success", true);

            if (request.queryParams().contains("log")) {
                int start = Integer.parseInt(request.queryParams("log"));
                responseObject.add("log", generateLogObject(start, -1));
            }

            return responseObject.toString();
        });
    }

    private static JsonObject generateLogObject(int start, int end) {
        if (start < 0) {
            start = Math.max(0, start + Logger.logHistory.size());
        }

        if (end <= 0) {
            end = Logger.logHistory.size();
        }

        JsonArray linesArray = new JsonArray();
        for (int i = start; i < end; i++) {
            linesArray.add(Logger.logHistory.get(i));
        }

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("start", Logger.logHistory.size());
        responseObject.addProperty("count", linesArray.size());
        responseObject.addProperty("beginning", start);
        responseObject.add("lines", linesArray);
        return responseObject;
    }
}