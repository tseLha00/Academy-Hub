package ch.accenture.academyhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TrainingStatus status = TrainingStatus.TODO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "estimated_completion_date")
    private LocalDate estimatedCompletionDate;

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

    public TrainingStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    /** Sets the learner's planned dates; the start date is optional, the estimate is required. */
    public void updateDates(LocalDate startDate, LocalDate estimatedCompletionDate) {
        if (estimatedCompletionDate == null) {
            throw new IllegalArgumentException("Estimated completion date is required");
        }
        if (startDate != null && estimatedCompletionDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "Estimated completion date cannot be before the start date"
            );
        }

        this.startDate = startDate;
        this.estimatedCompletionDate = estimatedCompletionDate;
    }

    /**
     * Completing keeps an existing completion timestamp; any other status reopens the
     * assignment and clears it, so completing again records a new timestamp.
     */
    public void updateStatus(TrainingStatus status) {
        if (status == TrainingStatus.COMPLETED) {
            if (completedAt == null) {
                completedAt = Instant.now();
            }
        } else {
            completedAt = null;
        }
        this.status = status;
    }

    public void markCompleted() {
        updateStatus(TrainingStatus.COMPLETED);
    }
}