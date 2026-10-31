package com.smartclassroom.service;

import com.smartclassroom.dao.AttendanceDAO;
import com.smartclassroom.dao.StudentDAO;
import com.smartclassroom.dao.SubjectDAO;
import com.smartclassroom.model.Attendance;
import com.smartclassroom.model.Student;
import com.smartclassroom.model.Subject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service class for all attendance-related business logic.
 *
 * <p><strong>Primary DSA Demonstration Class.</strong> This class explicitly
 * employs the following data structures and algorithms:
 * <ul>
 *   <li>{@link HashSet} — O(1) duplicate-detection during batch attendance submission.</li>
 *   <li>{@link HashMap} — aggregate attendance counts keyed by studentId.</li>
 *   <li>{@link LinkedHashMap} — insertion-ordered map for subject attendance reports.</li>
 *   <li>{@link ArrayList} — mutable result lists throughout.</li>
 *   <li>{@link Comparator} — sorting low-attendance report by ascending percentage.</li>
 * </ul>
 * </p>
 *
 * <p>All database access is delegated to {@link AttendanceDAO}, {@link StudentDAO},
 * and {@link SubjectDAO}. No SQL appears in this class.</p>
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    // -----------------------------------------------------------------------
    // Inner class — DSA: encapsulates per-student summary result
    // -----------------------------------------------------------------------

    /**
     * Immutable value object holding attendance summary for one student.
     *
     * <p>Used by {@link #getLowAttendanceReport(int, int, double)} to return
     * per-student aggregate data without exposing raw arrays.</p>
     */
    public static class StudentAttendanceSummary {

        private final Student student;
        private final int presentCount;
        private final int totalCount;
        private final double percentage;

        /**
         * Constructs a new {@code StudentAttendanceSummary}.
         *
         * @param student      the student this summary belongs to
         * @param presentCount number of sessions the student was present
         * @param totalCount   total number of sessions held
         * @param percentage   computed attendance percentage (0–100)
         */
        public StudentAttendanceSummary(Student student, int presentCount,
                                        int totalCount, double percentage) {
            this.student = student;
            this.presentCount = presentCount;
            this.totalCount = totalCount;
            this.percentage = percentage;
        }

        /** @return the student entity */
        public Student getStudent() { return student; }

        /** @return number of sessions the student was marked PRESENT */
        public int getPresentCount() { return presentCount; }

        /** @return total sessions conducted across all relevant subjects */
        public int getTotalCount() { return totalCount; }

        /** @return attendance percentage (presentCount / totalCount × 100) */
        public double getPercentage() { return percentage; }
    }

    // -----------------------------------------------------------------------
    // Mark Attendance
    // -----------------------------------------------------------------------

    /**
     * Marks (or updates) attendance for an entire class for a given subject and date.
     *
     * <p>Business rules enforced:
     * <ol>
     *   <li>The attendance list must not be null or empty.</li>
     *   <li>The subject must belong to the requesting faculty (authorization check).</li>
     *   <li>The input list must contain no duplicate {@code studentId} entries — detected
     *       in O(n) using a {@link HashSet}.</li>
     * </ol>
     * </p>
     *
     * <p><strong>DSA:</strong> {@link HashSet} is used for O(1) average-case membership
     * testing to detect duplicate student entries in the submitted list.</p>
     *
     * @param records   list of {@link Attendance} records (studentId, subjectId, date, status)
     * @param facultyId the ID of the faculty marking attendance (for authorization)
     * @throws IllegalArgumentException if records list is null/empty or contains duplicates
     * @throws RuntimeException         if the faculty is not authorized to manage this subject
     */
    public void markAttendance(List<Attendance> records, int facultyId) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("Attendance records cannot be empty.");
        }

        // Authorization: verify the subject belongs to this faculty
        int subjectId = records.get(0).getSubjectId();
        Subject subject = subjectDAO.findById(subjectId);
        if (subject == null || subject.getFacultyId() != facultyId) {
            throw new RuntimeException("You are not authorized to manage this subject.");
        }

        // DSA — HashSet: O(1) duplicate detection per entry → O(n) overall
        Set<Integer> seen = new HashSet<>();
        for (Attendance a : records) {
            if (!seen.add(a.getStudentId())) {
                throw new IllegalArgumentException(
                        "Duplicate student entry in attendance list for studentId: "
                        + a.getStudentId());
            }
        }

        attendanceDAO.saveBatch(records);
    }

    // -----------------------------------------------------------------------
    // Percentage Calculation
    // -----------------------------------------------------------------------

    /**
     * Calculates the attendance percentage for a student in a specific subject.
     *
     * <p>Returns {@code 0.0} if no sessions have been recorded, preventing
     * division-by-zero errors.</p>
     *
     * @param studentId the student's ID
     * @param subjectId the subject's ID
     * @return attendance percentage in the range [0.0, 100.0]
     */
    public double calculatePercentage(int studentId, int subjectId) {
        int[] summary = attendanceDAO.getAttendanceSummary(studentId, subjectId);
        int present = summary[0];
        int total = summary[1];
        if (total == 0) {
            return 0.0;
        }
        return (double) present / total * 100.0;
    }

    // -----------------------------------------------------------------------
    // Low Attendance Report — Primary DSA Demonstration
    // -----------------------------------------------------------------------

    /**
     * Generates a low-attendance report for a class, listing only students whose
     * aggregate attendance across all subjects taught by the given faculty falls
     * below the specified threshold.
     *
     * <p><strong>Algorithm &amp; DSA used:</strong>
     * <ol>
     *   <li><strong>List&lt;Student&gt;</strong> — retrieve all students in the class.</li>
     *   <li><strong>List&lt;Subject&gt; + loop filter</strong> — narrow subjects to those
     *       taught by {@code facultyId} in {@code classId}.</li>
     *   <li><strong>HashMap&lt;Integer, int[]&gt;</strong> — keyed by studentId, values are
     *       {@code int[]{presentTotal, sessionTotal}} accumulated across all subjects.
     *       Enables O(1) updates per (student, subject) pair.</li>
     *   <li><strong>ArrayList + loop filter</strong> — collect students below threshold.</li>
     *   <li><strong>Comparator + List.sort</strong> — sort result by ascending percentage.</li>
     * </ol>
     * </p>
     *
     * @param classId   the class to analyze
     * @param facultyId used to filter only this faculty's subjects
     * @param threshold students with attendance below this percentage are included (e.g., 75.0)
     * @return a list of {@link StudentAttendanceSummary} sorted by ascending attendance %
     */
    public List<StudentAttendanceSummary> getLowAttendanceReport(
            int classId, int facultyId, double threshold) {

        // Step 1 — retrieve all students in the class (List<Student>)
        List<Student> students = studentDAO.findByClassId(classId);

        // Step 2 — filter subjects: only those taught by this faculty in this class
        List<Subject> allSubjects = subjectDAO.findByFacultyId(facultyId);
        List<Subject> classSubjects = new ArrayList<>();
        for (Subject s : allSubjects) {
            if (s.getClassId() == classId) {
                classSubjects.add(s);
            }
        }

        // Step 3 — DSA: HashMap<studentId, int[]{present, total}>
        // Initialize every student entry with zeroes
        Map<Integer, int[]> attendanceMap = new HashMap<>();
        for (Student student : students) {
            attendanceMap.put(student.getStudentId(), new int[]{0, 0});
        }

        // Aggregate across all relevant subjects
        for (Subject subject : classSubjects) {
            for (Student student : students) {
                int[] summary = attendanceDAO.getAttendanceSummary(
                        student.getStudentId(), subject.getSubjectId());
                int[] current = attendanceMap.get(student.getStudentId());
                current[0] += summary[0]; // accumulate present count
                current[1] += summary[1]; // accumulate total sessions
            }
        }

        // Step 4 & 5 — compute percentage, filter below threshold
        List<StudentAttendanceSummary> lowList = new ArrayList<>();
        for (Student student : students) {
            int[] data = attendanceMap.get(student.getStudentId());
            double pct = (data[1] == 0)
                    ? 0.0
                    : (double) data[0] / data[1] * 100.0;
            if (pct < threshold) {
                lowList.add(new StudentAttendanceSummary(student, data[0], data[1], pct));
            }
        }

        // Step 6 — DSA: Comparator sort by ascending percentage
        lowList.sort(Comparator.comparingDouble(StudentAttendanceSummary::getPercentage));

        return lowList;
    }

    // -----------------------------------------------------------------------
    // Subject Attendance Report
    // -----------------------------------------------------------------------

    /**
     * Returns a per-student attendance percentage map for a given subject.
     *
     * <p><strong>DSA:</strong>
     * <ul>
     *   <li>A {@link HashMap} accumulates raw results (studentId → percentage).</li>
     *   <li>Results are inserted into a {@link LinkedHashMap} sorted by student full name,
     *       preserving insertion order for display.</li>
     * </ul>
     * </p>
     *
     * @param subjectId the subject to report on
     * @return a {@link LinkedHashMap} of {@link Student} → attendance percentage,
     *         sorted by student full name (case-insensitive)
     */
    public Map<Student, Double> getSubjectAttendanceReport(int subjectId) {
        // Look up the subject to find its classId
        Subject subject = subjectDAO.findById(subjectId);
        if (subject == null) {
            return new LinkedHashMap<>();
        }

        // Get all students for the subject's class
        List<Student> students = studentDAO.findByClassId(subject.getClassId());

        // DSA: HashMap — build intermediate map for fast studentId → % lookup
        Map<Integer, Double> rawMap = new HashMap<>();
        for (Student student : students) {
            int[] summary = attendanceDAO.getAttendanceSummary(
                    student.getStudentId(), subjectId);
            double pct = (summary[1] == 0)
                    ? 0.0
                    : (double) summary[0] / summary[1] * 100.0;
            rawMap.put(student.getStudentId(), pct);
        }

        // Sort students by full name (case-insensitive)
        students.sort(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER));

        // DSA: LinkedHashMap — preserves sorted insertion order for the caller
        Map<Student, Double> result = new LinkedHashMap<>();
        for (Student student : students) {
            result.put(student, rawMap.get(student.getStudentId()));
        }
        return result;
    }

    /**
     * Returns a list of {@link StudentAttendanceSummary} objects for a given subject.
     * Contains present count, total count, and attendance percentage for each student.
     *
     * @param subjectId the subject ID
     * @return sorted list of summaries by student name
     */
    public List<StudentAttendanceSummary> getSubjectAttendanceSummaryList(int subjectId) {
        Subject subject = subjectDAO.findById(subjectId);
        if (subject == null) {
            return new ArrayList<>();
        }
        List<Student> students = studentDAO.findByClassId(subject.getClassId());
        List<StudentAttendanceSummary> list = new ArrayList<>();
        for (Student student : students) {
            int[] summary = attendanceDAO.getAttendanceSummary(student.getStudentId(), subjectId);
            double pct = (summary[1] == 0) ? 0.0 : (double) summary[0] / summary[1] * 100.0;
            list.add(new StudentAttendanceSummary(student, summary[0], summary[1], pct));
        }
        list.sort(Comparator.comparing(s -> s.getStudent().getFullName(), String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    // -----------------------------------------------------------------------
    // Simple Query Delegates
    // -----------------------------------------------------------------------

    /**
     * Returns all attendance records for a specific student across all subjects.
     *
     * @param studentId the student's ID
     * @return list of {@link Attendance} records
     */
    public List<Attendance> getStudentAttendance(int studentId) {
        return attendanceDAO.findByStudentId(studentId);
    }

    /**
     * Returns attendance records for a given subject on a specific date.
     * Useful for reviewing or editing previously submitted attendance.
     *
     * @param subjectId the subject's ID
     * @param date      the date to retrieve records for
     * @return list of {@link Attendance} records for that date
     */
    public List<Attendance> getAttendanceForDate(int subjectId, LocalDate date) {
        return attendanceDAO.findBySubjectAndDate(subjectId, date);
    }

    /**
     * Returns attendance records for a specific student in a specific subject.
     *
     * @param subjectId the subject's ID
     * @param date      the date to filter by
     * @return list of {@link Attendance} records
     */
    public List<Attendance> getAttendanceBySubjectAndDate(int subjectId, LocalDate date) {
        return attendanceDAO.findBySubjectAndDate(subjectId, date);
    }
}
