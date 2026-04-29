package raisetech.StudentManagement.repository;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StudentRepositoryTest {

    @Autowired
    private StudentRepository sut;

    @Test
    void 受講生の全件検索が行えること() {
        List<Student> actual = sut.search();
        assertThat(actual).hasSize(5);
    }

    @Test
    void 登録されていないID情報の照会時にnullが返ってくること() {
        Student actual = sut.searchStudent("999");
        assertThat(actual).isNull();
    }

    @Test
    void 受講生コース情報の全件検索が行えること() {
        List<StudentCourse> actual = sut.searchStudentCourseList();
        //5件のデータがDBから返ってくることを想定
        assertThat(actual).hasSize(6);
        //実際のデータが一致しているかコース内情報をすべて比較
        assertThat(actual)
                .extracting(StudentCourse::getCourseName)
                .containsExactlyInAnyOrder(
                        "Javaコース",
                        "AWSコース",
                        "Webデザインコース",
                        "Javaコース",
                        "AWSコース",
                        "Javaコース"
                );
    }

    @Test
    void ID情報を元に受講生の詳細検索が行えること() {
        Student actual = sut.searchStudent("1");
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo("1");
        assertThat(actual.getName()).isEqualTo("山田 太郎");
        assertThat(actual.getFurigana()).isEqualTo("ヤマダ タロウ");
        assertThat(actual.getNickname()).isEqualTo("たろう");
        assertThat(actual.getEmail()).isEqualTo("taro.yamada@example.com");
        assertThat(actual.getArea()).isEqualTo("東京都");
        assertThat(actual.getAge()).isEqualTo(25);
        assertThat(actual.getGender()).isEqualTo("男性");
        assertThat(actual.getRemarks()).isEqualTo("");
        assertThat(actual.isDeleted()).isFalse();
    }

    @Test
    void 受講生IDに紐づいたコース情報の詳細検索が行えること() {
        //repositoryからID情報1のコース情報を取得
        List<StudentCourse> actual = sut.searchStudentCourse("1");
        StudentCourse studentCourse = actual.get(0);
        assertThat(actual).hasSize(1);
        assertThat(studentCourse.getStudentId()).isEqualTo("1");
        assertThat(studentCourse.getCourseName()).isEqualTo("Javaコース");
        assertThat(studentCourse.getCourseStartAt()).isEqualTo(LocalDateTime.of(2026, 4, 1, 0, 0));
        assertThat(studentCourse.getCourseEndAt()).isEqualTo(LocalDateTime.of(2027, 4, 1, 0, 0));
    }

    @Test
    void 存在しない受講生IDにコース情報が紐づかないこと() {
        List<StudentCourse> actual = sut.searchStudentCourse("999");
        assertThat(actual).isEmpty();
    }

    @Test
    void 受講生の登録処理が行えること() {
        Student student = new Student();
        student.setName("柿沢純一");
        student.setFurigana("カキサワジュンイチ");
        student.setNickname("ジュン");
        student.setEmail("kakisawa@example.com");
        student.setArea("茨城県");
        student.setAge(26);
        student.setGender("男性");
        student.setRemarks("");
        student.setDeleted(false);
        sut.registerStudent(student);
        List<Student> actual = sut.search();
        assertThat(actual).hasSize(6);
    }

    @Test
    void 受講生コースの登録処理が行えること() {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId("1");
        studentCourse.setCourseName("Javaコース");
        studentCourse.setCourseStartAt(LocalDateTime.of(2026, 4, 22, 0, 0));
        studentCourse.setCourseEndAt(LocalDateTime.of(2027, 4, 22, 0, 0));
        sut.registerStudentCourse(studentCourse);
        List<StudentCourse> actual = sut.searchStudentCourseList();
        assertThat(actual).hasSize(7);

    }

    @Test
    void 受講生の更新処理が行えること() {
        //今回は名前だけ変更
        //repositoryから受講生取得
        //別の名前を与える
        Student before = sut.searchStudent("1");
        //登録されている名前が更新内容と
        //違うかの確認
        assertThat(before.getName()).isNotEqualTo("山田 真一");
        before.setName("山田 真一");
        //今の情報を更新処理
        sut.updateStudent(before);
        //repositoryから再取得
        Student after = sut.searchStudent("1");
        //山田 真一で更新されたかの確認
        assertThat(after.getId()).isEqualTo("1");
        assertThat(after.getName()).isEqualTo("山田 真一");
        assertThat(after.getEmail()).isEqualTo("taro.yamada@example.com");
    }

    @Test
    void コース情報の更新処理が行えること() {
        List<StudentCourse> courseList = sut.searchStudentCourse("1");
        StudentCourse course = courseList.get(0);
        assertThat(course.getCourseName()).isNotEqualTo("AWSコース");
        course.setCourseName("AWSコース");
        sut.updateStudentCourse(course);

        StudentCourse updated = sut.searchStudentCourse("1").get(0);
        assertThat(updated.getStudentId()).isEqualTo("1");
        assertThat(updated.getCourseName()).isEqualTo("AWSコース");
    }

    @Test
    void 受講生の論理削除が行えること() {
        Student before = sut.searchStudent("1");
        assertThat(before.isDeleted()).isFalse();
        sut.deleteStudent("1");
        Student after = sut.searchStudent("1");
        assertThat(after.getId()).isEqualTo("1");
        assertThat(after.getName()).isEqualTo("山田 太郎");
        assertThat(after.isDeleted()).isTrue();
    }

    @Test
    void 受講生の復元処理が行えること() {
        //準備
        Student before = sut.searchStudent("1");
        //確認
        assertThat(before.isDeleted()).isFalse();
        //論理削除
        sut.deleteStudent("1");
        //再取得
        Student after = sut.searchStudent("1");
        //確認
        assertThat(after.isDeleted()).isTrue();
        //復元処理
        sut.restoreStudent("1");
        //復元処理の値を取得
        Student restored = sut.searchStudent("1");
        //確認
        assertThat(restored.getId()).isEqualTo("1");
        assertThat(restored.getName()).isEqualTo("山田 太郎");
        assertThat(restored.isDeleted()).isFalse();
    }

    @Test
    void 登録されているメールアドレスの確認ができること() {
        int actual = sut.countByEmail("taro.yamada@example.com");
        assertThat(actual).isEqualTo(1);
    }

    @Test
    void 登録されていないメールアドレスの件数が0件であること() {
        int actual = sut.countByEmail("test@example.com");
        assertThat(actual).isEqualTo(0);
    }
}



