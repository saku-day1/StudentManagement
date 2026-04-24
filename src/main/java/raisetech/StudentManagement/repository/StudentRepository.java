package raisetech.StudentManagement.repository;

import org.apache.ibatis.annotations.*;
import raisetech.StudentManagement.data.ApplicationStatus;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;

import java.util.List;

/**
 * 受講生テーブルと受講生コース情報テーブルと紐づくRepositoryです。
 *
 */

@Mapper
public interface StudentRepository {
    /**
     * 受講生の全件検索を行います。
     *
     * @return 受講生情報(論理削除されたものを除く)
     */
    List<Student> search();

    /**
     * 受講生の検索を行います。
     *
     * @param id 受講生ID
     * @return 受講生
     */
    Student searchStudent(String id);

    /**
     * 受講生のコース情報の全件検索を行います。
     *
     * @return 全件検索したコース情報の一覧。
     */
    List<StudentCourse> searchStudentCourseList();

    /**
     * 受講生IDに基づく受講生コース情報を検索します。
     *
     * @param studentId 受講生ID
     * @return 受講生IDに紐づく受講生コース情報。
     */
    List<StudentCourse> searchStudentCourse(String studentId);

    /**
     * 受講生を新規登録します。
     * IDに関しては自動採番を行う。
     *
     * @param student 受講生
     */
    void registerStudent(Student student);

    /**
     * 受講生コース情報を新規取得します。
     * IDに関しては自動採番を行う。
     *
     * @param studentCourse 受講生コース情報
     */
    void registerStudentCourse(StudentCourse studentCourse);

    /**
     * 受講生を更新します。
     *
     * @param student 受講生
     */
    void updateStudent(Student student);

    /**
     * 受講生コース情報のコース名を更新します。
     *
     * @param studentCourse 受講生コース
     */
    void updateStudentCourse(StudentCourse studentCourse);

    /**
     * 受講生の論理削除を行います。
     * すでに削除されている場合は選択できません。
     *
     * @param id 受講生ID
     */
    void deleteStudent(String id);

    /**
     * 削除された受講生の復元を行います。
     *
     * @param id 受講生ID
     */
    void restoreStudent(String id);

    /**
     * 指定したメールアドレスが既に登録されているかを確認します。
     *
     * @param email 受講生メールアドレス
     * @return 該当するメールアドレスの件数
     */
    int countByEmail(String email);

    /**
     * 受講生コースIDに基づいて受講生コース情報を検索します。
     *
     * @param studentCourseId 受講生コースID
     * @return 受講生コース情報
     */
    StudentCourse searchStudentCourseById(String studentCourseId);

    /**
     * 受講生コースIDに基づいて申込状況を検索します。
     * @param studentCourseId 受講生コースID
     * @return 申込状況
     */
    ApplicationStatus searchApplicationStatusByStudentCourseId(String studentCourseId);

    /**
     * 仮申込状況を作成します。
     * @param applicationStatus 申込状況
     */
    void createApplicationStatus(ApplicationStatus applicationStatus);

    /**
     * 現在の申込状況を更新します。
     * @param applicationStatus 申込状況
     */
    void updateApplicationStatus(ApplicationStatus applicationStatus);

    /**
     * 完了済みの申込状況を論理削除します。
     * @param applicationStatus 申込状況
     */
    void logicalDeleteApplicationStatus(ApplicationStatus applicationStatus);

    /**
     * 論理削除した申込状況を復元します。
     * @param applicationStatus 申込状況
     */
    void logicalRestoreApplicationStatus(ApplicationStatus applicationStatus);
}



