package com.easyliveline.streamingbackend.repository;

import com.easyliveline.streamingbackend.interfaces.UserRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

//    @Override
//    public User findByUsername(String username) {
//        String query = "SELECT u FROM User u WHERE u.username = :username";
//        try {
//            return entityManager.createQuery(query, User.class)
//                    .setParameter("username", username)
//                    .getSingleResult();
//        } catch (NoResultException e) {
//            throw new ResourceNotFoundException("User not found with username: " + username);
//        } catch (RuntimeException e) {
//            throw new RuntimeException("Error while fetching user", e); // Handle other exceptions properly
//        }
//    }
}