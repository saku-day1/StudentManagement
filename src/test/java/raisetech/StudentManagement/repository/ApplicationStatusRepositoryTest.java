package raisetech.StudentManagement.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import raisetech.StudentManagement.data.ApplicationStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MybatisTest
class ApplicationStatusRepositoryTest {

    @Autowired
    private ApplicationStatusRepository sut;

    @Test
    void 申込検索_受講生コースIDに基づいた検索が行えること() {
        ApplicationStatus actual
                = sut.searchApplicationStatusByStudentCourseId("1");

        assertNotNull(actual);
        assertEquals("1", actual.getStudentCourseId());
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
    void 受講生の新規申込処理が行えること() {
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId("6");
        applicationStatus.setStatus("仮申込");

        sut.createApplicationStatus(applicationStatus);

        ApplicationStatus actual =
                sut.searchApplicationStatusByStudentCourseId("6");

        assertThat(actual).isNotNull();
        assertThat(actual.getStudentCourseId()).isEqualTo("6");
        assertThat(actual.getStatus()).isEqualTo("仮申込");
        assertThat(actual.isDeleted()).isFalse();
        assertThat(actual.getDeletedAt()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "4, 本申込",
            "3, 受講終了"
    })
    void 申込状況の更新処理が行えること(String studentCourseId, String updateStatus) {
        ApplicationStatus applicationStatus =
                sut.searchApplicationStatusByStudentCourseId(studentCourseId);

        applicationStatus.setStatus(updateStatus);

        sut.updateApplicationStatus(applicationStatus);

        ApplicationStatus actual =
                sut.searchApplicationStatusByStudentCourseId(studentCourseId);

        assertThat(actual.getStatus()).isEqualTo(updateStatus);
        assertThat(actual.isDeleted()).isFalse();
    }

    @Test
    void 論理削除_申込状況の論理削除が行えること() {
        ApplicationStatus applicationStatus =
                sut.searchApplicationStatusByStudentCourseId("4");

        sut.logicalDeleteApplicationStatus(applicationStatus);

        ApplicationStatus result =
                sut.searchApplicationStatusByStudentCourseId("4");

        assertThat(result.isDeleted()).isTrue();
        assertThat(result.getDeletedAt()).isNotNull();
    }

    @Test
    void 復元処理_申込状況の復元処理が行えること() {
        ApplicationStatus applicationStatus =
                sut.searchApplicationStatusByStudentCourseId("2");

        sut.restoreApplicationStatus(applicationStatus);

        ApplicationStatus result =
                sut.searchApplicationStatusByStudentCourseId("2");

        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getStudentCourseId()).isEqualTo("2");
    }

    @Test
    void 物理削除_6か月より古い論理削除済み申込状況を削除できること() {
        LocalDateTime threshold = LocalDateTime.of(2025, 10, 27, 0, 0);

        sut.deleteOldDeletedApplicationStatuses(threshold);

        assertThat(sut.searchApplicationStatusByStudentCourseId("1")).isNull();
        assertThat(sut.searchApplicationStatusByStudentCourseId("5")).isNull();

        assertThat(sut.searchApplicationStatusByStudentCourseId("2")).isNotNull();
        assertThat(sut.searchApplicationStatusByStudentCourseId("3")).isNotNull();
        assertThat(sut.searchApplicationStatusByStudentCourseId("4")).isNotNull();
    }

    @Test
    void 物理削除_未削除の申込状況は削除しないこと() {
        LocalDateTime threshold = LocalDateTime.of(2025, 10, 27, 0, 0);

        sut.deleteOldDeletedApplicationStatuses(threshold);

        assertThat(sut.searchApplicationStatusByStudentCourseId("3")).isNotNull();
        assertThat(sut.searchApplicationStatusByStudentCourseId("4")).isNotNull();
    }

    @Test
    void 物理削除_6か月より新しい論理削除済み申込状況を削除しないこと() {
        LocalDateTime threshold = LocalDateTime.of(2025, 10, 27, 0, 0);

        sut.deleteOldDeletedApplicationStatuses(threshold);

        ApplicationStatus result =
                sut.searchApplicationStatusByStudentCourseId("2");

        assertThat(result).isNotNull();
        assertThat(result.isDeleted()).isTrue();
        assertThat(result.getDeletedAt()).isNotNull();
    }
}

