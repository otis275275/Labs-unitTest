# Báo cáo lab Nhóm 09

**Họ tên:** ___ · **MSSV:** ___ · **JDK đã dùng** (kết quả `java -version`): `java version "24.0.2" 2025-07-15 (Java(TM) SE Runtime Environment build 24.0.2+12-54)`

## Bài 1: Giá trị biên

| Ngày trễ | Hạng | Số tiền bạn điền | Cách tính |
|---:|---|---:|---|
| 24 | NORMAL | 48000 | Không có ngày ân hạn; 24 ngày trễ * 2.000đ/ngày = 48.000đ (chưa đạt mức trần 50.000đ) |
| 25 | NORMAL | 50000 | 25 ngày trễ * 2.000đ/ngày = 50.000đ (chạm đúng mức trần tối đa 50.000đ) |
| 26 | NORMAL | 50000 | 26 ngày trễ * 2.000đ/ngày = 52.000đ > 50.000đ (vượt trần nên lấy mức phí tối đa 50.000đ) |
| 2 | VIP | 0 | Được ân hạn 2 ngày đầu (ngày 1 và 2 không tính phí), daysLate = 2 <= graceDays (2) -> 0đ |
| 3 | VIP | 1000 | Ân hạn 2 ngày đầu, tính từ ngày thứ 3: (3 - 2) ngày * 1.000đ/ngày = 1.000đ |
| 51 | VIP | 49000 | (51 - 2) ngày * 1.000đ/ngày = 49 ngày * 1.000đ/ngày = 49.000đ (ngay dưới mức trần 50.000đ) |
| 52 | VIP | 50000 | (52 - 2) ngày * 1.000đ/ngày = 50 ngày * 1.000đ/ngày = 50.000đ (chạm đúng mức trần tối đa 50.000đ) |
| 53 | VIP | 50000 | (53 - 2) ngày * 1.000đ/ngày = 51 ngày * 1.000đ/ngày = 51.000đ > 50.000đ (vượt trần nên lấy mức phí tối đa 50.000đ) |

**Vì sao chọn các ngày này mà không chọn ngày 10?**
- Vì đây là kỹ thuật **Phân tích giá trị biên (Boundary Value Analysis - BVA)** kết hợp với **Phân vùng tương đương (Equivalence Partitioning)**:
  - Khi lập trình, lỗi logic thường xảy ra nhiều nhất tại các điểm ranh giới chuyển tiếp (boundary transitions) giữa các phân vùng tương đương do việc sử dụng sai toán tử so sánh (`<` thay vì `<=`, `>` thay vì `>=`, hoặc lỗi lệch 1 đơn vị - off-by-one errors).
  - Với hạng **NORMAL**: Ngưỡng chuyển tiếp là mức phí trần tối đa 50.000đ (tương ứng ngày 25 với đơn giá 2.000đ/ngày). Bộ ba giá trị (24, 25, 26) đại diện cho: giá trị ngay dưới biên (48.000đ), ngay tại biên chạm trần (50.000đ), và vượt qua biên bị áp mức trần (50.000đ).
  - Với hạng **VIP**: Có hai điểm biên chuyển tiếp:
    1. *Biên ân hạn (grace period)*: Ngày 2 là ngày cuối cùng được ân hạn (0đ), ngày 3 là ngày đầu tiên bắt đầu tính phí (1.000đ).
    2. *Biên phí trần tối đa*: Ngày 52 là ngày chạm mức tối đa 50.000đ (sau 2 ngày ân hạn với đơn giá 1.000đ/ngày). Bộ ba (51, 52, 53) kiểm tra giá trị ngay dưới biên chạm trần (49.000đ), tại biên chạm trần (50.000đ), và vượt qua biên bị áp trần (50.000đ).
  - Trong khi đó, ngày 10 là một giá trị thông thường nằm sâu bên trong phân vùng tương đương (nominal/interior value), không nằm tại bất kỳ ranh giới logic hay ngưỡng chuyển tiếp nào (với NORMAL là 20.000đ, với VIP là 8.000đ). Nếu chỉ kiểm tra ngày 10, test case không thể phát hiện được các lỗi sai lệch toán tử tại biên.

## Bài 2a: Coverage

| | Branch coverage của `LoanService` |
|---|---|
| Trước khi viết test | 66% (4/6 branches covered, 2 branches missed) |
| Sau khi viết 2 test | 100% (6/6 branches covered) |

**Test `daDatHanMuc` của bạn dùng `countActiveByMemberId` trả về giá trị nào? Vì sao chọn giá trị đó?**
- Trong bước viết test 2a ban đầu: Test có thể dùng `4L` vì với code ban đầu đang có điều kiện `> 3`, giá trị `4L` làm biểu thức `4 > 3` trả về `true`, thỏa mãn nhánh ném lỗi `LIMIT_REACHED` và giúp JaCoCo đạt 100% branch coverage.
- Tuy nhiên, khi đối chiếu kỹ với đặc tả nghiệp vụ: *"Mỗi thành viên được mượn tối đa 3 cuốn: đang mượn 3 cuốn thì không được mượn thêm"*, giá trị biên chính xác theo yêu cầu nghiệp vụ phải là `3L` (thành viên đang mượn đủ 3 cuốn thì khi mượn tiếp cuốn thứ 4 phải bị từ chối ngay với mã lỗi `LIMIT_REACHED`). Vì vậy, giá trị chuẩn xác cần chọn theo đặc tả là `3L`.

## Bài 2b: Mutation testing

**Lần chạy PIT đầu tiên:** có bao nhiêu mutant sống sót (SURVIVED)? Nằm ở dòng nào, là loại mutant gì?
- Có **1 mutant sống sót** (SURVIVED) trên tổng số 9 mutant được sinh ra (Killed 8/9, đạt 89% mutation coverage).
- Mutant sống sót nằm ở **dòng 28** trong file `LoanService.java`:
  `if (loans.countActiveByMemberId(memberId) > 3)`
- Loại mutant: `ConditionalsBoundaryMutator` (`changed conditional boundary` - đột biến thay đổi biên điều kiện, cụ thể PIT đã đổi toán tử `>` thành `>=`).

**Lỗi cài sẵn nằm ở đâu? Bạn phát hiện ra nó bằng cách nào?**
- **Vị trí lỗi:** Dòng 28 trong `src/main/java/vn/edu/seminar/loan/LoanService.java`:
  `if (loans.countActiveByMemberId(memberId) > 3)`
- **Cách phát hiện:**
  - Nhờ báo cáo PIT Mutation Testing chỉ ra mutant thay đổi biên điều kiện (`changed conditional boundary`) ở dòng 28 sống sót mà không bị test suite phát hiện.
  - Đối chiếu với đặc tả nghiệp vụ ở mục 4: *"Mỗi thành viên được mượn tối đa 3 cuốn: đang mượn 3 cuốn thì không được mượn thêm -> LIMIT_REACHED"*. Nghĩa là khi một thành viên đang mượn 3 cuốn (`countActiveByMemberId == 3`), hệ thống phải từ chối mượn tiếp. Tuy nhiên trong code viết `> 3`, nên khi `count = 3`, điều kiện `3 > 3` cho kết quả `false` và hệ thống vẫn cho phép mượn cuốn thứ 4 (chỉ chặn khi đã mượn từ 4 cuốn trở lên).
  - Khi cập nhật test `daDatHanMuc` về đúng giá trị biên theo đặc tả là `thenReturn(3L)`, test chạy bị đỏ (FAIL), xác nhận chính xác lỗi nằm ở code nghiệp vụ trong `LoanService.java`.

**Bạn đã sửa test và code như thế nào?**
- **Đoạn test hoàn chỉnh trong `src/test/java/vn/edu/seminar/loan/LoanServiceTest.java`:**
```java
    // BÀI 2a: thành viên đang mượn đủ số cuốn tối đa thì bị từ chối với mã LIMIT_REACHED.
    // Gợi ý: làm giống các test ở trên (availableBook, member, when(loans.countActiveByMemberId(1))..., assertCode).
    @Test void daDatHanMuc() {
        availableBook(); member(BigDecimal.ZERO);
        when(loans.countActiveByMemberId(1)).thenReturn(3L);
        assertCode(LIMIT_REACHED);
        verify(books, never()).save(any());
    }

    // BÀI 2b: thành viên còn nợ phí thì bị từ chối với mã UNPAID_FINE.
    @Test void conPhiChuaTra() {
        availableBook(); member(BigDecimal.valueOf(10_000));
        when(loans.countActiveByMemberId(1)).thenReturn(1L);
        assertCode(UNPAID_FINE);
        verify(books, never()).save(any());
    }
```

- **Dòng code sửa trong `src/main/java/vn/edu/seminar/loan/LoanService.java` (dòng 28):**
```java
// Trước khi sửa (lỗi cài sẵn):
if (loans.countActiveByMemberId(memberId) > 3) {

// Sau khi sửa (đúng đặc tả nghiệp vụ):
if (loans.countActiveByMemberId(memberId) >= 3) {
```

**Kết quả PIT sau khi sửa** (Killed bao nhiêu / tổng bao nhiêu):
- Killed **9 / 9** (đạt **100%** mutation coverage, 0 mutant sống sót).

## Câu hỏi tổng kết
**Coverage 100% có nghĩa là code không còn lỗi không? Hãy giải thích dựa trên những gì bạn thấy trong lab.**
- **Không.** Coverage 100% (cả Line coverage lẫn Branch coverage) **không đồng nghĩa** với việc code không còn lỗi.
- **Giải thích dựa trên những gì thấy trong bài lab:**
  1. *Coverage chỉ đo lường đường thực thi (Execution Path), không đo lường tính đúng đắn của logic:*
     - Ở Bài 2a, khi viết test `daDatHanMuc` với `count = 4L`, test đã đi qua cả 2 nhánh (true và false) của câu lệnh `if`. JaCoCo ghi nhận đạt 100% branch coverage (6/6 branches) và Maven báo build thành công (`BUILD SUCCESS`).
     - Tuy nhiên, code tại thời điểm đó vẫn sai logic so với đặc tả: hệ thống vẫn cho phép người đang mượn 3 cuốn mượn thêm cuốn thứ 4 (do điều kiện sai `> 3`). Dù coverage đạt 100%, bài test hoàn toàn không phát hiện được lỗi này do không kiểm tra đúng giá trị biên.
  2. *Coverage không kiểm tra chất lượng của test assertions và test data:*
     - Một bài test chỉ cần chạm tới dòng code là được tính vào độ bao phủ, kể cả khi assertions sơ sài hoặc bộ dữ liệu test bỏ sót các trường hợp biên quan trọng.
  3. *Tầm quan trọng của Mutation Testing:*
     - Mutation Testing (như PIT) giúp kiểm tra độ "nhạy" và độ tin cậy của test suite bằng cách cố ý tiêm lỗi (mutants) vào mã nguồn. Chỉ khi test suite tiêu diệt được các mutant (như mutant thay đổi biên điều kiện từ `>` thành `>=`), ta mới có cơ sở tin cậy rằng test cases đang kiểm tra đúng và chặt chẽ đặc tả nghiệp vụ.
