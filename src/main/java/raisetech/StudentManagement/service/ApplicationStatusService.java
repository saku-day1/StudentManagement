package raisetech.StudentManagement.service;

import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagement.data.ApplicationStatus;
import raisetech.StudentManagement.data.ApplicationStatusType;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.exception.*;
import raisetech.StudentManagement.repository.ApplicationStatusRepository;
import raisetech.StudentManagement.repository.StudentRepository;

import java.time.LocalDateTime;

/**
 * 申込状況の検索、作成、状態変更、論理削除を扱うサービスクラスです。
 */
@Service
public class ApplicationStatusService {

    private final StudentRepository studentRepository;
    private final ApplicationStatusRepository applicationStatusRepository;

    public ApplicationStatusService(StudentRepository studentRepository,
                                    ApplicationStatusRepository applicationStatusRepository) {
        this.studentRepository = studentRepository;
        this.applicationStatusRepository = applicationStatusRepository;
    }

    /**
     * 受講生コースID情報に基づいて申込状況を検索します。
     *
     * @param studentCourseId 受講生コースID
     * @return 受講生コースIDに紐づく申込状況
     * @throws ApplicationStatusNotFoundException 申込状況がない場合
     */
    public ApplicationStatus searchApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus =
                applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId);
        if (applicationStatus == null) {
            throw new ApplicationStatusNotFoundException(studentCourseId);
        }
        return applicationStatus;
    }

    /**
     * 受講生コースIDに基づき申込状況を初期状態（仮申込）として作成します。
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException 指定した受講生コース情報IDが存在しない場合
     * @throws StudentAlreadyAppliedException すでに申込状況が存在する場合
     */
    @Transactional
    public void createApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);

        ApplicationStatus existing =
                applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId);

        if (existing != null) {
            throw new StudentAlreadyAppliedException(studentCourseId);
        }

        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        applicationStatus.setDeleted(false);

        applicationStatusRepository.createApplicationStatus(applicationStatus);
    }

    /**
     * 受講生コース情報に基づいて申込状況を確定します
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException           指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException       申込状況がない場合
     * @throws ApplicationStatusAlreadyDeletedException 論理削除されている場合
     * @throws InvalidApplicationException              仮申込状態以外のステータスである場合
     */
    @Transactional
    public void confirmApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus
                = findApplicationStatusOrThrow(studentCourseId);

        validateApplicationStatusNotDeleted(applicationStatus, studentCourseId);

        ApplicationStatusType statusType
                = ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.canConfirm()) {
            throw new InvalidApplicationException(
                    studentCourseId,
                    "仮申込状態のみ本申込に変更できます。"
            );
        }
        applicationStatus.setStatus(ApplicationStatusType.CONFIRMED.getLabel());
        applicationStatusRepository.updateApplicationStatus(applicationStatus);
    }

    /**
     * 受講生コース情報に基づいて受講開始処理を行います
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException     指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException 申込状況がない場合
     * @throws InvalidApplicationException        本申込状態以外のステータスである場合
     */
    @Transactional
    public void startApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);

        ApplicationStatus applicationStatus =
                findApplicationStatusOrThrow(studentCourseId);

        validateApplicationStatusNotDeleted(applicationStatus, studentCourseId);

        ApplicationStatusType statusType =
                ApplicationStatusType.fromLabel(applicationStatus.getStatus());

        if (!statusType.canStart()) {
            throw new InvalidApplicationException(
                    studentCourseId,
                    "本申込状態のみ受講中に変更できます。"
            );
        }
        applicationStatus.setStatus(ApplicationStatusType.ACTIVE.getLabel());
        applicationStatusRepository.updateApplicationStatus(applicationStatus);
    }

    /**
     * 受講生コース情報に基づいて終了処理を行います
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException     指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException 申込状況がない場合
     * @throws InvalidApplicationException        受講中以外のステータスである場合
     */
    @Transactional
    public void completeApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus
                = findApplicationStatusOrThrow(studentCourseId);

        validateApplicationStatusNotDeleted(applicationStatus, studentCourseId);

        ApplicationStatusType statusType
                = ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.canComplete()) {
            throw new InvalidApplicationException(
                    studentCourseId,
                    "受講中のみ終了にできます。");
        }
        applicationStatus.setStatus(ApplicationStatusType.COMPLETED.getLabel());
        applicationStatusRepository.updateApplicationStatus(applicationStatus);
    }

    /**
     * 仮申込・本申込のキャンセルを行う。
     * キャンセルされた申込状況は履歴として保持され、復元はできない。
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException     指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException 申込状況がない場合
     * @throws InvalidApplicationException        受講中・終了のステータスである場合
     */
    @Transactional
    public void cancelApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus = findApplicationStatusOrThrow(studentCourseId);
        validateApplicationStatusNotDeleted(applicationStatus, studentCourseId);
        ApplicationStatusType statusType =
                ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.isCancelable()) {
            throw new InvalidApplicationException(
                    studentCourseId,
                    "この申込状況はキャンセルできません。");
        }
        applicationStatusRepository.logicalDeleteApplicationStatus(applicationStatus);
    }

    /**
     * 終了済みの申込状況を論理削除（非表示化）する。
     * 業務上は履歴整理のための処理であり、キャンセルとは異なる。
     * 終了の場合のみ論理削除可能とし、それ以外の状態の場合は例外をスローします。
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException           指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException       申込状況がない場合
     * @throws ApplicationStatusAlreadyDeletedException すでに論理削除されている場合
     * @throws InvalidApplicationException              完了以外のステータスである場合
     */
    @Transactional
    public void archiveCompletedApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);
        ApplicationStatus applicationStatus = findApplicationStatusOrThrow(studentCourseId);
        validateApplicationStatusNotDeleted(applicationStatus, studentCourseId);
        ApplicationStatusType statusType = ApplicationStatusType.fromLabel(applicationStatus.getStatus());
        if (!statusType.canArchive()) {
            throw new InvalidApplicationException(
                    studentCourseId,
                    "終了状態のみ非表示化できます。");
        }
        applicationStatusRepository.logicalDeleteApplicationStatus(applicationStatus);
    }

    /**
     * 申込状況の復元処理を行う。
     * 仮申込および本申込状態のみ対象とし、
     * 復元後は仮申込状態へ変更する。
     *
     * @param studentCourseId 受講生コース情報ID
     * @throws StudentCourseNotFoundException          指定した受講生コース情報IDが存在しない場合
     * @throws ApplicationStatusNotFoundException      申込状況がない場合
     * @throws ApplicationStatusAlreadyActiveException すでに有効状態の場合
     * @throws InvalidApplicationException             仮申込もしくは本申込状態以外の場合
     */
    @Transactional
    public void restoreApplicationStatus(String studentCourseId) {
        validateStudentCourseExists(studentCourseId);

        ApplicationStatus applicationStatus = findApplicationStatusOrThrow(studentCourseId);

        if (!applicationStatus.isDeleted()) {
            throw new ApplicationStatusAlreadyActiveException(studentCourseId);
        }

        ApplicationStatusType statusType =
                ApplicationStatusType.fromLabel(applicationStatus.getStatus());

        if (!statusType.isCancelable()) {
            throw new InvalidApplicationException(
                    studentCourseId,
                    "仮申込または本申込状態のみ復元できます。"
            );
        }
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        applicationStatus.setDeleted(false);

        applicationStatusRepository.restoreApplicationStatus(applicationStatus);
    }

    /**
     * 論理削除された申込処理のデータを６か月経過後に物理削除します。
     */
    @Transactional
    public void deleteOldDeletedApplicationStatuses() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);
        applicationStatusRepository.deleteOldDeletedApplicationStatuses(threshold);
    }

    /**
     * 受講生コースIDに紐づく申込状況を取得します。
     * 申込状況が存在しない場合は例外をスローします。
     *
     * @param studentCourseId 受講生コースID
     * @return 受講生コースIDに紐づく申込状況
     * @throws ApplicationStatusNotFoundException 申込状況が存在しない場合
     */
    @Nonnull
    private ApplicationStatus findApplicationStatusOrThrow(String studentCourseId) {
        ApplicationStatus applicationStatus =
                applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId);
        if (applicationStatus == null) {
            throw new ApplicationStatusNotFoundException(studentCourseId);
        }
        return applicationStatus;
    }

    /**
     * 指定された受講生コースIDが存在することを確認します。
     *
     * @param studentCourseId 受講生コースID
     * @throws StudentCourseNotFoundException 受講生コースIDが存在しない場合
     */
    private void validateStudentCourseExists(String studentCourseId) {
        StudentCourse studentCourse = studentRepository.searchStudentCourseById(studentCourseId);
        if (studentCourse == null) {
            throw new StudentCourseNotFoundException(studentCourseId);
        }
    }

    /**
     * 申込状況が論理削除されていないことを確認します。
     *
     * @param applicationStatus 確認対象の申込状況
     * @param studentCourseId   受講生コースID
     * @throws ApplicationStatusAlreadyDeletedException 申込状況が論理削除されている場合
     */
    private void validateApplicationStatusNotDeleted(
            ApplicationStatus applicationStatus,
            String studentCourseId) {
        if (applicationStatus.isDeleted()) {
            throw new ApplicationStatusAlreadyDeletedException(studentCourseId);
        }
    }
}



