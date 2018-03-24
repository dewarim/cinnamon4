package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.response.AclWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.ACL_DEFAULT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AclServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void listAclsTest() throws IOException {
        HttpResponse aclListResponse = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.ACL_GET_ACLS.getPath())
                .addHeader("ticket", ticket)
                .execute().returnResponse();
        assertThat(aclListResponse.getStatusLine().getStatusCode(),equalTo(HttpStatus.SC_OK));
        AclWrapper aclWrapper = mapper.readValue(aclListResponse.getEntity().getContent(),AclWrapper.class);
        List<Acl> acls = aclWrapper.getAcls();
        assertFalse(acls.isEmpty());
        Optional<Acl> defaultAcl = acls.stream().filter(acl -> acl.getName().equals(ACL_DEFAULT)).findFirst();
        assertTrue(defaultAcl.isPresent());
        Optional<Acl> reviewers = acls.stream().filter(acl -> acl.getName().equals("reviewers.acl")).findFirst();
        assertTrue(reviewers.isPresent());


    }
    
}
