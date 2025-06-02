package com.example.computershop.service;


import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


    public List<User> getAll() {
        return userRepository.findAll();
    }


    public Boolean create(User user) {
        try{
            this.userRepository.save(user);
            return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public Boolean update(User user) {
        try{
            this.userRepository.save(user);
            return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public Boolean delete(String userId) {
        try{
            this.userRepository.deleteById(userId);
            return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
