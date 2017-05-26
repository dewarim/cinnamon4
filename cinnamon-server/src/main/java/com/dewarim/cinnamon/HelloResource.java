package com.dewarim.cinnamon;

import com.dewarim.cinnamon.security.Secured;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * For testing authentication until we got a class implementing more interesting methods.
 */
@Path("hello")
public class HelloResource {

    @Context
    SecurityContext securityContext;
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Hello";
    }
    
    @Secured
    @GET
    @Path("hello-secure")
    @Produces(MediaType.TEXT_PLAIN)
    public String secureIt(){
        Principal principal = securityContext.getUserPrincipal();
        return "You are secure, user "+ principal.getName()+"!";
    }
}
