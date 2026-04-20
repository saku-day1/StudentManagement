package raisetech.StudentManagement.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.DuplicateEmailException;
import raisetech.StudentManagement.exception.StudentAlreadyActiveException;
import raisetech.StudentManagement.exception.StudentAlreadyDeletedException;
import raisetech.StudentManagement.exception.StudentNotFoundException;
import raisetech.StudentManagement.handler.GlobalExceptionHandler;
import raisetech.StudentManagement.service.StudentService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentRestController.class)
class StudentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService service;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 受講生詳細の受講生検索が実行できて空のリストが返却されること() throws Exception {
        when(service.searchStudentList()).thenReturn(List.of(new StudentDetail()));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("受講生一覧を取得しました"))
                .andExpect(jsonPath("$.data[0].student").doesNotExist())
                .andExpect(jsonPath("$.data[0].studentCourseList").doesNotExist());

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

    @Test
    void 受講生の詳細検索が実行できて取得した受講生詳細とステータス200が返却されること() throws Exception {
        String id = "1";

        Student student = new Student();
        student.setId("1");
        student.setName("山本健");
        student.setFurigana("ヤマモトケン");
        student.setEmail("yamamoto.ken@example.com");
        student.setArea("福岡");

        StudentCourse course = new StudentCourse();
        course.setCourseName("Webデザインコース");

        StudentDetail studentDetail = new StudentDetail();
        studentDetail.setStudent(student);
        studentDetail.setStudentCourseList(List.of(course));

        when(service.searchStudent(id)).thenReturn(studentDetail);

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("受講生詳細を取得しました"))
                .andExpect(jsonPath("$.data.student.id").value("1"))
                .andExpect(jsonPath("$.data.student.name").value("山本健"))
                .andExpect(jsonPath("$.data.student.furigana").value("ヤマモトケン"))
                .andExpect(jsonPath("$.data.student.email").value("yamamoto.ken@example.com"))
                .andExpect(jsonPath("$.data.student.area").value("福岡"))
                .andExpect(jsonPath("$.data.studentCourseList[0].courseName").value("Webデザインコース"));

        verify(service, times(1)).searchStudent(id);
    }

    @Test
    void 存在しないIDで受講生詳細を検索したときに404エラーが返ってくること() throws Exception {
        String id = "999";
        doThrow(new StudentNotFoundException(id))
                .when(service).searchStudent(id);

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生ID：999 が見つかりません。"));

        verify(service, times(1)).searchStudent(id);
    }

    @Test
    void 受講生の登録処理のリクエストを送信したときに成功メッセージが返ること() throws Exception {
        String json = """
                {
                  "student": {
                    "name": "山田 太郎",
                    "furigana": "ヤマダタロウ",
                    "email": "yamada@example.com",
                    "area": "東京都"
                  },
                  "studentCourseList": [
                    {
                      "courseName": "Javaコース"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("登録処理が成功しました"));

        ArgumentCaptor<StudentDetail> captor = ArgumentCaptor.forClass(StudentDetail.class);
        verify(service).registerStudent(captor.capture());

        StudentDetail capturedStudent = captor.getValue();
        assertEquals("山田 太郎", capturedStudent.getStudent().getName());
        assertEquals("ヤマダタロウ", capturedStudent.getStudent().getFurigana());
        assertEquals("yamada@example.com", capturedStudent.getStudent().getEmail());
        assertEquals("東京都", capturedStudent.getStudent().getArea());
        assertEquals(1, capturedStudent.getStudentCourseList().size());
        assertEquals("Javaコース", capturedStudent.getStudentCourseList().get(0).getCourseName());
    }

    @Test
    void 受講生登録でnameが空文字のときに400エラーとエラーメッセージが返ること() throws Exception {
        String json = """
                {
                  "student": {
                    "name": "",
                    "furigana": "ヤマダタロウ",
                    "email": "yamada@example.com",
                    "area": "東京都"
                  },
                  "studentCourseList": [
                    {
                      "courseName": "Javaコース"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("student.name: 名前は必須です"));

        verify(service, never()).registerStudent(any(StudentDetail.class));
    }

    @Test
    void 受講生登録でemailがすでに登録済みの場合に409エラーとエラーメッセージが返ること() throws Exception {
        String json = """
                {
                  "student": {
                    "name": "山田 太郎",
                    "furigana": "ヤマダタロウ",
                    "email": "yamada@example.com",
                    "area": "東京都"
                  },
                  "studentCourseList": [
                    {
                      "courseName": "Javaコース"
                    }
                  ]
                }
                """;

        doThrow(new DuplicateEmailException("yamada@example.com はすでに使われているメールアドレスです"))
                .when(service).registerStudent(any(StudentDetail.class));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("yamada@example.com はすでに使われているメールアドレスです"));

        verify(service, times(1)).registerStudent(any(StudentDetail.class));
    }

    @Test
    void 受講生更新で更新処理のリクエストを送信した時に成功メッセージが返ること() throws Exception {
        String json = """
                {
                  "student": {
                    "name": "山田 太郎",
                    "furigana": "ヤマダタロウ",
                    "email": "yamada@example.com",
                    "area": "東京都"
                  },
                  "studentCourseList": [
                    {
                      "courseName": "Javaコース"
                    }
                  ]
                }
                """;

        mockMvc.perform(put("/api/students/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("受講生情報を更新しました"))
                .andExpect(jsonPath("$.data.studentId").value("1"));

        ArgumentCaptor<StudentDetail> captor = ArgumentCaptor.forClass(StudentDetail.class);
        verify(service).updateStudent(captor.capture());

        StudentDetail capturedStudent = captor.getValue();
        assertEquals("1", capturedStudent.getStudent().getId());
        assertEquals("山田 太郎", capturedStudent.getStudent().getName());
        assertEquals("ヤマダタロウ", capturedStudent.getStudent().getFurigana());
        assertEquals("yamada@example.com", capturedStudent.getStudent().getEmail());
        assertEquals("東京都", capturedStudent.getStudent().getArea());
        assertEquals(1, capturedStudent.getStudentCourseList().size());
        assertEquals("Javaコース", capturedStudent.getStudentCourseList().get(0).getCourseName());
    }

    @Test
    void 受講生更新で名前が未入力の場合に400エラーが返ること() throws Exception {
        String json = """
                {
                  "student": {
                    "name": "",
                    "furigana": "ヤマダタロウ",
                    "email": "yamada@example.com",
                    "area": "東京都"
                  },
                  "studentCourseList": [
                    {
                      "courseName": "Javaコース"
                    }
                  ]
                }
                """;

        mockMvc.perform(put("/api/students/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("student.name: 名前は必須です"));

        verify(service, never()).updateStudent(any(StudentDetail.class));
    }

    @Test
    void 受講生更新時に存在しないIDを送信した場合に404エラーが返ってくること() throws Exception {
        String id = "999";
        String json = """
                {
                  "student": {
                    "name": "山田 太郎",
                    "furigana": "ヤマダタロウ",
                    "email": "yamada@example.com",
                    "area": "東京都"
                  },
                  "studentCourseList": [
                    {
                      "courseName": "Javaコース"
                    }
                  ]
                }
                """;

        doThrow(new StudentNotFoundException(id))
                .when(service).updateStudent(any(StudentDetail.class));

        mockMvc.perform(put("/api/students/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生ID：999 が見つかりません。"));

        verify(service, times(1)).updateStudent(any(StudentDetail.class));
    }

    @Test
    void 受講生論理削除時にリクエストを送った際にステータス200と受講生IDとメッセージが返却されること() throws Exception {
        String id = "1";

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("受講生を論理削除しました"))
                .andExpect(jsonPath("$.data.studentId").value(id));

        verify(service, times(1)).deleteStudent(id);
    }

    @Test
    void 受講生の論理削除時に存在しないIDでリクエストを送ったときにステータス404エラーが返ってくること() throws Exception {
        String id = "999";
        doThrow(new StudentNotFoundException(id))
                .when(service).deleteStudent(id);

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生ID：999 が見つかりません。"));

        verify(service, times(1)).deleteStudent(id);
    }

    @Test
    void すでに論理削除済みの受講生を削除しようとした時にステータス409エラーが返ってくること() throws Exception {
        String id = "1";
        doThrow(new StudentAlreadyDeletedException(id))
                .when(service).deleteStudent(id);

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生ID：1 はすでに論理削除済みです。"));

        verify(service, times(1)).deleteStudent(id);
    }

    @Test
    void 受講生復元時にリクエストを送った際にステータス200と受講生IDとメッセージが返却されること() throws Exception {
        String id = "1";

        mockMvc.perform(patch("/api/students/{id}/restore", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("受講生情報を復元しました"))
                .andExpect(jsonPath("$.data.studentId").value(id));

        verify(service, times(1)).restoreStudent(id);
    }

    @Test
    void 受講生復元時に存在しないIDでリクエストを送った際に404エラーが返ってくること() throws Exception {
        String id = "999";
        doThrow(new StudentNotFoundException(id)).when(service).restoreStudent(id);

        mockMvc.perform(patch("/api/students/{id}/restore", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生ID：999 が見つかりません。"));

        verify(service, times(1)).restoreStudent(id);
    }

    @Test
    void すでに復元処理済みの受講生IDを復元しようとした際に409エラーが返ってくること() throws Exception {
        String id = "1";
        doThrow(new StudentAlreadyActiveException(id))
                .when(service).restoreStudent(id);

        mockMvc.perform(patch("/api/students/{id}/restore", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生ID：1 はすでに有効状態のため復元できません。"));

        verify(service, times(1)).restoreStudent(id);
    }
}