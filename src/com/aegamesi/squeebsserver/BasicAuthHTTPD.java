package com.aegamesi.squeebsserver;

import fi.iki.elonen.NanoHTTPD;

public abstract class BasicAuthHTTPD extends NanoHTTPD {
    private String realm;

    public BasicAuthHTTPD(int port) {
        this(port, "Realm");
    }

    public BasicAuthHTTPD(int port, String realm) {
        super(port);
        this.realm = realm;
    }

    public abstract boolean authenticateCredentials(String username, String password);

    /**
     *
     * @param session The session to check for authentication
     * @return Whether the user is authenticated -- if not, call generateAuthenticationResponse
     */
    public boolean requireAuthentication(IHTTPSession session) {
        String authorization = session.getHeaders().get("authorization");

        if(authorization == null)
            return false;

        String[] auth_segments = authorization.split(" ");
        if(auth_segments.length < 2)
            return false;

        if(!auth_segments[0].equalsIgnoreCase("Basic"))
            return false;

        String credentials = new String(Base64.decode(auth_segments[1]));
        int colon = credentials.indexOf(':');
        if(colon == -1)
            return false;

        String username = credentials.substring(0, colon);
        String password = credentials.substring(colon + 1, credentials.length());
        return authenticateCredentials(username, password);
    }

    public Response generateAuthenticationResponse() {
        Response response = newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/html", null);
        response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        return response;
    }
}
