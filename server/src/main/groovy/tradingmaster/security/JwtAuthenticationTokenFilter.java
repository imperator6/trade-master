package tradingmaster.security;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tradingmaster.db.entity.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    static final String AUTHORIZATION = "Authorization";
    static final String UTF_8 = "UTF-8";
    static final int BEGIN_INDEX = 7;
    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    UserService userService;
    @Autowired
    UsernamePasswordAuthenticationTokenFactory usernamePasswordAuthenticationTokenFactory;
    @Autowired
    SecurityAppContext securityAppContext;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authToken = request.getHeader(AUTHORIZATION);

        int beginIndex = BEGIN_INDEX;

        // for websocket connections
        // https://stackoverflow.com/questions/30887788/json-web-token-jwt-with-spring-based-sockjs-stomp-web-socket
        if(authToken == null && request.getParameter("token") != null) {
            authToken = request.getParameter("token");
            beginIndex = 0;
        }

        if(authToken != null) {
            try {
                authToken = new String(authToken.substring(beginIndex).getBytes(), UTF_8);
                SecurityContext context = securityAppContext.getContext();
                if(context.getAuthentication() == null) {
                    logger.debug("Checking authentication for token " + authToken);
                    User u = userService.validateUser(authToken, request.getRemoteAddr());
                    if(u != null) {
                        logger.debug("User " + u.getUsername() + " found.");
                        Authentication authentication = usernamePasswordAuthenticationTokenFactory.create(u);
                        context.setAuthentication(authentication);
                    }
                }
            } catch (StringIndexOutOfBoundsException e) {
                logger.error(e.getMessage());
            }

        }
        chain.doFilter(request, response);
    }

}
