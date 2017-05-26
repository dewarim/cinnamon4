package com.dewarim.cinnamon;

import com.dewarim.cinnamon.security.AuthenticationResource;
import com.dewarim.cinnamon.security.AuthenticationFilter;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;

import static org.junit.Assert.assertEquals;


/**
 */
public class AuthenticationTest extends JerseyTest {

    @Override
    protected Application configure() {
        /*
         * Combine resources for authentication, simple secure/non-secure hello and token check. 
         */
        return new ResourceConfig(AuthenticationResource.class, HelloResource.class, AuthenticationFilter.class);
    }

    @Test
    public void testAuthentication() {
        String noAuthRequired = target("/hello/hello").request().get(String.class);
        assertEquals("Hello", noAuthRequired);

        Response forbidden = target("/hello/hello-secure").request().get();
        assertEquals(HttpStatus.SC_UNAUTHORIZED, forbidden.getStatus());

        Form form = new Form();
        form.param("username", "admin");
        form.param("password", "admin");
        Response response = target("/authentication").request().buildPost(Entity.form(form)).invoke();
        assertEquals(HttpStatus.SC_OK, response.getStatus());
        assertEquals(true, response.hasEntity());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        assertEquals("ACME::Token", response.readEntity(String.class));

        Response secureAccess = target("/hello/hello-secure").request().header("Authorization", "Cinnamon ACME::Token").get();
        assertEquals(HttpStatus.SC_OK, secureAccess.getStatus());
        assertEquals("You are secure, user admin!", secureAccess.readEntity(String.class));
    }

}
