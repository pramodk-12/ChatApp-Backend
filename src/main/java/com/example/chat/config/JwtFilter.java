package com.example.chat.config;

import com.example.chat.auth.models.User;
import com.example.chat.auth.repositories.UserRepository;
import com.example.chat.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository repository;

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // 🟢 Skip JWT filtering for WebSocket handshakes
        return path.startsWith("/ws");
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain chain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Check if the header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // 2. Extract username from token
            username = jwtService.extractUsername(jwt);

            // 3. If username is present and user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Find user in database
                User user = repository.findByUsername(username).orElse(null);

                // 4. Validate token against user
                if (user != null && jwtService.isTokenValid(jwt, username)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.emptyList() // Use empty list for authorities
                    );

                    // 5. Build security details from the request
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 6. Finalize authentication in the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("JWT Token expired: " + e.getMessage());
            // 1. Set the attribute for the EntryPoint to read
            request.setAttribute("expired", "Token has expired. Please log in again.");

            // 2. Instead of just return, send the error explicitly
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        }
        catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.MalformedJwtException e) {
            logger.error("Invalid JWT: " + e.getMessage());
            request.setAttribute("expired", "Invalid token session.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }
        catch (Exception e) {
            // Log the error (e.g., token expired, malformed, or signature invalid)
            // We do not throw an exception here so that the filter chain can finish.
            // This will result in a 403 Forbidden for protected routes.
            logger.error("JWT Authentication failed: " + e.getMessage());
        }

        // 7. Move to the next filter in the chain
        chain.doFilter(request, response);
    }
}