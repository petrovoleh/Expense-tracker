package org.expencetracker.webserver.component.service;

import org.expencetracker.webserver.component.models.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final List<User> userList;

    public UserService() {
        userList = new ArrayList<>();

        User user1 = new User("Ida", "32", "ida@mail.com");
        User user2 = new User("Hans", "26", "hans@mail.com");
        User user3 = new User("Lars", "45", "lars@mail.com");
        User user4 = new User("Ben", "32", "ben@mail.com");
        User user5 = new User("Eva", "59", "eva@mail.com");

        userList.addAll(Arrays.asList(user1,user2,user3,user4,user5));
    }

    public Optional<User> getUser(String id) {
        Optional<User> optional = Optional.empty();
        for (User user: userList) {
            if(id == user.getId()){
                optional = Optional.of(user);
                return optional;
            }
        }
        return optional;
    }
}