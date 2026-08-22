package com.picpaysimples.services;

import com.picpaysimples.domain.transaction.Transaction;
import com.picpaysimples.domain.user.User;
import com.picpaysimples.dtos.TransactionDTO;
import com.picpaysimples.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class TransactionService {
    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private NotificationService notificationService;

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {
        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());

        userService.validateTransaction(sender, transaction.value());
        boolean isAuthorized = this.authorizationService.authorizeTransaction(sender, transaction.value());
        if(!isAuthorized){
            throw new Exception("Transação não autorizada");
        }
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transaction.value());
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setTimestamp(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));
        this.repository.save(newTransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);

        try {
            this.notificationService.sendNotification(sender, "Transação realizada com sucesso.");
            this.notificationService.sendNotification(receiver, "Transação recebida com sucesso.");
        } catch (Exception e) {
            System.out.println("Falha ao enviar notificação: " + e.getMessage());
        }
        return newTransaction;
    }




}
