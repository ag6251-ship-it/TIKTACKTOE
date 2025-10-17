package com.healthsense.service;

import com.healthsense.dao.UserDao;
import com.healthsense.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private User currentUser;

    public User signUp(String name, String email, String password) throws SQLException {
        // Prevent duplicate accounts
        User existing = userDao.findByEmail(email);
        if (existing != null) {
            return null;
        }
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User created = userDao.create(email, name, passwordHash);
        currentUser = created;
        return created;
    }

    public User signIn(String email, String password) throws SQLException {
        User user = userDao.findByEmail(email);
        if (user == null) return null;
        if (!BCrypt.checkpw(password, user.getPasswordHash())) return null;
        currentUser = user;
        return user;
    }

    public void signOut() { currentUser = null; }

    public User getCurrentUser() { return currentUser; }
}


