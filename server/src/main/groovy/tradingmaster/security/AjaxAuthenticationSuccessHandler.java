
package tradingmaster.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tradingmaster.db.entity.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class AjaxAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    HeaderHandler headerHandler;
    @Autowired
    UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.debug("Authentication Successful");
        User u = userService.createUserToken(authentication.getName(), request.getRemoteAddr());
        response.getWriter().print("{ \"token\" : \"" + u.getToken() + "\"}");
        response.setStatus(HttpServletResponse.SC_OK);
        headerHandler.process(request, response);
    }

}
