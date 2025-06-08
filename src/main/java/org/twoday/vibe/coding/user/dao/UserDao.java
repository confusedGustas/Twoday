package org.twoday.vibe.coding.user.dao;


import org.twoday.vibe.coding.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserDao {

    Optional<User> findByEmail(String email);
    User saveUser(User user);
    Optional<User> findById(UUID id);

}
