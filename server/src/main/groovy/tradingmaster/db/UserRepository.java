package tradingmaster.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import tradingmaster.db.entity.User;

@Component
public interface UserRepository extends CrudRepository<User, Integer> {

     User findByUsername(String username);


}
