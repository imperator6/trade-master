package com.rwe.platform.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import com.rwe.platform.db.entity.User;

@Component
public interface UserRepository extends CrudRepository<User, Integer> {

     User findByUsername(String username);


}
