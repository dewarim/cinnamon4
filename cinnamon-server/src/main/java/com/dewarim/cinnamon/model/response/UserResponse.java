package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "users")
public class UserResponse {
    
    List<UserInfo> userInfo = new ArrayList<>();
    
}
