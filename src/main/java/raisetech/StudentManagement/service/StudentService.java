package raisetech.StudentManagement.service;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagement.controller.converter.StudentConverter;
import raisetech.StudentManagement.data.ApplicationStatus;
import raisetech.StudentManagement.data.ApplicationStatusType;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.*;
import raisetech.StudentManagement.repository.StudentRepository;


import raisetech.StudentManagement.exception.InvalidApplicationException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 受講生情報の検索、登録、更新、削除、復元を行うサービスクラスです。
 */
@Service
public class StudentService {

    private final StudentRepository repository;
    private final StudentConverter converter;

    @Autowired
    public StudentService(StudentRepository repository, StudentConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }

    /**
     * 受講生詳細の一覧検索を行います。
     * 全件検索を行うので、条件指定は行いません。
     *
     * @return 受講生詳細一覧(全件)
     */
    public List<StudentDetail> searchStudentList() {
        List<Student> studentList = repository.search();
        List<StudentCourse> studentCourseList = repository.searchStudentCourseList();
        return converter.convertStudentDetails(studentList, studentCourseList);
    }

    /**
     * 指定したIDに紐づく受講生詳細を取得します。
     * 受講生情報を取得した後、その受講生に紐づく受講生コース情報を設定します。
     *
     * @param id 受講生ID
     * @return 受講生詳細
     * @throws StudentNotFoundException 指定したIDの受講生が存在しない場合
     */
    public StudentDetail searchStudent(String id) {
        Student student = repository.searchStudent(id);
        if (student == null) {
            throw new StudentNotFoundException(id);
        }
        List<StudentCourse> studentCourse = repository.searchStudentCourse(student.getId());
        return new StudentDetail(student, studentCourse);
    }


    /**
     * 受講生詳細の登録を行います。
     * 受講生情報と受講生コース情報をそれぞれ登録し、
     * 受講生コース情報には受講生情報に紐づく値やコース開始日、コース終了日を設定します。
     *
     * @param studentDetail 受講生詳細
     * @throws DuplicateEmailException メールアドレスが既に登録されている場合
     */
    @Transactional
    public void registerStudent(StudentDetail studentDetail) {
        Student student = studentDetail.getStudent();

        if (repository.countByEmail(student.getEmail()) > 0) {
            throw new DuplicateEmailException(student.getEmail() + " はすでに使われているメールアドレスです");
        }
        repository.registerStudent(student);

        studentDetail.getStudentCourseList().forEach(studentCourse -> {
            initStudentsCourse(studentCourse, student);
            repository.registerStudentCourse(studentCourse);
        });
    }

    /**
     * 受講生コース情報の初期情報を設定します
     * 受講生ID、コース開始日、コース終了日を設定します
     *
     * @param studentCourse 受講生コース情報
     * @param student       受講生
     */
    private static void initStudentsCourse(StudentCourse studentCourse, Student student) {
        LocalDateTime now = LocalDateTime.now();

        studentCourse.setStudentId(student.getId());
        studentCourse.setCourseStartAt(now);
        studentCourse.setCourseEndAt(now.plusYears(1));
    }

    /**
     * 受講生情報の更新を行います。
     * 受講生の情報と受講生コース情報をそれぞれ更新します。
     *
     * @param studentDetail 受講生詳細
     */
    @Transactional
    public void updateStudent(StudentDetail studentDetail) {
        Student foundStudent = repository.searchStudent(studentDetail.getStudent().getId());
        if (foundStudent == null) {
            throw new StudentNotFoundException(studentDetail.getStudent().getId());
        }
        repository.updateStudent(studentDetail.getStudent());
        studentDetail.getStudentCourseList().forEach(studentCourse -> {
            studentCourse.setStudentId(studentDetail.getStudent().getId());
            repository.updateStudentCourse(studentCourse);
        });
    }

    /**
     * 指定した受講生IDの論理削除を行います
     *
     * @param id 受講生ID
     * @throws StudentNotFoundException       指定したIDの受講生が存在しない場合
     * @throws StudentAlreadyDeletedException 指定したIDの受講生が論理削除されている場合
     */
    public void deleteStudent(String id) {
        Student student = repository.searchStudent(id);

        if (student == null) {
            throw new StudentNotFoundException(id);
        }
        if (student.isDeleted()) {
            throw new StudentAlreadyDeletedException(id);
        }
        repository.deleteStudent(id);
    }

    /**
     * 指定した受講生IDの復元処理を行います
     *
     * @param id 受講生ID
     * @throws StudentNotFoundException      指定したIDの受講生が存在しない場合
     * @throws StudentAlreadyActiveException 指定したIDの受講生が有効状態の場合
     */
    public void restoreStudent(String id) {
        Student student = repository.searchStudent(id);
        if (student == null) {
            throw new StudentNotFoundException(id);
        }
        if (!student.isDeleted()) {
            throw new StudentAlreadyActiveException(id);
        }
        repository.restoreStudent(id);
    }

    /**
     * 受講生コース情報IDに基づいて申込状況を新規作成します
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException 指定した受講生コース情報IDが存在しない場合
     * @throws StudentAlreadyAppliedException すでに申し込まれている場合
     */
    @Transactional
    public void createApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus existing =
                repository.searchApplicationStatusByStudentCourseId(studentCourseId);
        if (existing != null) {
            throw new StudentAlreadyAppliedException(studentCourseId);
        }
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        repository.createApplicationStatus(applicationStatus);
    }

    /**
     * 受講生コース情報に基づいて申込状況を確定します
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException     指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException 申込状況がない場合
     * @throws InvalidApplicationException        仮申込状態以外のステータスである場合
     */
    @Transactional
    public void confirmApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus = findApplicationStatusOrThrow(studentCourseId);
        ApplicationStatusType statusType = ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.canConfirm()) {
            throw new InvalidApplicationException("仮申込状態のみ本申込に変更できます。");
        }
        applicationStatus.setStatus(ApplicationStatusType.CONFIRMED.getLabel());

        repository.updateApplicationStatus(applicationStatus);
    }

    @Nonnull
    private ApplicationStatus findApplicationStatusOrThrow(String studentCourseId) {
        ApplicationStatus applicationStatus =
                repository.searchApplicationStatusByStudentCourseId(studentCourseId);
        if (applicationStatus == null) {
            throw new ApplicationStatusNotFoundException(studentCourseId);
        }
        return applicationStatus;
    }

    private void validateStudentCourseExists(String studentCourseId) {
        StudentCourse studentCourse = repository.searchStudentCourseById(studentCourseId);
        if (studentCourse == null) {
            throw new StudentCourseNotFoundException(studentCourseId);
        }
    }

    /**
     * 受講生コース情報に基づいて受講開始処理を行います
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException     指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException 申込状況がない場合
     * @throws InvalidApplicationException        本申込状態以外のステータスである場合
     */
    @Transactional
    public void startApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus = findApplicationStatusOrThrow(studentCourseId);
        ApplicationStatusType statusType = ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.canStart()) {
            throw new InvalidApplicationException("本申込状態のみ受講開始できます。");
        }
        applicationStatus.setStatus(ApplicationStatusType.ACTIVE.getLabel());
        repository.updateApplicationStatus(applicationStatus);
    }

    /**
     * 受講生コース情報に基づいて受講完了処理を行います
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException     指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException 申込状況がない場合
     * @throws InvalidApplicationException        受講中以外のステータスである場合
     */
    @Transactional
    public void completeApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus = findApplicationStatusOrThrow(studentCourseId);
        ApplicationStatusType statusType = ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.canComplete()) {
            throw new InvalidApplicationException("受講中のみ受講完了にできます。");
        }
        applicationStatus.setStatus(ApplicationStatusType.COMPLETED.getLabel());
        repository.updateApplicationStatus(applicationStatus);
    }

    /**
     /**
     * 仮申込・本申込のキャンセルを行う。
     * キャンセルされた申込状況は履歴として保持され、復元はできない。
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException 指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException 申込状況がない場合
     * @throws InvalidApplicationException 受講中・受講完了のステータスである場合
     */
    @Transactional
    public void cancelApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus = findApplicationStatusOrThrow(studentCourseId);
        if (applicationStatus.isDeleted()) {
            throw new ApplicationStatusAlreadyDeletedException(studentCourseId);
        }
        ApplicationStatusType statusType =
                ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.isCancelable()) {
            throw new InvalidApplicationException("この申込状況はキャンセルできません。");
        }
        repository.logicalDeleteApplicationStatus(applicationStatus);
    }
    /**
     * 受講完了済みの申込状況を論理削除（非表示化）する。
     * 業務上は履歴整理のための処理であり、キャンセルとは異なる。
     * 受講完了の場合のみ論理削除可能とし、それ以外の状態の場合は例外をスローします。
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException           指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException       申込状況がない場合
     * @throws ApplicationStatusAlreadyDeletedException すでに論理削除されている場合
     * @throws InvalidApplicationException              完了以外のステータスである場合
     */
    @Transactional
    public void archiveCompletedApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus = findApplicationStatusOrThrow(studentCourseId);
        if (applicationStatus.isDeleted()) {
            throw new ApplicationStatusAlreadyDeletedException(studentCourseId);
        }
        ApplicationStatusType statusType = ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.canArchive()) {
            throw new InvalidApplicationException("受講完了状態のみ非表示化できます。");
        }
        repository.logicalDeleteApplicationStatus(applicationStatus);
    }
}






