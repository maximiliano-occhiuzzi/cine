package com.app.Gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class UserHeadersFilter extends OncePerRequestFilter {

    public static final String USERNAME_HEADER = "X-User-Username";
    public static final String ROLES_HEADER = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Map<String, String> added = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            added.put(USERNAME_HEADER, jwt.getSubject());

            List<String> roles = jwt.getClaimAsStringList("roles");
            added.put(ROLES_HEADER, roles == null ? "" : String.join(",", roles));
        }

        chain.doFilter(new UserHeadersRequestWrapper(request, added), response);
    }

    
    private static class UserHeadersRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> added;

        UserHeadersRequestWrapper(HttpServletRequest request, Map<String, String> added) {
            super(request);
            this.added = added;
        }

        private boolean isUserHeader(String name) {
            return USERNAME_HEADER.equalsIgnoreCase(name) || ROLES_HEADER.equalsIgnoreCase(name);
        }

        @Override
        public String getHeader(String name) {
            if (isUserHeader(name)) {
                return added.get(name);
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (isUserHeader(name)) {
                String value = added.get(name);
                return value == null
                        ? Collections.emptyEnumeration()
                        : Collections.enumeration(List.of(value));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Map<String, Boolean> names = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            Enumeration<String> original = super.getHeaderNames();
            while (original != null && original.hasMoreElements()) {
                String n = original.nextElement();
                if (!isUserHeader(n)) {
                    names.put(n, Boolean.TRUE);
                }
            }
            added.keySet().forEach(n -> names.put(n, Boolean.TRUE));
            return Collections.enumeration(names.keySet());
        }
    }
}
