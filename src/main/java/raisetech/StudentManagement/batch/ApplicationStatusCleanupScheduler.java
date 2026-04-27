package raisetech.StudentManagement.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import raisetech.StudentManagement.service.ApplicationStatusService;

@Slf4j
@Component
public class ApplicationStatusCleanupScheduler {

    private final ApplicationStatusService service;

    public ApplicationStatusCleanupScheduler(ApplicationStatusService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {
        log.info("古い論理削除データの物理削除処理を開始します");

        service.deleteOldDeletedApplicationStatuses();

        log.info("古い論理削除データの物理削除処理が終了しました");
    }
}
