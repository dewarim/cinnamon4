package com.dewarim.cinnamon.security;

import java.io.IOException;
import java.security.Principal;

//@Secured
//@Provider
//@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter
//        implements ContainerRequestFilter 
{
//
//    @Override
//    public void filter(ContainerRequestContext requestContext) throws IOException {
//
//        // Get the HTTP Authorization header from the request
//        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
//
//        // Check if the HTTP Authorization header is present and formatted correctly 
//        if (authorizationHeader == null || !authorizationHeader.startsWith("Cinnamon")) {
//            throw new NotAuthorizedException("Authorization header must be provided");
//        }
//
//        // Extract the token from the HTTP Authorization header
//        String token = authorizationHeader.substring("Cinnamon ".length()).trim();
//
//        try {
//
//            // Validate the token
//            String username = validateToken(token);
//
//            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
//            requestContext.setSecurityContext(new SecurityContext() {
//
//                @Override
//                public Principal getUserPrincipal() {
//
//                    return new Principal() {
//
//                        @Override
//                        public String getName() {
//                            return username;
//                        }
//                    };
//                }
//
//                @Override
//                public boolean isUserInRole(String role) {
//                    return true;
//                }
//
//                @Override
//                public boolean isSecure() {
//                    return currentSecurityContext.isSecure();
//                }
//
//                @Override
//                public String getAuthenticationScheme() {
//                    return "Cinnamon";
//                }
//            });
//
//
//        } catch (Exception e) {
//            requestContext.abortWith(
//                    Response.status(Response.Status.UNAUTHORIZED).build());
//        }
//    }
//
//    private String validateToken(String token) {
//        if (!token.equals("ACME::Token")) {
//            throw new NotAuthorizedException("Failed to valdiate token!");
//        }
//        return "admin";
//        // Check if it was issued by the server and if it's not expired
//        // Throw an Exception if the token is invalid
//    }
}