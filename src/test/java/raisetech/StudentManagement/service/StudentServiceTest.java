package raisetech.StudentManagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.StudentManagement.controller.converter.StudentConverter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.DuplicateEmailException;
import raisetech.StudentManagement.exception.StudentAlreadyActiveException;
import raisetech.StudentManagement.exception.StudentAlreadyDeletedException;
import raisetech.StudentManagement.exception.StudentNotFoundException;
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
    void 受講生一覧検索機能_repositoryとconverterの処理が適切に呼び出せていること() {

        //実行
        List<Student> studentList = new ArrayList<>();
        List<StudentCourse> studentCourseList = new ArrayList<>();

        when(repository.search()).thenReturn(studentList);
        when(repository.searchStudentCourseList()).thenReturn(studentCourseList);

        sut.searchStudentList();
        //検証
        verify(repository, times(1)).search();
        verify(repository, times(1)).searchStudentCourseList();
        verify(converter, times(1)).convertStudentDetails(studentList, studentCourseList);

    }

    @Test
    void 受講生詳細検索機能_受講生が存在する場合に受講生詳細情報が適切に返却されること() {
        //準備(サービスが使用する情報　Mockがあるため実際のDBは使用しない)
        //テストのための仮の値を準備(今回は1)
        String id = "1";
        //受講生を1名準備(DBは使わないため)
        Student student = new Student();
        //テスト用に準備した受講生に1のID情報を持たせる
        student.setId("1");
        //空のコースリストを用意
        List<StudentCourse> studentCourseList = new ArrayList<>();

        // repository.searchStudent(id) が呼ばれたときに、student を返すようMockに設定する
        when(repository.searchStudent(id)).thenReturn(student);

        // repository.searchStudentCourse(student.getId()) が呼ばれたときに、studentCourseList を返すようMockに設定する
        when(repository.searchStudentCourse(student.getId())).thenReturn(studentCourseList);

        //実行
        //actualに実際の処理結果を持たせる
        StudentDetail actual = sut.searchStudent(id);

        //検証
        //actualがnullかどうかの確認
        assertNotNull(actual);
        //返ってきた値の中身が正しいかどうかを確認
        //studentが入っているか？
        assertEquals(student, actual.getStudent());
        //studentCourseListが入っているか？
        assertEquals(studentCourseList, actual.getStudentCourseList());

        //検証
        //サービスで受講生情報を取得した後、コース検索まで行えているか
        //一連の流れが1回処理されているかどうかの確認
        verify(repository, times(1)).searchStudent(id);
        verify(repository, times(1)).searchStudentCourse(student.getId());

    }

    @Test
    void 受講生詳細検索の実施時にStudentに値がない場合に値が処理が行われないこと() {
        //準備
        String id = "abc";
        when(repository.searchStudent(id)).thenReturn(null);

        //実行・検証
        assertThrows(StudentNotFoundException.class, () -> sut.searchStudent(id));
        verify(repository, times(1)).searchStudent(id);
        verify(repository, never()).searchStudentCourse(anyString());
    }

    @Test
    void 受講生登録機能_入力した値が適切に処理され受講生が登録されること() {
        //準備
        //必要な各オブジェクトを用意
        //受講生
        Student student = new Student();
        //コース情報
        StudentCourse studentCourse = new StudentCourse();
        //受講生詳細
        StudentDetail studentDetail = new StudentDetail();
        //受講生詳細に生徒を詰める
        studentDetail.setStudent(student);

        //仮のメールアドレスを用意
        student.setEmail("taro@example.com");

        //空のコース情報のリストを作成
        List<StudentCourse> studentCourseList = new ArrayList<>();
        //仮のコース情報をリストに１件追加
        studentCourseList.add(studentCourse);
        //受講生詳細にコース情報を詰める
        studentDetail.setStudentCourseList(studentCourseList);
        //メールの重複判定を行う
        when(repository.countByEmail(student.getEmail())).thenReturn(0);

        //実行
        //登録処理の流れをsutに詰める
        sut.registerStudent(studentDetail);

        //検証
        //それぞれ1回ずつ処理を行うか検証
        verify(repository, times(1)).countByEmail(student.getEmail());
        verify(repository, times(1)).registerStudent(student);
        verify(repository, times(1)).registerStudentCourse(studentCourse);


    }

    @Test
    void 受講生登録時の例外処理_Emailの確認時に重複していた場合の例外処理が行われること() {
        //準備
        //受講生
        Student student = new Student();
        //受講生詳細
        StudentDetail studentDetail = new StudentDetail();
        //受講生のメールアドレス
        student.setEmail("taro@example.com");
        //受講生詳細に受講生を詰める
        studentDetail.setStudent(student);
        //メールアドレスの重複判定を行う
        //今回は同様のメールアドレスが1つあったと仮定
        when(repository.countByEmail(student.getEmail())).thenReturn(1);

        //検証
        //重複している場合にDuplicateEmailExceptionの処理を呼ぶ
        assertThrows(DuplicateEmailException.class, () -> sut.registerStudent(studentDetail));

        //重複していた場合に受講生詳細が登録をされていないか検証
        verify(repository, never()).registerStudent(student);
        verify(repository, never()).registerStudentCourse(any());
    }

    @Test
    void 受講生登録処理_コース名が複数ある場合に適切に処理され登録されること() {
        //準備
        //必要な各オブジェクトを用意
        Student student = new Student();
        StudentCourse course1 = new StudentCourse();
        StudentCourse course2 = new StudentCourse();
        StudentDetail studentDetail = new StudentDetail();

        studentDetail.setStudent(student);

        student.setEmail("taro@example.com");

        List<StudentCourse> studentCourseList = new ArrayList<>();
        //仮のコース情報をリストに2件追加
        studentCourseList.add(course1);
        studentCourseList.add(course2);

        studentDetail.setStudentCourseList(studentCourseList);


        when(repository.countByEmail(student.getEmail())).thenReturn(0);

        sut.registerStudent(studentDetail);

        //メールアドレスの重複判定は1回
        verify(repository, times(1)).countByEmail(student.getEmail());
        //受講生の登録判定は1回
        verify(repository, times(1)).registerStudent(student);
        //コース情報の登録判定は2回
        verify(repository, times(2)).registerStudentCourse(any());
    }

    @Test
    void 受講生の初期情報設定_受講生登録時にコース情報へ初期値が正しく設定されること() {
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

        //StudentCourse型の引数を捕まえるためのCaptorを用意する
        ArgumentCaptor<StudentCourse> captor = ArgumentCaptor.forClass(StudentCourse.class);
        //repositoryのregisterStudentCourseが呼ばれたときの引数をcaptorで取得する
        verify(repository).registerStudentCourse(captor.capture());
        //実物を取り出す
        StudentCourse capturedCourse = captor.getValue();

        //受講生IDが1であるか
        assertEquals("1", capturedCourse.getStudentId());
        //開始日時がnullでないか
        assertNotNull(capturedCourse.getCourseStartAt());
        //終了日時がnullでないか
        assertNotNull(capturedCourse.getCourseEndAt());
        //終了日時が、開始日時の丁度1年後になっているか
        assertEquals(capturedCourse.getCourseStartAt().plusYears(1), capturedCourse.getCourseEndAt());
    }

    @Test
    void 受講生の初期設定_受講生登録時に複数のコース情報へ初期値が正しく設定されること() {

        Student student = new Student();
        student.setId("1");
        StudentCourse course1 = new StudentCourse();
        StudentCourse course2 = new StudentCourse();
        StudentDetail studentDetail = new StudentDetail();

        studentDetail.setStudent(student);

        student.setEmail("taro@example.com");

        List<StudentCourse> studentCourseList = new ArrayList<>();
        //仮のコース情報をリストに2件追加
        studentCourseList.add(course1);
        studentCourseList.add(course2);

        studentDetail.setStudentCourseList(studentCourseList);


        when(repository.countByEmail(student.getEmail())).thenReturn(0);

        sut.registerStudent(studentDetail);

        //StudentCourse型の引数を捕まえる
        ArgumentCaptor<StudentCourse> captor = ArgumentCaptor.forClass(StudentCourse.class);
        //resisterStudentCourseが2回呼ばれたことを確認　
        //引数をすべてcaptorにつめる
        verify(repository, times(2)).registerStudentCourse(captor.capture());
        //捕まえた2件をまとめて取り出す
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
    void 受講生更新_更新対象の受講生が存在しない場合の例外処理が適切に行われること() {
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
    void 受講生論理削除_論理削除対象の受講生が存在する場合の論理削除が適切に行われること(){
        Student student = new Student();
        student.setId("1");
        student.setDeleted(false);

        when(repository.searchStudent("1")).thenReturn(student);

        sut.deleteStudent("1");

        verify(repository, times(1)).searchStudent("1");
        verify(repository, times(1)).deleteStudent("1");

    }
    @Test
    void 受講生論理削除_論理削除対象の受講生が存在しない場合の例外処理が適切に行われること(){

        when(repository.searchStudent("1")).thenReturn(null);

        assertThrows(StudentNotFoundException.class, () -> sut.deleteStudent("1"));
        verify(repository).searchStudent("1");
        verify(repository,never()).deleteStudent("1");

    }
    @Test
    void 受講生論理削除_論理削除済みの受講生を論理削除しようとした際の例外処理が適切に行われること(){
        Student student = new Student();
        student.setDeleted(true);

        when(repository.searchStudent("1")).thenReturn(student);

        assertThrows(StudentAlreadyDeletedException.class , () -> sut.deleteStudent("1"));
        verify(repository,never()).deleteStudent("1");
        verify(repository).searchStudent("1");
    }
    @Test
    void 受講生復元_受講生の復元処理が適切に行われること(){
        Student student = new Student();
        student.setId("1");
        student.setDeleted(true);

        when(repository.searchStudent("1")).thenReturn(student);

        sut.restoreStudent("1");

        verify(repository,times(1)).restoreStudent("1");
    }
    @Test
    void 受講生復元_受講生が存在しない場合に例外処理が行われること(){
        when(repository.searchStudent("1")).thenReturn(null);

        assertThrows(StudentNotFoundException.class, () -> sut.restoreStudent("1"));

        verify(repository,never()).restoreStudent("1");
        verify(repository,times(1)).searchStudent("1");
    }
    @Test
    void 受講生復元_受講生が復元済みの場合に例外処理が行われること(){
        Student student = new Student();
        student.setDeleted(false);

        when(repository.searchStudent("1")).thenReturn(student);

        assertThrows(StudentAlreadyActiveException.class , () -> sut.restoreStudent("1"));
        verify(repository,times(1)).searchStudent("1");
        verify(repository, never()).restoreStudent("1");

    }
}

