package raisetech.StudentManagement.service;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.StudentManagement.data.ApplicationStatus;
import raisetech.StudentManagement.data.ApplicationStatusType;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.exception.*;
import raisetech.StudentManagement.repository.ApplicationStatusRepository;
import raisetech.StudentManagement.repository.StudentRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationStatusServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ApplicationStatusRepository applicationStatusRepository;

    private ApplicationStatusService sut;

    @BeforeEach
    void before() {
        sut = new ApplicationStatusService(studentRepository, applicationStatusRepository);
    }

    @Test
    void 検索処理_申込状況の検索が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        ApplicationStatus actual = sut.searchApplicationStatus(studentCourseId);

        assertEquals(applicationStatus, actual);
        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 検索処理_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.searchApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 検索処理_受講生コースID情報が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "999";

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class,
                () -> sut.searchApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, never()).searchApplicationStatusByStudentCourseId(any());
    }


    @Test
    void 初期申込作成処理_仮申込が自動作成されること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        sut.createInitialApplicationStatus(studentCourseId);

        var captor = forClass(ApplicationStatus.class);
        verify(applicationStatusRepository, times(1)).createApplicationStatus(captor.capture());
        ApplicationStatus capturedStatus = captor.getValue();

        assertEquals(studentCourseId, capturedStatus.getStudentCourseId());
        assertEquals(ApplicationStatusType.PROVISIONAL.getLabel(), capturedStatus.getStatus());
        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 初期申込作成処理_受講生コースIDが存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.createInitialApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(applicationStatusRepository, never()).createApplicationStatus(any());
    }

    @Test
    void 初期申込作成処理_申込状況がすでに存在する場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus existingApplicationStatus = new ApplicationStatus();
        existingApplicationStatus.setStudentCourseId(studentCourseId);

        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId))
                .thenReturn(existingApplicationStatus);
        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);

        assertThrows(StudentAlreadyAppliedException.class,
                () -> sut.createInitialApplicationStatus(studentCourseId));
        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).createApplicationStatus(any());
    }

    @Test
    void 本申込処理_本申込処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.confirmApplicationStatus(studentCourseId);

        var captor = forClass(ApplicationStatus.class);
        verify(applicationStatusRepository, times(1)).updateApplicationStatus(captor.capture());
        ApplicationStatus capturedStatus = captor.getValue();

        assertEquals(studentCourseId, capturedStatus.getStudentCourseId());
        assertEquals(ApplicationStatusType.CONFIRMED.getLabel(), capturedStatus.getStatus());
        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 本申込処理_仮申込状況以外の場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.CONFIRMED.getLabel());

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.confirmApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 本申込処理_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.confirmApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }
    @Test
    void 本申込処理_論理削除されている場合に例外処理が適切に行われること() throws Exception{
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        applicationStatus.setDeleted(true);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(ApplicationStatusAlreadyDeletedException.class, () -> sut.confirmApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 本申込処理_受講生コースIDが存在しない場合例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.confirmApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講開始処理_受講開始処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.CONFIRMED.getLabel());

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.startApplicationStatus(studentCourseId);

        var captor = forClass(ApplicationStatus.class);
        verify(applicationStatusRepository, times(1)).updateApplicationStatus(captor.capture());
        ApplicationStatus capturedStatus = captor.getValue();

        assertEquals(studentCourseId, capturedStatus.getStudentCourseId());
        assertEquals(ApplicationStatusType.ACTIVE.getLabel(), capturedStatus.getStatus());
        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 受講開始処理_本申込以外の状態の場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.ACTIVE.getLabel());

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.startApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }
    @Test
    void 受講開始処理_論理削除されている場合に例外処理が適切に行われること() throws Exception{
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.CONFIRMED.getLabel());
        applicationStatus.setDeleted(true);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(ApplicationStatusAlreadyDeletedException.class, () -> sut.startApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講開始処理_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.startApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講開始処理_受講生コースIDが存在しない場合例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.startApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講完了処理_受講完了処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.ACTIVE.getLabel());

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.completeApplicationStatus(studentCourseId);

        var captor = forClass(ApplicationStatus.class);
        verify(applicationStatusRepository, times(1)).updateApplicationStatus(captor.capture());
        ApplicationStatus capturedStatus = captor.getValue();

        assertEquals(studentCourseId, capturedStatus.getStudentCourseId());
        assertEquals(ApplicationStatusType.COMPLETED.getLabel(), capturedStatus.getStatus());
        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 受講完了処理_受講中以外の状態の場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.COMPLETED.getLabel());

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.completeApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講完了処理_論理削除されている場合に例外処理が適切に行われること() throws Exception{
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.ACTIVE.getLabel());
        applicationStatus.setDeleted(true);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(ApplicationStatusAlreadyDeletedException.class, () -> sut.completeApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講完了処理_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.completeApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講完了処理_受講生コースIDが存在しない場合例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.completeApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(applicationStatusRepository, never()).updateApplicationStatus(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = ApplicationStatusType.class,
            names = {"PROVISIONAL", "CONFIRMED"}
    )
    void 申込状況キャンセル_キャンセル可能な状態の場合にキャンセル処理が適切に行われること(ApplicationStatusType statusType) {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(statusType.getLabel());
        applicationStatus.setDeleted(false);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.cancelApplicationStatus(studentCourseId);

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, times(1)).logicalDeleteApplicationStatus(applicationStatus);
    }

    @ParameterizedTest
    @EnumSource(
            value = ApplicationStatusType.class,
            names = {"ACTIVE", "COMPLETED"}
    )
    void 申込状況キャンセル_キャンセル不可能な場合の例外処理が適切に行われること(ApplicationStatusType statusType) {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(statusType.getLabel());
        applicationStatus.setDeleted(false);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.cancelApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 申込状況キャンセル_すでにキャンセル済みの場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        applicationStatus.setDeleted(true);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(ApplicationStatusAlreadyDeletedException.class, () -> sut.cancelApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 申込状況キャンセル_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.cancelApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 申込状況キャンセル_受講生コースIDが存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.cancelApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(applicationStatusRepository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 完了状態の論理削除_受講完了した申込状況の論理削除が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.COMPLETED.getLabel());
        applicationStatus.setDeleted(false);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.archiveCompletedApplicationStatus(studentCourseId);

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, times(1)).logicalDeleteApplicationStatus(applicationStatus);
    }

    @ParameterizedTest
    @EnumSource(
            value = ApplicationStatusType.class,
            names = {"COMPLETED"},
            mode = EnumSource.Mode.EXCLUDE
    )
    void 完了状態の論理削除_完了状態以外で論理削除を行った時に例外処理が適切に行われること(ApplicationStatusType statusType) {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(statusType.getLabel());
        applicationStatus.setDeleted(false);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.archiveCompletedApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 完了状態の論理削除_すでに論理削除済みの場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.COMPLETED.getLabel());
        applicationStatus.setDeleted(true);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(ApplicationStatusAlreadyDeletedException.class, () -> sut.archiveCompletedApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 完了状態の論理削除_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.archiveCompletedApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 完了状態の論理削除_受講生コースIDが存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.archiveCompletedApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(applicationStatusRepository, never()).logicalDeleteApplicationStatus(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = ApplicationStatusType.class,
            names = {"PROVISIONAL", "CONFIRMED"}
    )
    void 復元処理_復元可能な状態の場合に復元処理が適切に行われること(ApplicationStatusType statusType) {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(statusType.getLabel());
        applicationStatus.setDeleted(true);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.restoreApplicationStatus(studentCourseId);

        assertEquals(ApplicationStatusType.PROVISIONAL.getLabel(), applicationStatus.getStatus());
        assertFalse(applicationStatus.isDeleted());

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, times(1)).restoreApplicationStatus(applicationStatus);
    }

    @ParameterizedTest
    @EnumSource(
            value = ApplicationStatusType.class,
            names = {"ACTIVE", "COMPLETED"}
    )
    void 復元処理_復元不可能な場合の例外処理が適切に行われること(ApplicationStatusType statusType) {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(statusType.getLabel());
        applicationStatus.setDeleted(true);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.restoreApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).restoreApplicationStatus(any());
    }

    @Test
    void 復元処理_すでに申込状況が有効状態の場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        applicationStatus.setDeleted(false);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(ApplicationStatusAlreadyActiveException.class, () -> sut.restoreApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).restoreApplicationStatus(any());
    }

    @Test
    void 復元処理_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(applicationStatusRepository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.restoreApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(applicationStatusRepository, never()).restoreApplicationStatus(any());
    }

    @Test
    void 復元処理_受講生コースIDが存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(studentRepository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.restoreApplicationStatus(studentCourseId));

        verify(studentRepository, times(1)).searchStudentCourseById(studentCourseId);
        verify(applicationStatusRepository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(applicationStatusRepository, never()).restoreApplicationStatus(any());
    }

    @Nonnull
    private static StudentCourse createStudentCourse(String studentCourseId) {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setId(studentCourseId);
        return studentCourse;
    }
}
