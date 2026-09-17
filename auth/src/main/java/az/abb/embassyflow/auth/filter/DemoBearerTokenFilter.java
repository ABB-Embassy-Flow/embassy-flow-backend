package az.abb.embassyflow.auth.filter;

import az.abb.embassyflow.common.web.AuthAttributes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DemoBearerTokenFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer demo-token-customer-";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIX)) {
            try {
                String tokenValue = header.substring(PREFIX.length()).trim();
                request.setAttribute(AuthAttributes.CUSTOMER_ID, Long.parseLong(tokenValue));
            } catch (NumberFormatException ignored) {
                // invalid demo token -> treated as anonymous
            }
        }
        filterChain.doFilter(request, response);
    }
}