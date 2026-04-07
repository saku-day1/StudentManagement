package raisetech.StudentManagement.repository;

import org.apache.ibatis.annotations.*;
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
     * 受講生の全件を行います。
     *
     * @return 受講生情報(論理削除されたものを除く)
     *
     */
    @Select("SELECT * FROM students WHERE is_deleted = false")
    List<Student> search();
    /**
     *
     * 受講生の検索を行います。
     * @param id　受講生ID
     * @return 受講生
     */

    @Select("SELECT * FROM students WHERE id = #{id} AND is_deleted = false")
    Student searchStudent(String id);

    /**
     * 受講生のコース情報の全件検索を行います。
     * @return 全件検索したコース情報の一覧。
     */
    @Select("SELECT * FROM student_courses")
    List<StudentCourse> searchStudentCourseList();

    /**
     * 受講生IDに基づく受講生コース情報を検索します。
     * @param studentId 受講生ID
     * @return 受講生IDに紐づく受講生コース情報。
     */

    @Select("SELECT * FROM student_courses WHERE student_id = #{studentId}")
    List<StudentCourse> searchStudentCourse(String studentId);

    /**
     * 受講生を新規登録します。
     * IDに関しては自動採番を行う。
     * @param student 受講生
     */

    @Insert("INSERT INTO students(name,furigana,nickname,email,area,age,gender,remarks,is_deleted) VALUES" +
            "(#{name},#{furigana},#{nickname},#{email},#{area},#{age},#{gender},#{remarks}, false)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void registerStudent(Student student);

    /**
     * 受講生コース情報を新規取得します。
     * IDに関しては自動採番を行う。
     * @param studentCourse 受講生コース情報
     */
    @Insert("INSERT INTO student_courses(student_id,course_name,course_start_at,course_end_at)" +
            "values(#{studentId},#{courseName},#{courseStartAt},#{courseEndAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void registerStudentCourse(StudentCourse studentCourse);

    /**
     * 受講生を更新します。
     * @param student　受講生
     */
    @Update("""
                UPDATE students
                SET name = #{name},
                    furigana = #{furigana},
                    nickname = #{nickname},
                    email = #{email},
                    area = #{area},
                    age = #{age},
                    gender = #{gender},
                    remarks = #{remarks},
                    is_deleted = #{deleted}
                WHERE id = #{id}
            """)
    void updateStudent(Student student);

    /**
     * 受講生コース情報のコース名を更新します。
     * @param studentCourse　受講生コース
     */
    @Update("""
                UPDATE student_courses
                SET course_name = #{courseName}
                WHERE id = #{id}
            """)
    void updateStudentCourse(StudentCourse studentCourse);

    /**
     * 受講生の論理削除を行います。
     * すでに削除されている場合は選択できません。
     * @param id 受講生ID
     */
    @Update("""
    UPDATE students
    SET is_deleted = true
    WHERE id = #{id} AND is_deleted = false
    """)
    void deleteStudent(String id);

    /**
     * 削除された受講生の復元を行います。
     * @param id 受講生ID
     */
    @Update("""
    UPDATE students
    SET is_deleted = false
    WHERE id = #{id} AND is_deleted = true
""")
    void restoreStudent(String id);

    /**
     * 指定したメールアドレスが既に登録されているかを確認します。
     *
     * @param email 受講生メールアドレス
     * @return 存在する場合はtrue、存在しない場合はfalse
     */
    boolean existsByEmail(String email);
}



