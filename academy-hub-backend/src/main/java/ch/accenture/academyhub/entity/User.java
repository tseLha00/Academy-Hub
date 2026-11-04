package ch.accenture.academyhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Locale;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 75)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 75)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "username", length = 80, unique = true)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    protected User() {
        // Required by JPA.
    }

    public User(String firstName, String lastName, Role role) {
        this(firstName, lastName, role, null, null);
    }

    public User(
            String firstName,
            String lastName,
            Role role,
            String username,
            String passwordHash
    ) {
        this.firstName = firstName.strip();
        this.lastName = lastName.strip();
        this.role = role;
        this.username = username == null
                ? null
                : username.strip().toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Role getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}