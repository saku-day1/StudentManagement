package raisetech.StudentManagement.repository;

import org.apache.ibatis.annotations.*;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentsCourses;

import java.util.List;

/**
 * Student情報を扱うリポジトリ
 * 全件検索や単一条件での検索が行えるクラスです。
 */

@Mapper
public interface StudentRepository {
    /**
     * 全件検索します。
     *
     * @return 全件検索した受講生情報の一覧。
     *
     */
    @Select("SELECT * FROM students ")
    List<Student> search();

    /**
     *
     * @param id
     * @return
     */

    @Select("SELECT * FROM students WHERE id = #{id}")
    Student searchStudent(String id);

    /**
     * 全件検索します。
     *
     * @return 全件検索したコース情報の一覧。
     */
    @Select("SELECT * FROM student_courses")
    List<StudentsCourses> searchStudentsCourseList();

    /**
     * 生徒の追加
     *
     */
    @Select("SELECT * FROM student_courses WHERE student_id = #{studentId}")
    List<StudentsCourses> searchStudentsCourses(String studentId);

    @Insert("INSERT INTO students(name,furigana,nickname,email,area,age,gender,remarks,is_deleted) VALUES" +
            "(#{name},#{furigana},#{nickname},#{email},#{area},#{age},#{gender},#{remarks}, false)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void registerStudent(Student student);

    /*
    コース情報の追加
     */
    @Insert("INSERT INTO student_courses(student_id,course_name,course_start_at,course_end_at)" +
            "values(#{studentId},#{courseName},#{courseStartAt},#{courseEndAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void registerStudentCourse(StudentsCourses studentCourse);


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
                    deleted = #{deleted}
                WHERE id = #{id}
            """)
    void updateStudent(Student student);


    @Update("""
                UPDATE student_courses
                SET course_name = #{courseName}
                WHERE id = #{id}
            """)
    void updateStudentCourse(StudentsCourses studentCourse);
}


