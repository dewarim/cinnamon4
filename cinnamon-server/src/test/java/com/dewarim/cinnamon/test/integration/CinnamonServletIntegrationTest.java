package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.response.CinnamonError;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CinnamonServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void connectFailsWithoutValidUsername() throws IOException{
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON_CONNECT.getPath();
        HttpResponse response = Request.Post(url)
                .bodyForm(Form.form().add("user", "invalid-user").add("pwd", "admin").build())
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_UNAUTHORIZED));
        
        CinnamonError error  = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        assertThat(error.getCode(),equalTo(ErrorCode.CONNECTION_FAIL_INVALID_USERNAME.getCode()));
    }

    @Test
    public void connectFailsWithWrongPassword() throws IOException{
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON_CONNECT.getPath();
        HttpResponse response = Request.Post(url)
                .bodyForm(Form.form().add("user", "admin").add("pwd", "invalid").build())
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_UNAUTHORIZED));

        CinnamonError error  = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        assertThat(error.getCode(),equalTo(ErrorCode.CONNECTION_FAIL_WRONG_PASSWORD.getCode()));
    }
    
    
    /**
     * When base class starts the test server, connect() is called automatically to
     * provide a ticket for all other API test classes.
     */
    @Test
    public void connectSucceedsWithValidUsernameAndPassword(){
        assertThat(ticket,notNullValue());
    }
    
}
