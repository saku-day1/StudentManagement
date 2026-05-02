package raisetech.StudentManagement.repository;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import raisetech.StudentManagement.dto.StudentSearchCriteria;
import raisetech.StudentManagement.dto.StudentSearchSummary;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/search-test-data.sql")
class StudentSearchRepositoryTest {

    @Autowired
    private StudentSearchRepository sut;

    @Test
    void 条件なしで検索した場合に受講生サマリー一覧が取得できること() {
        List<StudentSearchSummary> result =
                sut.searchStudentSummaries(new StudentSearchCriteria());

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getStudentId()).isNotNull();
        assertThat(result.get(0).getName()).isNotNull();
        assertThat(result.get(0).getCourseName()).isNotNull();
    }

    @Test
    void 名前を部分一致で検索した場合に条件に一致する受講生サマリー情報が返ること() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setName("田");

        List<StudentSearchSummary> result =
                sut.searchStudentSummaries(criteria);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(StudentSearchSummary::getName)
                .containsExactlyInAnyOrder("山田 太郎", "田中 恒一", "田辺 彩");
        assertThat(result).extracting(StudentSearchSummary::getName)
                .doesNotContain("佐藤 花子");
    }

    @Test
    void コース名で検索した場合に条件に一致する受講生サマリー情報が返ること() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setCourseName("Javaコース");

        List<StudentSearchSummary> result =
                sut.searchStudentSummaries(criteria);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(StudentSearchSummary::getName)
                .containsExactlyInAnyOrder("山田 太郎", "鈴木 一郎", "田中 恒一");

        assertThat(result)
                .extracting(StudentSearchSummary::getCourseName)
                .containsOnly("Javaコース");
    }

    @Test
    void 申込状況で検索した場合に条件に一致する受講生サマリー情報が返ること() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setStatus("仮申込");

        List<StudentSearchSummary> result =
                sut.searchStudentSummaries(criteria);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(StudentSearchSummary::getName)
                .containsExactlyInAnyOrder("山田 太郎", "田中 恒一");

        assertThat(result)
                .extracting(StudentSearchSummary::getStatus)
                .containsOnly("仮申込");
    }

    @Test
    void 申込状況を指定しない場合に申込状況がnullの受講生も含めて返ること() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setStatus(null);

        List<StudentSearchSummary> result =
                sut.searchStudentSummaries(criteria);

        assertThat(result).hasSize(7);
        assertThat(result).extracting(StudentSearchSummary::getName)
                .contains("田辺 彩");

        assertThat(result)
                .extracting(StudentSearchSummary::getStatus)
                .containsNull();
    }

    @Test
    void 名前とコース名と申込状況で検索した場合に一致した受講生サマリーが返ること() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setName("高橋");
        criteria.setCourseName("Webデザインコース");
        criteria.setStatus("終了");

        List<StudentSearchSummary> result =
                sut.searchStudentSummaries(criteria);

        StudentSearchSummary summary = result.get(0);

        assertThat(summary.getName()).isEqualTo("高橋 美咲");
        assertThat(summary.getCourseName()).isEqualTo("Webデザインコース");
        assertThat(summary.getStatus()).isEqualTo("終了");
    }

    @Test
    void 条件に一致しない名前で検索した場合に空のリストが返ること() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setName("村上");

        List<StudentSearchSummary> result =
                sut.searchStudentSummaries(criteria);

        assertThat(result).isEmpty();
    }

    @Test
    void 名前とコース名と申込状況で検索した場合にコース名が一致しなかった場合に空のリストが返ること() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setName("高橋");
        criteria.setCourseName("Javaコース");
        criteria.setStatus("終了");

        List<StudentSearchSummary> result =
                sut.searchStudentSummaries(criteria);

        assertThat(result).isEmpty();
    }
}