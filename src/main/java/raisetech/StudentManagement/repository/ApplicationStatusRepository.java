package raisetech.StudentManagement.repository;

import org.apache.ibatis.annotations.Mapper;
import raisetech.StudentManagement.data.ApplicationStatus;

/**
 * 申込状況テーブルと紐づくRepositoryです。
 */
@Mapper
public interface ApplicationStatusRepository {

    /**
     * 受講生コースIDに基づいて申込状況を検索します。
     *
     * @param studentCourseId 受講生コースID
     * @return 申込状況
     */
    ApplicationStatus searchApplicationStatusByStudentCourseId(String studentCourseId);

    /**
     * 仮申込状況を作成します。
     *
     * @param applicationStatus 申込状況
     */
    void createApplicationStatus(ApplicationStatus applicationStatus);

    /**
     * 現在の申込状況を更新します。
     *
     * @param applicationStatus 申込状況
     */
    void updateApplicationStatus(ApplicationStatus applicationStatus);

    /**
     * 申込状況を論理削除します。
     *
     * @param applicationStatus 申込状況
     */
    void logicalDeleteApplicationStatus(ApplicationStatus applicationStatus);

    /**
     * 申込状況を復元します。
     * @param applicationStatus 申込状況
     */
    void restoreApplicationStatus(ApplicationStatus applicationStatus);
}
