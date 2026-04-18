package raisetech.StudentManagement.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.service.StudentService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest (StudentRestController.class)
class StudentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService service;

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 受講生詳細の受講生検索が実行できて空のリストが返却されること() throws Exception {
        when(service.searchStudentList()).thenReturn(List.of(new StudentDetail()));
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"student\":null,\"studentCourseList\":null}]"));

        verify(service, times(1)).searchStudentList();
    }
        @Test
        void 受講生詳細の受講生で適切な値を入力したときに入力チェックに異常が発生しないこと() {
            Student student = new Student();
            student.setId("1");
            student.setName("牧秀悟");
            student.setFurigana("マキシュウゴ");
            student.setEmail("maki@example.com");
            student.setArea("長野");

            Set<ConstraintViolation<Student>> violations = validator.validate(student);

            assertThat(violations.size()).isEqualTo(0);
        }

    @Test
    void 受講生詳細の受講生でIDに数字以外を用いた時に入力チェックにかかること() {
        Student student = new Student();
        student.setId("テスト");
        student.setName("牧秀悟");
        student.setFurigana("マキシュウゴ");
        student.setEmail("maki@example.com");
        student.setArea("長野");

        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        assertThat(violations.size()).isEqualTo(1);
        assertThat(violations).extracting("message").containsOnly("IDは数字で入力してください");
    }

}