package ch.accenture.academyhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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

    @Email
    @Size(max = 254)
    @Pattern(
            regexp = "(?s).*\\S.*",
            message = "Email must not be blank"
    )
    @Column(name = "email", length = 254)
    private String email;

    @Size(max = 100)
    @Pattern(
            regexp = "(?s).*\\S.*",
            message = "Location must not be blank"
    )
    @Column(name = "location", length = 100)
    private String location;

    @Size(max = 150)
    @Pattern(
            regexp = "(?s).*\\S.*",
            message = "Profession must not be blank"
    )
    @Column(name = "profession", length = 150)
    private String profession;

    @Positive
    @Column(name = "apprenticeship_year")
    private Integer apprenticeshipYear;

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

    public void updateProfile(
            String email,
            String location,
            String profession,
            Integer apprenticeshipYear
    ) {
        this.email = email == null ? null : email.strip();
        this.location = location == null ? null : location.strip();
        this.profession = profession == null ? null : profession.strip();
        this.apprenticeshipYear = apprenticeshipYear;
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

    public String getEmail() {
        return email;
    }

    public String getLocation() {
        return location;
    }

    public String getProfession() {
        return profession;
    }

    public Integer getApprenticeshipYear() {
        return apprenticeshipYear;
    }
}