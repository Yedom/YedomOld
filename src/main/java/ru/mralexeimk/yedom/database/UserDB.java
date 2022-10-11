package ru.mralexeimk.yedom.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.models.User;

import java.util.List;

@Component
public class UserDB {
    private final JdbcTemplate db;
    private final PasswordEncoder encoder;

    @Autowired
    public UserDB(JdbcTemplate jdbcTemplate, PasswordEncoder encoder) {
        this.db = jdbcTemplate;
        this.encoder = encoder;
    }

    public List<User> getUsers() {
        return db.query("SELECT * FROM users", new BeanPropertyRowMapper<>(User.class));
    }

    public User getUserByEmail(String email) {
        return db.query("SELECT * FROM users WHERE email=?", new Object[]{email},
                new BeanPropertyRowMapper<>(User.class)).stream().findAny().orElse(null);
    }

    public User getUserByUsername(String username) {
        return db.query("SELECT * FROM users WHERE username=?", new Object[]{username},
                new BeanPropertyRowMapper<>(User.class)).stream().findAny().orElse(null);
    }

    public User getUserById(int id) {
        return db.query("SELECT * FROM users WHERE id=?", new Object[]{id}, new BeanPropertyRowMapper<>(User.class))
                .stream().findAny().orElse(null);
    }

    public void save(User person) {
        db.update("INSERT INTO users(username, password, email, created_on, last_login)" +
                        " VALUES(?, ?, ?, ?, ?)", person.getUsername(), encoder.encode(person.getPassword()),
                person.getEmail(), person.getCreatedOn(), person.getLastLogin());
    }

    public void updateById(int id, User updatedPerson) {
        db.update("UPDATE users SET username=?, password=?, email=?, created_on=?, last_login=?" +
                        " WHERE id=?", updatedPerson.getUsername(),
                encoder.encode(updatedPerson.getPassword()), updatedPerson.getEmail(),
                updatedPerson.getCreatedOn(), updatedPerson.getLastLogin(), id);
    }

    public void updateByUsername(String username, User updatedPerson) {
        db.update("UPDATE users SET password=?, email=?, created_on=?, last_login=?" +
                        " WHERE username=?",
                encoder.encode(updatedPerson.getPassword()), updatedPerson.getEmail(),
                updatedPerson.getCreatedOn(), updatedPerson.getLastLogin(), username);
    }

    public void deleteById(int id) {
        db.update("DELETE FROM users WHERE id=?", id);
    }
}
