package com.dewarim.cinnamon.integration;

import org.junit.Test;



/**
 */
public class AuthenticationTest extends CinnamonIntegrationTest{

    @Test
    public void testAuthentication() {
        
                
        
        
//        String noAuthRequired = target("/hello/hello").request().get(String.class);
//        assertEquals("Hello", noAuthRequired);
//
//        Response forbidden = target("/hello/hello-secure").request().get();
//        assertEquals(HttpStatus.SC_UNAUTHORIZED, forbidden.getStatus());
//
//        Form form = new Form();
//        form.param("username", "admin");
//        form.param("password", "admin");
//        Response response = target("/authentication").request().buildPost(Entity.form(form)).invoke();
//        assertEquals(HttpStatus.SC_OK, response.getStatus());
//        assertEquals(true, response.hasEntity());
//        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
//        assertEquals("ACME::Token", response.readEntity(String.class));
//
//        Response secureAccess = target("/hello/hello-secure").request().header("Authorization", "Cinnamon ACME::Token").get();
//        assertEquals(HttpStatus.SC_OK, secureAccess.getStatus());
//        assertEquals("You are secure, user admin!", secureAccess.readEntity(String.class));
    }

}
