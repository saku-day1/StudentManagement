package raisetech.StudentManagement.repository;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import raisetech.StudentManagement.data.Student;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository sut;

    @Test
    void 受講生の全件検索が行えること(){
        List<Student> actual = sut.search();

        assertThat(actual.size()).isEqualTo(5);
    }
    @Test
    void 受講生の登録処理が行えること(){
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

        assertThat(actual.size()).isEqualTo(6);
    }

}