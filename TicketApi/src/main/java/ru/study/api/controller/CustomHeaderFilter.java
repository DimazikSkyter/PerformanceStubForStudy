package ru.study.api.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.study.api.dto.TicketPurchaseResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomHeaderFilter extends OncePerRequestFilter {

    //todo Заменить на кэш
    private final Map<String, Integer> cache = new HashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //todo убрать
        String header;
        if ((header = request.getHeader("uniqueId")) != null) {
            Integer check = cache.putIfAbsent(header, 1);
            if(check == null) {
                response.setStatus(400);
                filterChain.doFilter(request, response);
                return;
            }

        }

        String requestId = request.getHeader("REQUEST_ID");
        if (requestId != null) {
            response.setHeader("REQUEST_ID", requestId);
        }
        filterChain.doFilter(request, response);
    }
}
