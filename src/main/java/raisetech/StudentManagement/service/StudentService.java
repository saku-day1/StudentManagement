package raisetech.StudentManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagement.controller.converter.StudentConverter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;

import raisetech.StudentManagement.dto.UpdateStudentRequest;
import raisetech.StudentManagement.exception.DuplicateEmailException;
import raisetech.StudentManagement.exception.StudentNotFoundException;
import raisetech.StudentManagement.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 受講生情報を取り扱うサービスです。
 * 受講生の検索や登録、更新処理を行います。
 */
@Service
public class StudentService {

    private final StudentRepository repository;
    private final StudentConverter converter;

    @Autowired
    public StudentService(StudentRepository repository, StudentConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }

    /**
     * 受講生詳細の一覧検索を行います。
     * 全件検索を行うので、条件指定は行いません。
     *
     * @return 受講生詳細一覧(全件)
     */
    public List<StudentDetail> searchStudentList() {
        List<Student> studentList = repository.search();
        List<StudentCourse> studentCourseList = repository.searchStudentCourseList();
        return converter.convertStudentDetails(studentList, studentCourseList);
    }

    /**
     * 受講生詳細検索です。
     * IDに紐づく受講生詳細情報を取得した後、その受講生情報に紐づく受講生コース情報を取得して設定します。
     *
     * @param id 受講生ID
     * @return 受講生詳細
     */
    public StudentDetail searchStudent(String id) {
        Student student = repository.searchStudent(id);
        if(student == null){
            throw new StudentNotFoundException("ID:" + id + " の受講生情報が見つかりませんでした");
        }
        List<StudentCourse> studentCourse = repository.searchStudentCourse(student.getId());
        return new StudentDetail(student, studentCourse);
    }


    /**
     * 受講生詳細の登録を行います。
     * 受講生と受講生コース情報を個別に登録し、受講生コース情報には受講生情報を紐づける値やコース開始日、コース終了日を設定します。
     *
     * @param studentDetail 受講生詳細
     */
    @Transactional
    public void registerStudent(StudentDetail studentDetail) {
        Student student = studentDetail.getStudent();

        if(repository.countByEmail(student.getEmail()) > 0){
            throw new DuplicateEmailException(student.getEmail() + " はすでに使われているメールアドレスです");
        }
        repository.registerStudent(student);

        studentDetail.getStudentCourseList().forEach(studentCourse -> {
            initStudentsCourse(studentCourse, student);
            repository.registerStudentCourse(studentCourse);
        });
    }

    /**
     * 受講生コース情報を登録する際の初期情報を設定する。
     *
     * @param studentCourse 受講生コース情報
     * @param student       受講生
     */
    private static void initStudentsCourse(StudentCourse studentCourse, Student student) {
        LocalDateTime now = LocalDateTime.now();

        studentCourse.setStudentId(student.getId());
        studentCourse.setCourseStartAt(now);
        studentCourse.setCourseEndAt(now.plusYears(1));
    }

    /**
     * 受講生情報の更新を行います。
     * 受講生の情報と受講生コース情報をそれぞれ更新します。
     *
     * @param studentDetail 受講生詳細
     */
    @Transactional
    public void updateStudent(StudentDetail studentDetail) {
        repository.updateStudent(studentDetail.getStudent());

        studentDetail.getStudentCourseList().forEach(studentCourse -> {
            studentCourse.setStudentId(studentDetail.getStudent().getId());
            repository.updateStudentCourse(studentCourse);
        });
    }

    public void deleteStudent(String id) {
        repository.deleteStudent(id);
    }

    public void restoreStudent(String id) {
        repository.restoreStudent(id);
    }

    @Transactional
    public void updateDetailStudent(UpdateStudentRequest request) {
        Student student = new Student();
        student.setId(request.getId());
        student.setName(request.getName());
        student.setFurigana(request.getFurigana());
        student.setNickname(request.getNickname());
        student.setEmail(request.getEmail());
        student.setArea(request.getArea());
        student.setAge(request.getAge());
        student.setGender(request.getGender());
        student.setRemarks(request.getRemarks());
        student.setDeleted(request.isDeleted());
        repository.updateStudent(student);

        StudentCourse course = new StudentCourse();
        course.setId(request.getCourseId());
        course.setStudentId(request.getId());
        course.setCourseName(request.getCourseName());
        repository.updateStudentCourse(course);
    }

}






