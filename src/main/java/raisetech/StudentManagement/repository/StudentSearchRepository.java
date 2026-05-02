package raisetech.StudentManagement.repository;

import org.apache.ibatis.annotations.Mapper;
import raisetech.StudentManagement.dto.StudentSearchCriteria;
import raisetech.StudentManagement.dto.StudentSearchSummary;

import java.util.List;

/**
 * 受講生情報とコース情報と申込状況をサマリーとして紐づけるRepositoryです
 */
@Mapper
public interface StudentSearchRepository {

    /**
     * 検索条件に基づいて受講生サマリー情報を検索します。
     *
     * <p>検索条件はすべてAND条件で評価されます。
     * 条件が未指定（nullまたは空文字）の場合は、その条件は無視されます。</p>
     *
     * <p>nameおよびfuriganaは部分一致検索、
     * それ以外の項目は完全一致検索を行います。</p>
     *
     * <p>申込状況はLEFT JOINで取得するため、
     * 申込状況が存在しない受講生も検索対象に含まれます。</p>
     *
     * @param criteria 検索条件
     * @return 受講生サマリー情報のリスト（該当なしの場合は空リスト）
     */
    List<StudentSearchSummary> searchStudentSummaries(StudentSearchCriteria criteria);
}
