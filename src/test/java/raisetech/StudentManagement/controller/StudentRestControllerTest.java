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
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.DuplicateEmailException;
import raisetech.StudentManagement.exception.StudentNotFoundException;
import raisetech.StudentManagement.service.StudentService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                                                "name": "山田 太郎",
                                                "furigana": "ヤマダタロウ",
                                                "email": "yamada@example.com",
                                                "area": "東京都"
                                              },
                                              "studentCourseList":[{
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
                .andExpect(jsonPath("$.message").value("登録処理が成功しました"));
        //StudentDetailを掴まえるようのArgumentCaptorを用意
        ArgumentCaptor<StudentDetail> captor = ArgumentCaptor.forClass(StudentDetail.class);
        //registerStudent()に対して渡された引数をいれる
        verify(service).registerStudent(captor.capture());
        //捕まえたStudentDetailをcapturedStudentとして取り出す
        StudentDetail capturedStudent = captor.getValue();
        //検証
        //取り出したStudentDetailの中身の照合
        assertEquals("山田 太郎",capturedStudent.getStudent().getName());
        assertEquals("ヤマダタロウ",capturedStudent.getStudent().getFurigana());
        assertEquals("yamada@example.com",capturedStudent.getStudent().getEmail());
        assertEquals("東京都",capturedStudent.getStudent().getArea());
        assertEquals(1,capturedStudent.getStudentCourseList().size());
        assertEquals("Javaコース",capturedStudent.getStudentCourseList().get(0).getCourseName());
    }

    @Test
    void 受講生登録でnameが空文字のときに400エラーとエラーメッセージが返ること() throws Exception {
        //準備
        String json = """
                {
                                              "student": {
                                                "name": "",
                                                "furigana": "ヤマダタロウ",
                                                "email": "yamada@example.com",
                                                "area": "東京都"
                                              },
                                              "studentCourseList":[{
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
                                              "studentCourseList":[{
                                                  "courseName": "Javaコース"
                                                }
                                                ]
                                            }
                """;
        //戻り値がないため、doThrow
        //登録処理ではなくメールの重複が起きたと想定して例外処理が発生したと動作を変更する
        doThrow(new DuplicateEmailException("yamada@example.com はすでに使われているメールアドレスです"))
                .when(service).registerStudent(any(StudentDetail.class));
        mockMvc.perform(
                        post("/api/students")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("yamada@example.com はすでに使われているメールアドレスです"));
        verify(service, times(1)).registerStudent(any(StudentDetail.class));

    }
    @Test
    void 受講生更新で更新処理のリクエストを送信した時に成功メッセージが返ること() throws Exception{
        //準備
        String json = """
                {
                                              "student": {
                                                "name": "山田 太郎",
                                                "furigana": "ヤマダタロウ",
                                                "email": "yamada@example.com",
                                                "area": "東京都"
                                              },
                                              "studentCourseList":[{
                                                  "courseName": "Javaコース"
                                                }
                                                ]
                                            }
                """;
        mockMvc.perform(put("/api/students/{id}",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("受講生情報を更新しました"));
        ArgumentCaptor<StudentDetail> captor = ArgumentCaptor.forClass(StudentDetail.class);
        verify(service).updateStudent(captor.capture());
        StudentDetail capturedStudent = captor.getValue();
        assertEquals("1", capturedStudent.getStudent().getId());
        assertEquals("山田 太郎",capturedStudent.getStudent().getName());
        assertEquals("ヤマダタロウ",capturedStudent.getStudent().getFurigana());
        assertEquals("yamada@example.com",capturedStudent.getStudent().getEmail());
        assertEquals("東京都",capturedStudent.getStudent().getArea());
        assertEquals(1,capturedStudent.getStudentCourseList().size());
        assertEquals("Javaコース",capturedStudent.getStudentCourseList().get(0).getCourseName());
    }
    @Test
    void 受講生更新で名前が未入力の場合に400エラーが返ること() throws Exception{
        String json = """
                {
                                              "student": {
                                                "name": "",
                                                "furigana": "ヤマダタロウ",
                                                "email": "yamada@example.com",
                                                "area": "東京都"
                                              },
                                              "studentCourseList":[{
                                                  "courseName": "Javaコース"
                                                }
                                                ]
                                            }
                """;
        mockMvc.perform(put("/api/students/{id}",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                //実行
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("student.name: 名前は必須です"));
        verify(service,never()).updateStudent(any(StudentDetail.class));
    }
    @Test
    void 受講生更新時に存在しないIDを送信した場合に404エラーが返ってくること() throws Exception{
        String id = "999";
        String json = """
                {
                                              "student": {
                                                "name": "山田 太郎",
                                                "furigana": "ヤマダタロウ",
                                                "email": "yamada@example.com",
                                                "area": "東京都"
                                              },
                                              "studentCourseList":[{
                                                  "courseName": "Javaコース"
                                                }
                                                ]
                                            }
                """;
        doThrow(new StudentNotFoundException(id))
                .when(service).updateStudent(any(StudentDetail.class));
        mockMvc.perform(put("/api/students/{id}",999)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                .value("受講生ID：999 が見つかりません。"));

        verify(service,times(1)).updateStudent(any(StudentDetail.class));
    }
    }



