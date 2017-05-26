package com.dewarim.cinnamon.security;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/authentication")
public class AuthenticationResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateUser(@FormParam("username") String username,
                                     @FormParam("password") String password) {

        try {

            // Authenticate the user using the credentials provided
            authenticate(username, password);

            // Issue a token for the user
            String token = issueToken(username);

            // Return the token on the response
            return Response.ok(token).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    private void authenticate(String username, String password) throws Exception {
        if(! username.equals("admin") || !password.equals("admin")) {
            throw new WebApplicationException("invalid login");
        }
        // Authenticate against a database, LDAP, file or whatever
        // Throw an Exception if the credentials are invalid
    }

    private String issueToken(String username) {
        // Issue a token (can be a random String persisted to a database or a JWT token)
        // The issued token must be associated to a user
        // Return the issued token
        return "ACME::Token";
    }
}