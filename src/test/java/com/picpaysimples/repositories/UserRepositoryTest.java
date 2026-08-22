package com.picpaysimples.repositories;

import com.picpaysimples.domain.user.User;
import com.picpaysimples.domain.user.UserType;
import com.picpaysimples.dtos.UserDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("Should get user successfully from the DB")
    void findUserByDocumentSuccess() {
        String document =  "12312301";
        UserDTO data = new UserDTO("Anon", "Tests", document, new BigDecimal(100),"anon@gmail.com", "667", UserType.COMMON);
        this.createUser(data);

        Optional<User> result = this.userRepository.findUserByDocument(document);
        assertThat(result.isPresent()).isTrue();
    }    @Test
    @DisplayName("Should fail to get user from DB when user doesn't exist")
    void findUserByDocumentFailure() {
        String document =  "12312301";

        Optional<User> result = this.userRepository.findUserByDocument(document);
        assertThat(result.isEmpty()).isTrue();
    }
    private User createUser(UserDTO data){
        User newUser = new User(data);
        this.entityManager.persist(newUser);
        return newUser;
    }
}