package com.smartclassroom.service;

import com.smartclassroom.dao.AssignmentDAO;
import com.smartclassroom.dao.SubjectDAO;
import com.smartclassroom.dao.SubmissionDAO;
import com.smartclassroom.model.Assignment;
import com.smartclassroom.model.Subject;
import com.smartclassroom.model.Submission;
import com.smartclassroom.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Service class encapsulating all business logic for assignments and submissions.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate assignment data (title, deadline, marks) before persistence.</li>
 *   <li>Enforce authorization — a faculty member may only manage their own subjects' assignments.</li>
 *   <li>Derive the current status of an assignment for a student (PENDING, SUBMITTED,
 *       OVERDUE, LATE_SUBMITTED, EVALUATED).</li>
 *   <li>Handle file-path validation for submissions.</li>
 *   <li>Enforce evaluation mark range constraints.</li>
 * </ul>
 * </p>
 *
 * <p>All database access is delegated to {@link AssignmentDAO}, {@link SubmissionDAO},
 * and {@link SubjectDAO}. No SQL appears in this class.</p>
 */
public class AssignmentService {

    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    // -----------------------------------------------------------------------
    // Create / Update / Delete Assignment
    // -----------------------------------------------------------------------

    /**
     * Creates a new assignment after validating all business rules.
     *
     * <p>Validations:
     * <ul>
     *   <li>Title must not be blank.</li>
     *   <li>Deadline must be in the future.</li>
     *   <li>Max marks must be greater than 0.</li>
     *   <li>The subject must be assigned to the requesting faculty.</li>
     * </ul>
     * </p>
     *
     * @param assignment the {@link Assignment} to create (subjectId must be set)
     * @param facultyId  the ID of the faculty creating the assignment (for authorization)
     * @return the generated {@code assignment_id}
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException         if the faculty is not authorized to manage the subject
     */
    public int createAssignment(Assignment assignment, int facultyId) {
        // Validate title
        if (assignment.getTitle() == null || assignment.getTitle().isBlank()) {
            throw new IllegalArgumentException("Assignment title cannot be empty.");
        }

        // Validate deadline
        if (assignment.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline cannot be null.");
        }
        if (!assignment.getDeadline().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline must be a future date and time.");
        }

        // Validate max marks
        if (assignment.getMaxMarks() <= 0) {
            throw new IllegalArgumentException("Maximum marks must be greater than 0.");
        }

        // Authorization: subject must belong to this faculty
        Subject subject = subjectDAO.findById(assignment.getSubjectId());
        if (subject == null) {
            throw new RuntimeException("Subject not found for id: " + assignment.getSubjectId());
        }
        if (subject.getFacultyId() != facultyId) {
            throw new RuntimeException("You are not authorized to create assignments for this subject.");
        }

        // Set metadata
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setFacultyId(facultyId);

        return assignmentDAO.save(assignment);
    }

    /**
     * Updates an existing assignment after re-validating all business rules.
     *
     * @param assignment the {@link Assignment} with updated values (must have valid assignmentId)
     * @param facultyId  the ID of the faculty requesting the update (for authorization)
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException         if the faculty is not authorized to manage the subject
     */
    public void updateAssignment(Assignment assignment, int facultyId) {
        // Validate title
        if (assignment.getTitle() == null || assignment.getTitle().isBlank()) {
            throw new IllegalArgumentException("Assignment title cannot be empty.");
        }

        // Validate deadline
        if (assignment.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline cannot be null.");
        }
        if (!assignment.getDeadline().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline must be a future date and time.");
        }

        // Validate max marks
        if (assignment.getMaxMarks() <= 0) {
            throw new IllegalArgumentException("Maximum marks must be greater than 0.");
        }

        // Authorization: subject must belong to this faculty
        Subject subject = subjectDAO.findById(assignment.getSubjectId());
        if (subject == null) {
            throw new RuntimeException("Subject not found for id: " + assignment.getSubjectId());
        }
        if (subject.getFacultyId() != facultyId) {
            throw new RuntimeException("You are not authorized to update assignments for this subject.");
        }

        assignmentDAO.update(assignment);
    }

    /**
     * Deletes an assignment, enforcing that the requesting faculty owns the subject.
     *
     * @param assignmentId the ID of the assignment to delete
     * @param facultyId    the ID of the requesting faculty
     * @throws RuntimeException if the assignment is not found or the faculty is not authorized
     */
    public void deleteAssignment(int assignmentId, int facultyId) {
        Assignment assignment = assignmentDAO.findById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("Assignment not found for id: " + assignmentId);
        }
        Subject subject = subjectDAO.findById(assignment.getSubjectId());
        if (subject == null || subject.getFacultyId() != facultyId) {
            throw new RuntimeException("You are not authorized to delete this assignment.");
        }
        assignmentDAO.delete(assignmentId);
    }

    // -----------------------------------------------------------------------
    // Query Methods
    // -----------------------------------------------------------------------

    /**
     * Returns all assignments for a subject, sorted by deadline ascending.
     *
     * @param subjectId the subject ID
     * @return sorted list of {@link Assignment} objects
     */
    public List<Assignment> getAssignmentsBySubject(int subjectId) {
        List<Assignment> list = assignmentDAO.findBySubjectId(subjectId);
        list.sort(Comparator.comparing(Assignment::getDeadline));
        return list;
    }

    /**
     * Returns all assignments created by a faculty member, sorted by deadline ascending.
     *
     * @param facultyId the faculty ID
     * @return sorted list of {@link Assignment} objects
     */
    public List<Assignment> getAssignmentsByFaculty(int facultyId) {
        List<Assignment> list = assignmentDAO.findByFacultyId(facultyId);
        list.sort(Comparator.comparing(Assignment::getDeadline));
        return list;
    }

    /**
     * Returns all assignments visible to a student for a given subject, with
     * the derived {@link Assignment.AssignmentStatus} computed for each.
     *
     * <p>For each assignment, the method checks whether a submission exists and
     * calls {@link #deriveStatus(Assignment, Submission)} to set the transient status.</p>
     *
     * @param studentId the student's ID
     * @param subjectId the subject's ID
     * @return list of {@link Assignment} objects with {@code derivedStatus} populated,
     *         sorted by deadline ascending
     */
    public List<Assignment> getAssignmentsForStudent(int studentId, int subjectId) {
        List<Assignment> assignments = assignmentDAO.findBySubjectId(subjectId);
        for (Assignment a : assignments) {
            Submission sub = submissionDAO.findByStudentAndAssignment(studentId, a.getAssignmentId());
            a.setDerivedStatus(deriveStatus(a, sub));
        }
        assignments.sort(Comparator.comparing(Assignment::getDeadline));
        return assignments;
    }

    // -----------------------------------------------------------------------
    // Status Derivation
    // -----------------------------------------------------------------------

    /**
     * Derives the current {@link Assignment.AssignmentStatus} for a student based on
     * submission data and deadline.
     *
     * <p>Decision logic (evaluated in order):
     * <ol>
     *   <li>If there is a submission and it has been {@code EVALUATED} → {@code EVALUATED}.</li>
     *   <li>If there is a submission and it was submitted <em>after</em> the deadline → {@code LATE_SUBMITTED}.</li>
     *   <li>If there is a submission (on time) → {@code SUBMITTED}.</li>
     *   <li>If there is no submission and the deadline has passed → {@code OVERDUE}.</li>
     *   <li>Otherwise → {@code PENDING}.</li>
     * </ol>
     * </p>
     *
     * @param a   the assignment (provides deadline and max marks)
     * @param sub the submission for this student, or {@code null} if not submitted
     * @return the derived {@link Assignment.AssignmentStatus}
     */
    public Assignment.AssignmentStatus deriveStatus(Assignment a, Submission sub) {
        if (sub != null) {
            if (sub.getEvaluationStatus() == Submission.EvaluationStatus.EVALUATED) {
                return Assignment.AssignmentStatus.EVALUATED;
            }
            if (sub.getSubmittedAt().isAfter(a.getDeadline())) {
                return Assignment.AssignmentStatus.LATE_SUBMITTED;
            }
            return Assignment.AssignmentStatus.SUBMITTED;
        }
        // No submission
        if (LocalDateTime.now().isAfter(a.getDeadline())) {
            return Assignment.AssignmentStatus.OVERDUE;
        }
        return Assignment.AssignmentStatus.PENDING;
    }

    // -----------------------------------------------------------------------
    // Submission Handling
    // -----------------------------------------------------------------------

    /**
     * Submits (or re-submits) an assignment for a student.
     *
     * <p>Business rules:
     * <ul>
     *   <li>File path must not be blank.</li>
     *   <li>File extension must be one of: {@code pdf, doc, docx, zip, txt, java}.</li>
     *   <li>If the student has already submitted <em>and</em> the deadline has passed,
     *       resubmission is not allowed.</li>
     * </ul>
     * </p>
     *
     * @param assignmentId the assignment to submit for
     * @param studentId    the student making the submission
     * @param filePath     the path to the uploaded file
     * @throws IllegalArgumentException if the file path is blank or has an invalid extension
     * @throws RuntimeException         if the assignment is not found or resubmission after deadline is attempted
     */
    public void submitAssignment(int assignmentId, int studentId, String filePath) {
        // Validate file path
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }

        // Validate file extension
        String[] allowedExtensions = {"pdf", "doc", "docx", "zip", "txt", "java"};
        if (!ValidationUtil.isValidFileExtension(filePath, allowedExtensions)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: pdf, doc, docx, zip, txt, java.");
        }

        // Fetch assignment
        Assignment assignment = assignmentDAO.findById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("Assignment not found for id: " + assignmentId);
        }

        // Check resubmission after deadline
        boolean alreadySubmitted = submissionDAO.existsByStudentAndAssignment(studentId, assignmentId);
        if (alreadySubmitted && LocalDateTime.now().isAfter(assignment.getDeadline())) {
            throw new RuntimeException("Deadline has passed. Resubmission is not allowed.");
        }

        // Build and persist submission
        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setFilePath(filePath);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setEvaluationStatus(Submission.EvaluationStatus.SUBMITTED);

        submissionDAO.save(submission);
    }

    /**
     * Records marks and feedback for a student's submission.
     *
     * <p>Business rules:
     * <ul>
     *   <li>Marks must be ≥ 0 and ≤ the assignment's maximum marks.</li>
     * </ul>
     * </p>
     *
     * @param submissionId the ID of the submission to evaluate
     * @param assignmentId the assignment this submission belongs to (used to look up max marks)
     * @param marks        the marks awarded
     * @param feedback     optional textual feedback for the student
     * @throws IllegalArgumentException if marks are out of range
     * @throws RuntimeException         if the assignment is not found
     */
    public void evaluateSubmission(int submissionId, int assignmentId,
                                   double marks, String feedback) {
        Assignment assignment = assignmentDAO.findById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("Assignment not found for id: " + assignmentId);
        }
        if (!ValidationUtil.isValidMarks(marks, assignment.getMaxMarks())) {
            throw new IllegalArgumentException(
                    "Marks " + marks + " are out of range. Maximum allowed: "
                    + assignment.getMaxMarks() + ".");
        }
        submissionDAO.updateEvaluation(submissionId, marks, feedback);
    }

    // -----------------------------------------------------------------------
    // Submission Queries
    // -----------------------------------------------------------------------

    /**
     * Returns all submissions for a given assignment.
     *
     * @param assignmentId the assignment ID
     * @return list of {@link Submission} objects
     */
    public List<Submission> getSubmissions(int assignmentId) {
        return submissionDAO.findByAssignmentId(assignmentId);
    }

    /**
     * Returns all submissions made by a specific student.
     *
     * @param studentId the student's ID
     * @return list of {@link Submission} objects
     */
    public List<Submission> getStudentSubmissions(int studentId) {
        return submissionDAO.findByStudentId(studentId);
    }
}
