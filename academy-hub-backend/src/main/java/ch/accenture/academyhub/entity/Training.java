package ch.accenture.academyhub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    protected Training() {}

    public Training(String name, User createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getCreatedBy() {
        return createdBy;
    }


}
