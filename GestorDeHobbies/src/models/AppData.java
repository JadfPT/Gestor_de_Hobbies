package models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppData implements Serializable {

    private Map<String, User> users = new HashMap<>();

    public Map<String, User> getUsers() {
        return users;
    }

    public User getUser(String username) {
        return users.get(username.toLowerCase());
    }

    public void addUser(User user) {
        users.put(user.getUsername().toLowerCase(), user);
    }

    public boolean exists(String username) {
        return users.containsKey(username.toLowerCase());
    }
}
