package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import com.pashcevich.data_unifier.exception.UserAdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PostgresUserAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PostgresUserAdapter.class);

    @Autowired
    private UserRepository userRepository;

    public List<UserEntity> getAllUsers() {
        try {
            logger.info("Fetching all users from PostgreSQL");
            List<UserEntity> users = userRepository.findAll();
            logger.info("Successfully fetched {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error fetching users from PostgreSQL", e);
            throw new UserAdapterException("Failed to fetch users from PostgreSQL", e);
        }
    }

    public UserEntity getUserById(Long id) throws UserNotFoundException {
        try {
            logger.info("Fetching user with id {} from PostgreSQL", id);
            Optional<UserEntity> user = userRepository.findById(id);
            if (user.isPresent()) {
                logger.info("Successfully fetched user with id {}", id);
                return user.get();
            } else {
                logger.warn("User with id {} not found", id);
                throw new UserNotFoundException("User with id " + id + " not found");
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching user with id {} from PostgreSQL", id, e);
            throw new UserAdapterException("Failed to fetch user with id " + id + " from PostgreSQL", e);
        }
    }

    public void saveUser(UserEntity user) {
        try {
            logger.info("Saving user to PostgreSQL: {}", user.getName());
            userRepository.save(user);
            logger.info("Successfully saved user to PostgreSQL");
        } catch (Exception e) {
            logger.error("Error saving user to PostgreSQL", e);
            throw new UserAdapterException("Failed to save user to PostgreSQL", e);
        }
    }
}
