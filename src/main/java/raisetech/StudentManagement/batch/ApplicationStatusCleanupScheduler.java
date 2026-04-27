package raisetech.StudentManagement.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import raisetech.StudentManagement.service.ApplicationStatusService;

/**
 * 論理削除された申込状況データを定期的に物理削除するスケジューラーです。
 *
 * <p>毎日午前3時に実行され、6か月以上前に論理削除された申込状況データの
 * 物理削除処理をサービス層に依頼します。</p>
 */
@Slf4j
@Component
public class ApplicationStatusCleanupScheduler {

    private final ApplicationStatusService service;

    public ApplicationStatusCleanupScheduler(ApplicationStatusService service) {
        this.service = service;
    }

    /**
     * 古い論理削除済み申込状況データの物理削除処理を定期実行します。
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {
        log.info("古い論理削除データの物理削除処理を開始します");

        service.deleteOldDeletedApplicationStatuses();

        log.info("古い論理削除データの物理削除処理が終了しました");
    }
}
