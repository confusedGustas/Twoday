package org.twoday.vibe.coding.user.dao;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.user.entity.User;
import org.twoday.vibe.coding.user.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        if(email == null) {
            throw new EntityNotFoundException("Email cannot be null");
        }

        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUser(User user) {
        if(user == null) {
            throw new EntityNotFoundException("User cannot be null");
        }

        userRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) {
            throw new EntityNotFoundException("Id cannot be null");
        }

        return userRepository.findById(id);
    }

}
