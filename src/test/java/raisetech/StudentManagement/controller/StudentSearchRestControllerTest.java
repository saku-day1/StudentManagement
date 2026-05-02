package raisetech.StudentManagement.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import raisetech.StudentManagement.dto.StudentSearchCriteria;
import raisetech.StudentManagement.dto.StudentSearchSummary;
import raisetech.StudentManagement.service.StudentSearchService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentSearchRestController.class)
class StudentSearchRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentSearchService service;

    @Test
    void 完全一致条件で検索した場合に条件に一致する受講生サマリー情報が返ること() throws Exception {
        StudentSearchSummary searchSummary = new StudentSearchSummary();
        searchSummary.setStudentId(1);
        searchSummary.setStudentCourseId(1);
        searchSummary.setName("田中啓介");
        searchSummary.setFurigana("タナカケイスケ");
        searchSummary.setEmail("keisuke@example.com");
        searchSummary.setCourseName("Webデザインコース");
        searchSummary.setStatus("仮申込");

        when(service.searchStudentSummaries(any())).thenReturn(List.of(searchSummary));

        mockMvc.perform(get("/api/students/search")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studentId").value(1))
                .andExpect(jsonPath("$[0].name").value("田中啓介"))
                .andExpect(jsonPath("$[0].furigana").value("タナカケイスケ"))
                .andExpect(jsonPath("$[0].email").value("keisuke@example.com"))
                .andExpect(jsonPath("$[0].courseName").value("Webデザインコース"))
                .andExpect(jsonPath("$[0].status").value("仮申込"));

        ArgumentCaptor<StudentSearchCriteria> captor =
                ArgumentCaptor.forClass(StudentSearchCriteria.class);

        verify(service, times(1)).searchStudentSummaries(captor.capture());

        assertEquals(1, captor.getValue().getStudentId());
    }

    @Test
    void 該当データがない場合に空の配列データが返るこ() throws Exception {
        when(service.searchStudentSummaries(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/students/search")
                        .param("studentId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service, times(1)).searchStudentSummaries(any());
    }

    @Test
    void 検索条件に一致するデータが複数ある場合に配列で返ること() throws Exception {
        StudentSearchSummary summary1 = new StudentSearchSummary();
        summary1.setStudentId(1);
        summary1.setStudentCourseId(1);
        summary1.setName("田中啓介");
        summary1.setFurigana("タナカケイスケ");
        summary1.setEmail("keisuke@example.com");
        summary1.setCourseName("Javaコース");
        summary1.setStatus("仮申込");

        StudentSearchSummary summary2 = new StudentSearchSummary();
        summary2.setStudentId(2);
        summary2.setStudentCourseId(2);
        summary2.setName("田中太郎");
        summary2.setFurigana("タナカタロウ");
        summary2.setEmail("taro@example.com");
        summary2.setCourseName("Javaコース");
        summary2.setStatus("本申込");

        when(service.searchStudentSummaries(any()))
                .thenReturn(List.of(summary1, summary2));

        mockMvc.perform(get("/api/students/search")
                        .param("name", "田中"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].studentId").value(1))
                .andExpect(jsonPath("$[0].name").value("田中啓介"))
                .andExpect(jsonPath("$[1].studentId").value(2))
                .andExpect(jsonPath("$[1].name").value("田中太郎"));

        verify(service, times(1)).searchStudentSummaries(any());
    }

    @ParameterizedTest
    @CsvSource({
            "name, 田中",
            "furigana, タナカ",
            "courseName, Javaコース",
            "status, 仮申込"
    })
    void 各検索条件で検索できること(String paramName, String paramValue) throws Exception {
        StudentSearchSummary summary = new StudentSearchSummary();
        summary.setStudentId(1);
        summary.setStudentCourseId(1);
        summary.setName("田中啓介");
        summary.setFurigana("タナカケイスケ");
        summary.setEmail("keisuke@example.com");
        summary.setCourseName("Javaコース");
        summary.setStatus("仮申込");

        when(service.searchStudentSummaries(any()))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/api/students/search")
                        .param(paramName, paramValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studentId").value(1))
                .andExpect(jsonPath("$[0].name").value("田中啓介"));

        verify(service, times(1)).searchStudentSummaries(any());
    }

    @ParameterizedTest
    @CsvSource({
            "studentId, abc",
            "studentCourseId, abc"
    })
    void 不正なIDで検索した場合にステータス400とエラーメッセージが返ること(
            String paramName,
            String paramValue
    ) throws Exception {
        mockMvc.perform(get("/api/students/search")
                        .param(paramName, paramValue))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(containsString(paramName)))
                .andExpect(jsonPath("$.message").value(containsString("Integer")));

        verify(service, never()).searchStudentSummaries(any());
    }
}
