# Lab Nhóm 09: Unit Test · Coverage · Mutation Testing

Lab dùng lại code của case study **quản lý thư viện** mà nhóm trình bày. Thời gian làm khoảng **30 phút**, gồm 2 bài. Hai bài **đều đang chạy đỏ**: bạn lấp các chỗ trống cho build chạy xanh, tìm ra một lỗi nhóm đã cài sẵn trong code, rồi nộp báo cáo.

## 0. Chuẩn bị (5 phút)

**Yêu cầu:** JDK **17 trở lên** (17, 21 hoặc 25 đều được) và Git. Không cần cài Maven, vì repo có sẵn Maven Wrapper (`mvnw`).

```bash
java -version
```

1. Clone repo về máy:
   ```bash
   git clone https://github.com/tzin1401/seminar-lab-nhom09.git
   cd seminar-lab-nhom09
   ```
2. Chạy thử:
   ```bash
   ./mvnw clean verify
   ```

   Trên Windows (cmd/PowerShell), thay `./mvnw` bằng `mvnw.cmd` ở mọi lệnh.

Lần chạy đầu Maven sẽ tải thư viện về, mất 1–2 phút. Kết quả phải là **`BUILD FAILURE`**: điều này là bình thường, vì lab cố ý bắt đầu ở trạng thái đỏ.

## Đặc tả nghiệp vụ

**Tính phí trả trễ** (`FineCalculator`):

- Hạng NORMAL: 2.000đ cho mỗi ngày trễ, không có ngày ân hạn.
- Hạng VIP: được ân hạn 2 ngày đầu (không tính phí), từ ngày thứ 3 tính 1.000đ/ngày.
- Phí tối đa là **50.000đ**, dù trễ bao nhiêu ngày.

**Mượn sách** (`LoanService.borrow`): hệ thống kiểm tra theo thứ tự sau, gặp điều kiện nào không thỏa thì ném `LoanException` với mã tương ứng:

| # | Điều kiện                                                                                                       | Mã lỗi khi vi phạm |
| - | ------------------------------------------------------------------------------------------------------------------ | --------------------- |
| 1 | Sách phải tồn tại                                                                                              | `BOOK_NOT_FOUND`    |
| 2 | Sách phải còn trên kệ                                                                                         | `BOOK_UNAVAILABLE`  |
| 3 | Thành viên phải tồn tại                                                                                       | `MEMBER_NOT_FOUND`  |
| 4 | Mỗi thành viên được mượn **tối đa 3 cuốn**: đang mượn 3 cuốn thì không được mượn thêm | `LIMIT_REACHED`     |
| 5 | Thành viên không được còn nợ phí                                                                          | `UNPAID_FINE`       |

Qua hết 5 bước thì hệ thống lưu phiếu mượn và đánh dấu sách là đã được mượn.

## Bài 1: Giá trị biên và test data-driven (10 phút)

Mở file `src/test/resources/fine-cases.csv`. Mỗi dòng là **một test case** gồm: số ngày trễ, hạng thành viên và số tiền mong đợi. Test `FineCalculatorTest.tinhPhiTheoDuLieu` đọc file này và chạy một lần cho mỗi dòng.

1. Thay mỗi dấu `?` bằng số tiền đúng **theo đặc tả ở trên**. Tự tính tay, không được chạy code để lấy kết quả.
2. Chạy lại chỉ test của bài này:
   ```bash
   ./mvnw test -Dtest=FineCalculatorTest
   ```
3. Dòng nào còn đỏ nghĩa là số bạn tính đang lệch với code. Đọc lại đặc tả để tìm chỗ tính sai.

**Câu hỏi cho báo cáo:** vì sao file CSV chọn các ngày 24, 25, 26 (NORMAL) và 2, 3, 51, 52, 53 (VIP) mà không chọn, ví dụ, ngày 10?

## Bài 2: Coverage 100% có phải là hết lỗi? (20 phút)

### 2a. Lấp test cho đủ coverage

```bash
./mvnw clean verify
```

Build sẽ báo lỗi: `branches covered ratio is 0.66, but expected minimum is 1.00`. Đây là **coverage gate**: build chỉ xanh khi **mọi nhánh** trong `LoanService` đều được test chạy qua.

1. Mở báo cáo coverage `target/site/jacoco/index.html` bằng trình duyệt, vào `vn.edu.seminar.loan` → `LoanService` → `borrow`.
   - Dòng **vàng** (hình thoi vàng): mới có một phía của câu `if` được test chạy qua.
   - Dòng **xanh**: cả hai phía đều đã được chạy qua.
2. Mở `src/test/java/vn/edu/seminar/loan/LoanServiceTest.java` và viết 2 test đang để `// TODO`: `daDatHanMuc` và `conPhiChuaTra`. Có thể làm theo mẫu của các test sẵn có ở phía trên.
3. Chạy lại `./mvnw clean verify` cho tới khi thấy `All coverage checks have been met` và `BUILD SUCCESS`.

### 2b. Cho code bị "đột biến" xem test có bắt được không

Coverage chỉ cho biết **dòng code nào đã chạy**, không cho biết test **có kiểm tra đúng kết quả** hay không. PIT là công cụ tự sửa code thành nhiều phiên bản sai (gọi là **mutant**), ví dụ đổi `>` thành `>=`, rồi chạy lại toàn bộ test. Nếu test vẫn xanh trên một phiên bản sai, nghĩa là test chưa đủ mạnh để phát hiện thay đổi đó.

```bash
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage
```

Mở `target/pit-reports/index.html` → `LoanService.java`. Dòng nào **đỏ** là dòng có mutant **SURVIVED** (sống sót, tức là test không phát hiện ra).

1. Nếu tất cả mutant đều bị diệt (KILLED) thì rất tốt. Hãy kiểm tra lại xem test của bạn có thực sự khớp với đặc tả không.
2. Nếu có mutant sống sót: đọc mutant đó là gì, đối chiếu với **đặc tả**, sửa test để kiểm đúng giá trị biên. Nếu test mới chạy đỏ, hãy suy nghĩ: lỗi nằm ở **test** hay ở **code**?
3. Khi đã tìm ra, sửa lỗi trong code, chạy lại `./mvnw clean verify` và PIT cho tới khi cả hai đều xanh.

> Gợi ý: trong `src/main` có đúng **một lỗi** nhóm đã cài sẵn.

## Nộp bài

Chỉ nộp **file báo cáo**, không cần push code hay nộp link repo.

**Quy tắc đặt tên:** `StudentID_ExerciseID.zip`, trong đó `ExerciseID` là **`UnitTesting`**. Ví dụ MSSV `23120001` thì nộp file `23120001_UnitTesting.zip`.

1. Điền đầy đủ file `REPORT.md`. Ở phần 2b, **dán luôn đoạn test và dòng code bạn đã sửa**, vì nhóm chỉ chấm qua báo cáo.
2. Đổi tên `REPORT.md` thành `<MSSV>_UnitTesting.md`, ví dụ `23120001_UnitTesting.md`. Có thể đổi tên trong VS Code: chuột phải vào file → Rename.
3. Nén file đó thành `<MSSV>_UnitTesting.zip`:
   ```bash
   # macOS / Linux
   zip 23120001_UnitTesting.zip 23120001_UnitTesting.md
   # Windows (PowerShell)
   Compress-Archive 23120001_UnitTesting.md 23120001_UnitTesting.zip
   ```
   Hoặc chuột phải vào file → Compress (macOS) / Send to → Compressed (zipped) folder (Windows), rồi đổi tên file zip cho đúng quy tắc.
4. Nộp file `<MSSV>_UnitTesting.zip`.

**Chấm điểm (10đ):** Bài 1 được 3đ, 2a được 3đ, 2b (tìm và sửa được lỗi) được 3đ, câu hỏi tổng kết được 1đ.
