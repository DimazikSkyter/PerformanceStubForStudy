package ru.study.stub.controller;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomHeaderFilter extends OncePerRequestFilter {
    @Override
    public void doFilterInternal(final HttpServletRequest request,
                                 final @NonNull HttpServletResponse response,
                                 final @NonNull FilterChain chain) throws ServletException, IOException {
        String requestId = request.getHeader("REQUEST_ID");
        if (requestId != null) {
            response.setHeader("REQUEST_ID", requestId);
        }
        chain.doFilter(request, response);
    }

}
