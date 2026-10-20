package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.CreateTrainingRequest;
import ch.accenture.academyhub.dto.TrainingDetailsResponse;
import ch.accenture.academyhub.dto.UpdateTrainingAssignmentsRequest;
import ch.accenture.academyhub.dto.UpdateTrainingRequest;
import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.Training;
import ch.accenture.academyhub.entity.TrainingAssignment;
import ch.accenture.academyhub.entity.User;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import ch.accenture.academyhub.repository.TrainingRepository;
import ch.accenture.academyhub.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Service
@Validated
public class TrainingManagementService {

    private final TrainingRepository trainingRepository;
    private final TrainingAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public TrainingManagementService(
            TrainingRepository trainingRepository,
            TrainingAssignmentRepository assignmentRepository,
            UserRepository userRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TrainingDetailsResponse createTraining(
            @NotNull @Positive Long actorId,
            @NotNull @Valid CreateTrainingRequest request
    ) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Acting user does not exist"
                ));

        Set<Long> learnerIds;

        if (actor.getRole() == Role.LEARNER) {
            if (!request.learnerIds().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Learners cannot select training assignees"
                );
            }

            learnerIds = Set.of(actor.getId());
        } else if (actor.getRole() == Role.ADMIN) {
            learnerIds = request.learnerIds();
        } else {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User role cannot create trainings"
            );
        }

        List<User> learners = userRepository.findAllById(learnerIds);

        if (learners.size() != learnerIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more selected learners do not exist"
            );
        }

        if (learners.stream().anyMatch(
                learner -> learner.getRole() != Role.LEARNER
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trainings can only be assigned to learners"
            );
        }

        Training training = trainingRepository.save(
                new Training(request.name().strip(), actor)
        );

        List<TrainingAssignment> assignments = learners.stream()
                .map(learner -> new TrainingAssignment(training, learner))
                .toList();

        assignmentRepository.saveAll(assignments);

        return new TrainingDetailsResponse(
                training.getId(),
                training.getName(),
                actor.getId(),
                learners.stream()
                        .map(User::getId)
                        .sorted()
                        .toList()
        );
    }

    @Transactional
    public void updateTraining(
            @NotNull @Positive Long actorId,
            @NotNull @Positive Long trainingId,
            @NotNull @Valid UpdateTrainingRequest request
    ) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Acting user does not exist"
                ));

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Training does not exist"
                ));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isLearnerCreator =
                actor.getRole() == Role.LEARNER
                        && training.getCreatedBy().getId().equals(actor.getId());

        if (!isAdmin && !isLearnerCreator) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot edit this training"
            );
        }

        training.rename(request.name().strip());
    }

    @Transactional
    public void updateAssignments(
            @NotNull @Positive Long actorId,
            @NotNull @Positive Long trainingId,
            @NotNull @Valid UpdateTrainingAssignmentsRequest request
    ) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Acting user does not exist"
                ));

        if (actor.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only admins can change assignments"
            );
        }

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Training does not exist"
                ));

        Set<Long> selectedIds = request.learnerIds();
        List<User> selectedLearners = userRepository.findAllById(selectedIds);

        if (selectedLearners.size() != selectedIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more selected learners do not exist"
            );
        }

        if (selectedLearners.stream().anyMatch(
                learner -> learner.getRole() != Role.LEARNER
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trainings can only be assigned to learners"
            );
        }

        List<TrainingAssignment> existing =
                assignmentRepository.findAllByTrainingId(trainingId);

        Set<Long> existingLearnerIds = existing.stream()
                .map(assignment -> assignment.getLearner().getId())
                .collect(Collectors.toSet());

        List<TrainingAssignment> removed = existing.stream()
                .filter(assignment ->
                        !selectedIds.contains(assignment.getLearner().getId()))
                .toList();

        List<TrainingAssignment> added = selectedLearners.stream()
                .filter(learner -> !existingLearnerIds.contains(learner.getId()))
                .map(learner -> new TrainingAssignment(training, learner))
                .toList();

        assignmentRepository.deleteAll(removed);
        assignmentRepository.saveAll(added);
    }

    @Transactional
    public void deleteTraining(
            @NotNull @Positive Long actorId,
            @NotNull @Positive Long trainingId
    ) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Acting user does not exist"
                ));

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Training does not exist"
                ));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isLearnerCreator =
                actor.getRole() == Role.LEARNER
                        && training.getCreatedBy().getId().equals(actor.getId());

        if (!isAdmin && !isLearnerCreator) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete this training"
            );
        }

        List<TrainingAssignment> assignments =
                assignmentRepository.findAllByTrainingId(trainingId);

        if (!isAdmin && !assignments.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assigned trainings can only be deleted by an admin"
            );
        }

        assignmentRepository.deleteAll(assignments);
        assignmentRepository.flush();

        trainingRepository.delete(training);
    }
}