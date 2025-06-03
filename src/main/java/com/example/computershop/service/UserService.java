package com.example.computershop.service;

import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> getUser() {
        return userRepository.findAll();
    }

    public List<Object[]> getTop5UserByRevenueOrder() {
        return userRepository.getUserByRevenueOrder(PageRequest.of(0, 5));
    }

    public long countUsers(){
        return userRepository.countUsers();
    };
}
