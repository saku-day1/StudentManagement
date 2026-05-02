package raisetech.StudentManagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.StudentManagement.dto.StudentSearchCriteria;
import raisetech.StudentManagement.dto.StudentSearchSummary;
import raisetech.StudentManagement.repository.StudentSearchRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentSearchServiceTest {
    @Mock
    private StudentSearchRepository repository;

    private StudentSearchService sut;

    @BeforeEach
    void before() {
        sut = new StudentSearchService(repository);

    }
    @Test
    void 検索条件をRepositoryへ渡し検索結果を返すこと() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setName("田中");

        StudentSearchSummary summary = new StudentSearchSummary();
        summary.setStudentId(1);
        summary.setName("田中啓介");

        when(repository.searchStudentSummaries(criteria)).thenReturn(List.of(summary));

        List<StudentSearchSummary> actual = sut.searchStudentSummaries(criteria);

        assertEquals(1,actual.size());
        assertEquals(1, actual.get(0).getStudentId());
        assertEquals("田中啓介", actual.get(0).getName());

        verify(repository, times(1)).searchStudentSummaries(criteria);

    }
    @Test
    void 該当データがない場合に空リストを返すこと() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setStudentId(999);

        when(repository.searchStudentSummaries(criteria))
                .thenReturn(List.of());

        List<StudentSearchSummary> actual = sut.searchStudentSummaries(criteria);

        assertEquals(0, actual.size());

        verify(repository, times(1)).searchStudentSummaries(criteria);
    }
}