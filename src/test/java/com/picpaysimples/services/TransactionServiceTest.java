package com.picpaysimples.services;

import com.picpaysimples.domain.user.User;
import com.picpaysimples.domain.user.UserType;
import com.picpaysimples.dtos.TransactionDTO;
import com.picpaysimples.repositories.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private TransactionRepository repository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should successfully create transaction")
    void createTransactionSuccess() throws Exception {
        User sender = new User(1L, "Sender", "Test", "12312301", "sender@gmail.com", "123123", new BigDecimal(100), UserType.COMMON);
        User receiver = new User(2L, "Receiver", "Testz", "98797802", "receiver@gmail.com", "1231234", new BigDecimal(1000), UserType.COMMON);
        when(userService.findUserById(1L)).thenReturn(sender);
        when(userService.findUserById(2L)).thenReturn(receiver);

        when(authorizationService.authorizeTransaction(any(), any())).thenReturn(true);

        TransactionDTO request =  new TransactionDTO(new BigDecimal(10), 1L, 2L);
        transactionService.createTransaction(request);

        verify(repository, times(1)).save(any());

        sender.setBalance(new BigDecimal(90));
        verify(userService, times(1)).saveUser(sender);
        receiver.setBalance(new BigDecimal(1010));
        verify(userService, times(1)).saveUser(receiver);

        verify(notificationService, times(1)).sendNotification(sender, "Transação realizada com sucesso.");
        verify(notificationService, times(1)).sendNotification(receiver, "Transação recebida com sucesso.");
    }
    @Test
    @DisplayName("Should throw exception when transaction fails")
    void createTransactionFailure() throws Exception {
        User sender = new User(1L, "Sender", "Test", "12312301", "sender@gmail.com", "123123", new BigDecimal(100), UserType.COMMON);
        User receiver = new User(2L, "Receiver", "Testz", "98797802", "receiver@gmail.com", "1231234", new BigDecimal(1000), UserType.COMMON);
        when(userService.findUserById(1L)).thenReturn(sender);
        when(userService.findUserById(2L)).thenReturn(receiver);

        when(authorizationService.authorizeTransaction(any(), any())).thenReturn(false);

        Exception thrown = Assertions.assertThrows(Exception.class, () -> {
            TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
            transactionService.createTransaction(request);
        });

        Assertions.assertEquals("Transação não autorizada", thrown.getMessage());

    }

}