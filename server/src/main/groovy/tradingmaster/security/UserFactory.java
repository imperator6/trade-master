package tradingmaster.security;

import org.springframework.stereotype.Component;
import tradingmaster.db.entity.User;


@Component
public class UserFactory {

    public User create(String username, String password, String salt, String role) {
        return new User(username, password, salt, role);
    }

}
