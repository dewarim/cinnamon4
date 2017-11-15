package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.response.Connection;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 */
public class AuthenticationFilterIntegrationTest extends CinnamonIntegrationTest{

    @Test
    public void testAuthentication() throws IOException {
        
        // connect to get token
        String tokenRequestResult = Request.Post("http://localhost:"+cinnamonTestPort+"/cinnamon/connect")
                .bodyForm(Form.form().add("user",  "admin").add("pwd",  "admin").build())
                .execute().returnContent().asString();

        XmlMapper mapper = new XmlMapper();
        
        Connection connection = mapper.readValue(tokenRequestResult, Connection.class);
        assertThat(connection.getTicket(), is(notNullValue()));
        assertThat(connection.getTicket().matches("(?:[a-z0-9]+-){4}[a-z0-9]+"),is(true ));
        
        System.out.println(tokenRequestResult);
        
        // call api function: 
        
        
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
