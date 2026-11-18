package ch.accenture.academyhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "training_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_training_assignments_training_learner",
                columnNames = {"training_id", "learner_id"}
        )
)
public class TrainingAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "progress_percentage", nullable = false)
    private int progressPercentage;

    protected TrainingAssignment() {
        // Required by JPA.
    }

    public TrainingAssignment(Training training, User learner) {
        this.training = training;
        this.learner = learner;
    }

    public Long getId() {
        return id;
    }

    public Training getTraining() {
        return training;
    }

    public User getLearner() {
        return learner;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    /** Updates progress; a completed assignment cannot be moved backwards. */
    public void updateProgressPercentage(int progressPercentage) {
        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        if (isCompleted() && progressPercentage < 100) {
            throw new IllegalStateException("Completed training progress cannot be reduced");
        }

        this.progressPercentage = progressPercentage;
        if (progressPercentage == 100) {
            markCompleted();
        }
    }

    /** Marks this learner's assignment as completed without overwriting its original timestamp. */
    public void markCompleted() {
        if (completedAt == null) {
            completedAt = Instant.now();
        }
        progressPercentage = 100;
    }
}