package raisetech.StudentManagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentCaptor.forClass;
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

        var captor = forClass(StudentCourse.class);
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

        var captor = forClass(StudentCourse.class);
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

        var captor = forClass(StudentCourse.class);
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

}
