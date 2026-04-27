package raisetech.StudentManagement.repository;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import raisetech.StudentManagement.data.ApplicationStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@MybatisTest
class ApplicationStatusRepositoryTest {

    @Autowired
    private ApplicationStatusRepository sut;

    @Test
    void 申込検索_受講生コースIDに基づいた検索が行えること(){
        ApplicationStatus actual
                = sut.searchApplicationStatusByStudentCourseId("1");

        assertNotNull(actual);
        assertEquals("1",actual.getStudentCourseId());
        assertEquals("仮申込", actual.getStatus());
        assertThat(actual.isDeleted()).isTrue();
    }
    @Test
    void 申込検索_存在しない受講生コースIDに申込状況が紐づかないこと() {
        ApplicationStatus actual
                = sut.searchApplicationStatusByStudentCourseId("999");
        assertThat(actual).isNull();
    }

    @Test
    void 受講生の新規申込処理が行えること(){

    }

}