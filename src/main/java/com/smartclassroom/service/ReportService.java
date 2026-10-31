package com.smartclassroom.service;

import com.smartclassroom.dao.AssignmentDAO;
import com.smartclassroom.dao.AttendanceDAO;
import com.smartclassroom.dao.ClassDAO;
import com.smartclassroom.dao.FacultyDAO;
import com.smartclassroom.dao.StudentDAO;
import com.smartclassroom.dao.SubjectDAO;
import com.smartclassroom.dao.SubmissionDAO;
import com.smartclassroom.model.Assignment;
import com.smartclassroom.model.Student;
import com.smartclassroom.model.Subject;
import com.smartclassroom.model.Submission;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates data from multiple DAOs and services to produce dashboard summaries
 * and cross-entity reports.
 *
 * <p>This class serves three roles:
 * <ul>
 *   <li><strong>Admin Dashboard</strong> — system-wide counts (students, faculty, classes, subjects).</li>
 *   <li><strong>Faculty Dashboard</strong> — assigned subjects, upcoming assignment deadlines,
 *       pending submission counts.</li>
 *   <li><strong>Student Dashboard</strong> — per-subject attendance percentages, enrolled subjects.</li>
 * </ul>
 * </p>
 *
 * <p>DSA used in this class:
 * <ul>
 *   <li>{@link HashMap} — intermediate accumulation before sorting.</li>
 *   <li>{@link LinkedHashMap} — ordered output map for subject attendance.</li>
 *   <li>{@link ArrayList} + filter loop — upcoming assignment selection.</li>
 *   <li>{@link Comparator} — sorting assignments and subjects by name/deadline.</li>
 * </ul>
 * </p>
 */
public class ReportService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AttendanceService attendanceService = new AttendanceService();

    // =========================================================================
    // Admin Dashboard — System-wide Counts
    // =========================================================================

    /**
     * Returns the total number of student records in the system.
     *
     * @return total student count
     */
    public int getTotalStudents() {
        return studentDAO.count();
    }

    /**
     * Returns the total number of faculty records in the system.
     *
     * @return total faculty count
     */
    public int getTotalFaculty() {
        return facultyDAO.count();
    }

    /**
     * Returns the total number of class records in the system.
     *
     * @return total class count
     */
    public int getTotalClasses() {
        return classDAO.count();
    }

    /**
     * Returns the total number of subject records in the system.
     *
     * @return total subject count
     */
    public int getTotalSubjects() {
        return subjectDAO.count();
    }

    // =========================================================================
    // Faculty Dashboard
    // =========================================================================

    /**
     * Returns all subjects currently assigned to a faculty member.
     *
     * @param facultyId the faculty's ID
     * @return list of {@link Subject} objects assigned to the faculty
     */
    public List<Subject> getFacultySubjects(int facultyId) {
        return subjectDAO.findByFacultyId(facultyId);
    }

    /**
     * Returns assignments created by a faculty member whose deadlines are
     * in the future, sorted ascending by deadline (soonest first).
     *
     * <p><strong>DSA:</strong> An {@link ArrayList} is built via a loop filter
     * (O(n)), then sorted with a {@link Comparator} on deadline.</p>
     *
     * @param facultyId the faculty's ID
     * @return sorted list of upcoming {@link Assignment} objects
     */
    public List<Assignment> getUpcomingAssignments(int facultyId) {
        List<Assignment> all = assignmentDAO.findByFacultyId(facultyId);
        List<Assignment> upcoming = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Assignment a : all) {
            if (a.getDeadline().isAfter(now)) {
                upcoming.add(a);
            }
        }
        // Sort ascending by deadline — soonest deadline first
        upcoming.sort(Comparator.comparing(Assignment::getDeadline));
        return upcoming;
    }

    /**
     * Returns the number of submissions for an assignment that have not yet
     * been evaluated (i.e., their evaluation status is {@code SUBMITTED}).
     *
     * @param assignmentId the assignment's ID
     * @return count of pending (ungraded) submissions
     */
    public long getPendingSubmissionsCount(int assignmentId) {
        List<Submission> submissions = submissionDAO.findByAssignmentId(assignmentId);
        return submissions.stream()
                .filter(s -> s.getEvaluationStatus() == Submission.EvaluationStatus.SUBMITTED)
                .count();
    }

    // =========================================================================
    // Student Dashboard
    // =========================================================================

    /**
     * Returns a map of {@link Subject} to attendance percentage for a student,
     * covering all subjects in the student's enrolled class.
     *
     * <p><strong>DSA:</strong>
     * <ul>
     *   <li>A {@link HashMap}&lt;Integer, Double&gt; (keyed by subjectId) accumulates
     *       raw percentage values in O(n).</li>
     *   <li>Subjects are then sorted by name using a {@link Comparator}.</li>
     *   <li>A {@link LinkedHashMap} is returned, preserving the sorted insertion order
     *       so the UI can iterate predictably without re-sorting.</li>
     * </ul>
     * </p>
     *
     * @param studentId the student's ID
     * @return a {@link LinkedHashMap} of {@link Subject} → attendance percentage,
     *         sorted by subject name (case-insensitive); empty map if student not found
     */
    public Map<Subject, Double> getStudentAttendanceSummary(int studentId) {
        // Find the student to get their classId
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            return new LinkedHashMap<>();
        }

        // Get all subjects for the student's class
        List<Subject> subjects = subjectDAO.findByClassId(student.getClassId());

        // DSA: HashMap — accumulate subject attendance percentages
        Map<Integer, Double> rawMap = new HashMap<>();
        for (Subject subject : subjects) {
            int[] summary = attendanceDAO.getAttendanceSummary(studentId, subject.getSubjectId());
            double pct = (summary[1] == 0)
                    ? 0.0
                    : (double) summary[0] / summary[1] * 100.0;
            rawMap.put(subject.getSubjectId(), pct);
        }

        // Sort subjects alphabetically by subject name
        subjects.sort(Comparator.comparing(Subject::getSubjectName, String.CASE_INSENSITIVE_ORDER));

        // DSA: LinkedHashMap — maintain sorted order for the caller
        Map<Subject, Double> result = new LinkedHashMap<>();
        for (Subject subject : subjects) {
            result.put(subject, rawMap.get(subject.getSubjectId()));
        }
        return result;
    }

    /**
     * Delegates to {@link AttendanceService#getLowAttendanceReport(int, int, double)}
     * to produce a list of students in a class whose aggregate attendance is below
     * the given threshold.
     *
     * @param classId   the class to analyze
     * @param facultyId the faculty whose subjects are considered
     * @param threshold percentage below which attendance is flagged (e.g., 75.0)
     * @return sorted list of {@link AttendanceService.StudentAttendanceSummary} (ascending %)
     */
    public List<AttendanceService.StudentAttendanceSummary> getLowAttendanceReport(
            int classId, int facultyId, double threshold) {
        return attendanceService.getLowAttendanceReport(classId, facultyId, threshold);
    }

    /**
     * Returns the number of assignments associated with a given subject.
     *
     * @param subjectId the subject's ID
     * @return count of assignments for the subject
     */
    public int getAssignmentCount(int subjectId) {
        return assignmentDAO.findBySubjectId(subjectId).size();
    }

    /**
     * Returns all subjects in which a student is enrolled (via their class).
     *
     * @param studentId the student's ID
     * @return list of {@link Subject} objects for the student's class;
     *         empty list if the student is not found
     */
    public List<Subject> getStudentSubjects(int studentId) {
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            return new ArrayList<>();
        }
        return subjectDAO.findByClassId(student.getClassId());
    }
}
