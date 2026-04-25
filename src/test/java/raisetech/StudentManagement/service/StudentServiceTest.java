package raisetech.StudentManagement.service;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.StudentManagement.controller.converter.StudentConverter;
import raisetech.StudentManagement.data.ApplicationStatus;
import raisetech.StudentManagement.data.ApplicationStatusType;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.*;
import raisetech.StudentManagement.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository repository;

    @Mock
    private StudentConverter converter;

    private StudentService sut;

    @BeforeEach
    void before() {
        sut = new StudentService(repository, converter);
    }


    @Test
    void 受講生一覧検索_repositoryとconverterの処理が適切に呼び出されること() {

        List<Student> studentList = new ArrayList<>();
        List<StudentCourse> studentCourseList = new ArrayList<>();

        when(repository.search()).thenReturn(studentList);
        when(repository.searchStudentCourseList()).thenReturn(studentCourseList);

        sut.searchStudentList();

        verify(repository, times(1)).search();
        verify(repository, times(1)).searchStudentCourseList();
        verify(converter, times(1)).convertStudentDetails(studentList, studentCourseList);
    }

    @Test
    void 受講生詳細検索_受講生が存在する場合に詳細情報が適切に返却されること() {

        String id = "1";
        Student student = new Student();
        student.setId("1");
        List<StudentCourse> studentCourseList = new ArrayList<>();

        when(repository.searchStudent(id)).thenReturn(student);
        when(repository.searchStudentCourse(student.getId())).thenReturn(studentCourseList);

        StudentDetail actual = sut.searchStudent(id);

        assertNotNull(actual);
        assertEquals(student, actual.getStudent());
        assertEquals(studentCourseList, actual.getStudentCourseList());

        verify(repository, times(1)).searchStudent(id);
        verify(repository, times(1)).searchStudentCourse(student.getId());
    }

    @Test
    void 受講生詳細検索_受講生が存在しない場合に例外が発生すること() {

        String id = "abc";
        when(repository.searchStudent(id)).thenReturn(null);

        assertThrows(StudentNotFoundException.class, () -> sut.searchStudent(id));
        verify(repository, times(1)).searchStudent(id);
        verify(repository, never()).searchStudentCourse(anyString());
    }

    @Test
    void 受講生登録_入力した値が適切に処理され受講生が登録されること() {

        Student student = new Student();
        StudentCourse studentCourse = new StudentCourse();
        StudentDetail studentDetail = new StudentDetail();
        studentDetail.setStudent(student);

        student.setEmail("taro@example.com");

        List<StudentCourse> studentCourseList = new ArrayList<>();
        studentCourseList.add(studentCourse);
        studentDetail.setStudentCourseList(studentCourseList);

        when(repository.countByEmail(student.getEmail())).thenReturn(0);

        sut.registerStudent(studentDetail);

        verify(repository, times(1)).countByEmail(student.getEmail());
        verify(repository, times(1)).registerStudent(student);
        verify(repository, times(1)).registerStudentCourse(studentCourse);
    }

    @Test
    void 受講生登録_メールアドレスが重複している場合に例外が発生すること() {

        Student student = new Student();
        StudentDetail studentDetail = new StudentDetail();

        student.setEmail("taro@example.com");
        studentDetail.setStudent(student);

        when(repository.countByEmail(student.getEmail())).thenReturn(1);

        assertThrows(DuplicateEmailException.class, () -> sut.registerStudent(studentDetail));

        verify(repository, never()).registerStudent(student);
        verify(repository, never()).registerStudentCourse(any());
    }

    @Test
    void 受講生登録_コース名が複数ある場合に適切に処理され登録されること() {

        Student student = new Student();
        StudentCourse course1 = new StudentCourse();
        StudentCourse course2 = new StudentCourse();
        StudentDetail studentDetail = new StudentDetail();

        studentDetail.setStudent(student);
        student.setEmail("taro@example.com");

        List<StudentCourse> studentCourseList = new ArrayList<>();
        studentCourseList.add(course1);
        studentCourseList.add(course2);
        studentDetail.setStudentCourseList(studentCourseList);

        when(repository.countByEmail(student.getEmail())).thenReturn(0);

        sut.registerStudent(studentDetail);

        verify(repository, times(1)).countByEmail(student.getEmail());
        verify(repository, times(1)).registerStudent(student);
        verify(repository, times(2)).registerStudentCourse(any());
    }

    @Test
    void 受講生初期設定_受講生登録時にコース情報へ初期値が正しく設定されること() {

        Student student = new Student();
        student.setId("1");
        StudentCourse studentCourse = new StudentCourse();
        StudentDetail studentDetail = new StudentDetail();

        studentDetail.setStudent(student);
        student.setEmail("taro@example.com");

        List<StudentCourse> studentCourseList = new ArrayList<>();
        studentCourseList.add(studentCourse);
        studentDetail.setStudentCourseList(studentCourseList);

        when(repository.countByEmail(student.getEmail())).thenReturn(0);

        sut.registerStudent(studentDetail);

        ArgumentCaptor<StudentCourse> captor = ArgumentCaptor.forClass(StudentCourse.class);
        verify(repository).registerStudentCourse(captor.capture());
        StudentCourse capturedCourse = captor.getValue();

        assertEquals("1", capturedCourse.getStudentId());
        assertNotNull(capturedCourse.getCourseStartAt());
        assertNotNull(capturedCourse.getCourseEndAt());
        assertEquals(capturedCourse.getCourseStartAt().plusYears(1), capturedCourse.getCourseEndAt());
    }

    @Test
    void 受講生初期設定_受講生登録時に複数のコース情報へ初期値が正しく設定されること() {

        Student student = new Student();
        student.setId("1");
        StudentCourse course1 = new StudentCourse();
        StudentCourse course2 = new StudentCourse();
        StudentDetail studentDetail = new StudentDetail();

        studentDetail.setStudent(student);
        student.setEmail("taro@example.com");

        List<StudentCourse> studentCourseList = new ArrayList<>();
        studentCourseList.add(course1);
        studentCourseList.add(course2);
        studentDetail.setStudentCourseList(studentCourseList);

        when(repository.countByEmail(student.getEmail())).thenReturn(0);

        sut.registerStudent(studentDetail);

        ArgumentCaptor<StudentCourse> captor = ArgumentCaptor.forClass(StudentCourse.class);
        verify(repository, times(2)).registerStudentCourse(captor.capture());
        List<StudentCourse> capturedCourses = captor.getAllValues();

        StudentCourse capturedCourse1 = capturedCourses.get(0);
        StudentCourse capturedCourse2 = capturedCourses.get(1);

        assertEquals("1", capturedCourse1.getStudentId());
        assertNotNull(capturedCourse1.getCourseStartAt());
        assertNotNull(capturedCourse1.getCourseEndAt());
        assertEquals(capturedCourse1.getCourseStartAt().plusYears(1), capturedCourse1.getCourseEndAt());

        assertEquals("1", capturedCourse2.getStudentId());
        assertNotNull(capturedCourse2.getCourseStartAt());
        assertNotNull(capturedCourse2.getCourseEndAt());
        assertEquals(capturedCourse2.getCourseStartAt().plusYears(1), capturedCourse2.getCourseEndAt());
    }

    @Test
    void 受講生更新_受講生更新時に受講生情報とコース情報が適切に更新されること() {

        Student student = new Student();
        student.setId("1");
        StudentCourse course = new StudentCourse();
        StudentDetail studentDetail = new StudentDetail();
        studentDetail.setStudent(student);

        List<StudentCourse> studentCourseList = new ArrayList<>();
        studentCourseList.add(course);
        studentDetail.setStudentCourseList(studentCourseList);

        when(repository.searchStudent(studentDetail.getStudent().getId())).thenReturn(student);

        sut.updateStudent(studentDetail);

        ArgumentCaptor<StudentCourse> captor = ArgumentCaptor.forClass(StudentCourse.class);
        verify(repository, times(1)).updateStudent(student);
        verify(repository, times(1)).updateStudentCourse(captor.capture());

        StudentCourse capturedCourse = captor.getValue();

        assertEquals("1", capturedCourse.getStudentId());
    }

    @Test
    void 受講生更新_更新対象の受講生が存在しない場合に例外処理が適切に行われること() {

        Student student = new Student();
        student.setId("1");
        StudentDetail studentDetail = new StudentDetail();
        studentDetail.setStudent(student);

        when(repository.searchStudent(studentDetail.getStudent().getId())).thenReturn(null);

        assertThrows(StudentNotFoundException.class, () -> sut.updateStudent(studentDetail));

        verify(repository, times(1)).searchStudent(studentDetail.getStudent().getId());
        verify(repository, never()).updateStudent(student);
        verify(repository, never()).updateStudentCourse(any(StudentCourse.class));
    }

    @Test
    void 受講生論理削除_論理削除対象の受講生が存在する場合に論理削除が適切に行われること() {

        Student student = new Student();
        student.setId("1");
        student.setDeleted(false);

        when(repository.searchStudent("1")).thenReturn(student);

        sut.deleteStudent("1");

        verify(repository, times(1)).searchStudent("1");
        verify(repository, times(1)).deleteStudent("1");
    }

    @Test
    void 受講生論理削除_論理削除対象の受講生が存在しない場合に例外処理が適切に行われること() {

        when(repository.searchStudent("1")).thenReturn(null);

        assertThrows(StudentNotFoundException.class, () -> sut.deleteStudent("1"));
        verify(repository).searchStudent("1");
        verify(repository, never()).deleteStudent("1");
    }

    @Test
    void 受講生論理削除_論理削除済みの受講生を論理削除しようとした際に例外処理が適切に行われること() {

        Student student = new Student();
        student.setDeleted(true);

        when(repository.searchStudent("1")).thenReturn(student);

        assertThrows(StudentAlreadyDeletedException.class, () -> sut.deleteStudent("1"));
        verify(repository, never()).deleteStudent("1");
        verify(repository).searchStudent("1");
    }

    @Test
    void 受講生復元_受講生の復元処理が適切に行われること() {

        Student student = new Student();
        student.setId("1");
        student.setDeleted(true);

        when(repository.searchStudent("1")).thenReturn(student);

        sut.restoreStudent("1");

        verify(repository, times(1)).searchStudent("1");
        verify(repository, times(1)).restoreStudent("1");
    }

    @Test
    void 受講生復元_受講生が存在しない場合に例外処理が行われること() {

        when(repository.searchStudent("1")).thenReturn(null);

        assertThrows(StudentNotFoundException.class, () -> sut.restoreStudent("1"));

        verify(repository, never()).restoreStudent("1");
        verify(repository, times(1)).searchStudent("1");
    }

    @Test
    void 受講生復元_受講生が復元済みの場合に例外処理が行われること() {

        Student student = new Student();
        student.setDeleted(false);

        when(repository.searchStudent("1")).thenReturn(student);

        assertThrows(StudentAlreadyActiveException.class, () -> sut.restoreStudent("1"));
        verify(repository, never()).restoreStudent("1");
        verify(repository).searchStudent("1");
    }

    @Test
    void 申込処理_申込状況の新規作成が適切に行われること() {
        //準備
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        //実行
        sut.createApplicationStatus(studentCourseId);
        ArgumentCaptor<ApplicationStatus> captor = ArgumentCaptor.forClass(ApplicationStatus.class);
        verify(repository, times(1)).createApplicationStatus(captor.capture());
        ApplicationStatus capturedStatus = captor.getValue();
        //検証
        assertEquals(studentCourseId, capturedStatus.getStudentCourseId());
        assertEquals("仮申込", capturedStatus.getStatus());
        //確認
        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Nonnull
    private static StudentCourse createStudentCourse(String studentCourseId) {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setId(studentCourseId);
        return studentCourse;
    }

    @Test
    void 申込処理_受講生コースIDが存在しない場合に例外処理が適切に行われること() {
        //準備
        String studentCourseId = "999";
        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(null);
        //実行・検証
        assertThrows(StudentCourseNotFoundException.class, () -> sut.createApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(repository, never()).createApplicationStatus(any());
    }

    @Test
    void 申込処理_申込状況がすでに存在する場合に例外処理が適切に行われること() {
        //準備
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus existingApplicationStatus = new ApplicationStatus();
        existingApplicationStatus.setStudentCourseId(studentCourseId);
        //実行・検証
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId))
                .thenReturn(existingApplicationStatus);
        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);

        assertThrows(StudentAlreadyAppliedException.class,
                () -> sut.createApplicationStatus(studentCourseId));
        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).createApplicationStatus(any());
    }

    @Test
    void 本申込処理_本申込処理が適切に行われること() {
        //受講生コースID準備
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        //申込状況の準備
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        //仮申込状況で準備
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        //検索処理の実行時の戻り値を設定
        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);
        //テスト対象の準備
        sut.confirmApplicationStatus(studentCourseId);
        ArgumentCaptor<ApplicationStatus> captor = ArgumentCaptor.forClass(ApplicationStatus.class);
        verify(repository, times(1)).updateApplicationStatus(captor.capture());
        ApplicationStatus capturedStatus = captor.getValue();

        assertEquals(studentCourseId, capturedStatus.getStudentCourseId());
        assertEquals(ApplicationStatusType.CONFIRMED.getLabel(), capturedStatus.getStatus());


        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 本申込処理_仮申込状況以外の場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);

        applicationStatus.setStatus(ApplicationStatusType.CONFIRMED.getLabel());

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.confirmApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).updateApplicationStatus(any());
    }

    @Test
    void 本申込処理_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.confirmApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).updateApplicationStatus(any());
    }

    @Test
    void 本申込処理_受講生コースIDが存在しない場合例外処理が適切に行われること() {

        //準備
        String studentCourseId = "999";
        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(null);
        //実行・検証
        assertThrows(StudentCourseNotFoundException.class, () -> sut.confirmApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(repository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講開始処理_受講開始処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.CONFIRMED.getLabel());

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.startApplicationStatus(studentCourseId);

        ArgumentCaptor<ApplicationStatus> captor = ArgumentCaptor.forClass(ApplicationStatus.class);
        verify(repository, times(1)).updateApplicationStatus(captor.capture());
        ApplicationStatus capturedStatus = captor.getValue();

        assertEquals(studentCourseId, capturedStatus.getStudentCourseId());
        assertEquals(ApplicationStatusType.ACTIVE.getLabel(), capturedStatus.getStatus());

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 受講開始処理_本申込以外の状態の場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);

        applicationStatus.setStatus(ApplicationStatusType.ACTIVE.getLabel());

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.startApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講開始処理_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.startApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講開始処理_受講生コースIDが存在しない場合例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.startApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(repository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講完了処理_受講完了処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.ACTIVE.getLabel());

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.completeApplicationStatus(studentCourseId);

        ArgumentCaptor<ApplicationStatus> captor = ArgumentCaptor.forClass(ApplicationStatus.class);
        verify(repository, times(1)).updateApplicationStatus(captor.capture());
        ApplicationStatus capturedStatus = captor.getValue();

        assertEquals(studentCourseId, capturedStatus.getStudentCourseId());
        assertEquals(ApplicationStatusType.COMPLETED.getLabel(), capturedStatus.getStatus());

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
    }

    @Test
    void 受講完了処理_受講中以外の状態の場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);

        applicationStatus.setStatus(ApplicationStatusType.COMPLETED.getLabel());

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.completeApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講完了処理_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.completeApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).updateApplicationStatus(any());
    }

    @Test
    void 受講完了処理_受講生コースIDが存在しない場合例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(null);

        assertThrows(StudentCourseNotFoundException.class, () -> sut.completeApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(repository, never()).updateApplicationStatus(any());
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

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.cancelApplicationStatus(studentCourseId);

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, times(1)).logicalDeleteApplicationStatus(applicationStatus);
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

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.cancelApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 申込状況キャンセル_すでにキャンセル済みの場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        applicationStatus.setDeleted(true);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(ApplicationStatusAlreadyDeletedException.class, () -> sut.cancelApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 申込状況キャンセル_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.cancelApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 申込状況キャンセル_受講生コースIDが存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(null);
        assertThrows(StudentCourseNotFoundException.class, () -> sut.cancelApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(repository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 完了状態の論理削除_受講完了した申込状況の論理削除が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.COMPLETED.getLabel());
        applicationStatus.setDeleted(false);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        sut.archiveCompletedApplicationStatus(studentCourseId);

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, times(1)).logicalDeleteApplicationStatus(applicationStatus);
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

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(InvalidApplicationException.class, () -> sut.archiveCompletedApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 完了状態の論理削除_すでに論理削除済みの場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.COMPLETED.getLabel());
        applicationStatus.setDeleted(true);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(applicationStatus);

        assertThrows(ApplicationStatusAlreadyDeletedException.class, () -> sut.archiveCompletedApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 完了状態の論理削除_申込状況が存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "1";
        StudentCourse studentCourse = createStudentCourse(studentCourseId);

        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(studentCourse);
        when(repository.searchApplicationStatusByStudentCourseId(studentCourseId)).thenReturn(null);

        assertThrows(ApplicationStatusNotFoundException.class,
                () -> sut.archiveCompletedApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, times(1)).searchApplicationStatusByStudentCourseId(studentCourseId);
        verify(repository, never()).logicalDeleteApplicationStatus(any());
    }

    @Test
    void 完了状態の論理削除_受講生コースIDが存在しない場合に例外処理が適切に行われること() {
        String studentCourseId = "999";
        when(repository.searchStudentCourseById(studentCourseId)).thenReturn(null);
        assertThrows(StudentCourseNotFoundException.class, () -> sut.archiveCompletedApplicationStatus(studentCourseId));

        verify(repository, times(1)).searchStudentCourseById(studentCourseId);
        verify(repository, never()).searchApplicationStatusByStudentCourseId(any());
        verify(repository, never()).logicalDeleteApplicationStatus(any());
    }
}
