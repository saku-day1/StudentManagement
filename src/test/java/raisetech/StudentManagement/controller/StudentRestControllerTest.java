package raisetech.StudentManagement.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.service.StudentService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentRestController.class)
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

    @Test
    void 受講生の登録処理のリクエストを送信したときに成功メッセージが返ること() throws Exception {
        //準備
        //実際にAPIに送る登録処理のjsonのデータ
        String json = """
                {
                                              "student": {
                                                "id": "1",
                                                "name": "山田 太郎",
                                                "furigana": "ヤマダタロウ",
                                                "email": "yamada@example.com",
                                                "area": "東京都"
                                              },
                                              "studentCourseList":[{
                                                  "id": "1",
                                                  "studentId": "1",
                                                  "courseName": "Javaコース"
                                                }
                                                ]
                                            }
                """;
        //HTTPリクエストを疑似的に送信
        mockMvc.perform(post("/api/students")
                        //送るデータの形式の指定
                        //今回はJSONを送る
                        .contentType(MediaType.APPLICATION_JSON)
                        //さっき準備したJSONのデータを送信
                        .content(json)
                )
                //確認
                //実際のレスポンスが201　Createdであることを確認
                .andExpect(status().isCreated())
                //実際に返却されるmessage(JSON)を確認
                .andExpect(jsonPath("$.message").value("登録処理が成功しました"))
                //実際に返却されるstudentId(JSON)を確認し返却されるID情報が"1"であることを確認
                .andExpect(jsonPath("$.studentId").value("1"));
        //登録処理の中でサービスクラスが1回だけ呼ばれたことを確認
        verify(service, times(1)).registerStudent(any(StudentDetail.class));
    }
    @Test
    void 受講生登録でnameが空文字のときに400エラーとエラーメッセージが返ること() throws Exception{
        //準備
        String json = """
                {
                                              "student": {
                                                "id": "1",
                                                "name": "",
                                                "furigana": "ヤマダタロウ",
                                                "email": "yamada@example.com",
                                                "area": "東京都"
                                              },
                                              "studentCourseList":[{
                                                  "id": "1",
                                                  "studentId": "1",
                                                  "courseName": "Javaコース"
                                                }
                                                ]
                                            }
                """;
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        )
                //実行
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("student.name: 名前は必須です"));
        //検証
        verify(service,never()).registerStudent(any(StudentDetail.class));
    }
    @Test

}