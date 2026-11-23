package vn.edu.seminar.loan;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static vn.edu.seminar.loan.LoanException.Code.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    @Mock BookRepository books;
    @Mock MemberRepository members;
    @Mock LoanRepository loans;
    @Captor ArgumentCaptor<Loan> loanCaptor;
    private LoanService service;
    private final Instant fixed = Instant.parse("2026-10-16T00:00:00Z");

    @BeforeEach void setUp() {
        service = new LoanService(books, members, loans, Clock.fixed(fixed, ZoneOffset.UTC));
    }

    @Test void sachKhongTonTai() {
        when(books.findById(1)).thenReturn(Optional.empty());
        assertCode(BOOK_NOT_FOUND);
        verifyNoInteractions(members, loans);
        verify(books, never()).save(any());
    }

    @Test void sachDangDuocMuon() {
        when(books.findById(1)).thenReturn(Optional.of(new Book(1, false)));
        assertCode(BOOK_UNAVAILABLE);
        verifyNoInteractions(members, loans);
        verify(books, never()).save(any());
    }

    @Test void thanhVienKhongTonTai() {
        availableBook();
        when(members.findById(1)).thenReturn(Optional.empty());
        assertCode(MEMBER_NOT_FOUND);
        verifyNoInteractions(loans);
        verify(books, never()).save(any());
    }

    // BÀI 2a: thành viên đang mượn đủ số cuốn tối đa thì bị từ chối với mã LIMIT_REACHED.
    // Gợi ý: làm giống các test ở trên (availableBook, member, when(loans.countActiveByMemberId(1))..., assertCode).
    @Test void daDatHanMuc() {
        // TODO
    }

    // BÀI 2b: thành viên còn nợ phí thì bị từ chối với mã UNPAID_FINE.
    @Test void conPhiChuaTra() {
        // TODO
    }

    @Test void muonThanhCongKhiDangMuonHaiCuon() {
        availableBook(); member(BigDecimal.ZERO);
        when(loans.countActiveByMemberId(1)).thenReturn(2L);
        when(loans.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Loan result = service.borrow(1, 1);
        verify(loans).save(loanCaptor.capture());
        Loan saved = loanCaptor.getValue();
        assertThat(saved.id()).isNotBlank();
        assertThat(saved.bookId()).isEqualTo(1);
        assertThat(saved.memberId()).isEqualTo(1);
        assertThat(saved.borrowedAt()).isEqualTo(fixed);
        assertThat(result).isEqualTo(saved);
        verify(books).save(new Book(1, false));
    }

    private void availableBook() { when(books.findById(1)).thenReturn(Optional.of(new Book(1, true))); }
    private void member(BigDecimal debt) { when(members.findById(1)).thenReturn(Optional.of(new Member(1, debt))); }
    private void assertCode(LoanException.Code code) {
        assertThatThrownBy(() -> service.borrow(1, 1)).isInstanceOfSatisfying(LoanException.class,
                exception -> assertThat(exception.code()).isEqualTo(code));
    }
}
