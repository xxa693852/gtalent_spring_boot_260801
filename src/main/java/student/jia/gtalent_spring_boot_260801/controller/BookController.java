package student.jia.gtalent_spring_boot_260801.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import student.jia.gtalent_spring_boot_260801.entity.Book;
import student.jia.gtalent_spring_boot_260801.repository.BookRepository;
import student.jia.gtalent_spring_boot_260801.request.BookCreateRequest;
import student.jia.gtalent_spring_boot_260801.response.ApiResponse;
import student.jia.gtalent_spring_boot_260801.response.BookResponse;
import student.jia.gtalent_spring_boot_260801.response.PageResponse;
import student.jia.gtalent_spring_boot_260801.service.MailService;

import java.util.List;

/**
 * 處理書籍 API 的 Controller。
 * 練習 2：新增、修改、刪除成功後，會寄 Gmail 通知。
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository repository;
    private final MailService mailService;
    private final String notificationEmail;

    /**
     * Spring 會自動把需要的物件傳進建構子，稱為「建構子注入」。
     * notificationEmail 的值來自 application.properties；可由環境變數
     * BOOK_NOTIFICATION_TO 覆蓋，避免將真實信箱寫死在程式碼。
     */
    public BookController(
            BookRepository repository,
            MailService mailService,
            @Value("${app.mail.book-notification-to}") String notificationEmail) {
        this.repository = repository;
        this.mailService = mailService;
        this.notificationEmail = notificationEmail;
    }

    /** 取得書籍列表，支援分頁。 */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<BookResponse> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 限制頁碼與每頁筆數，避免不合理的查詢。
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 50) {
            size = 50;
        }

        List<BookResponse> bookResponses = repository.findAll(page, size).stream()
                // 不直接回傳 Entity，避免將 status、deletedAt 等欄位暴露給前端。
                .map(BookResponse::new)
                .toList();
        return new PageResponse<>(bookResponses, page, size, repository.countAll());
    }

    /** 依書籍編號查詢一筆資料。 */
    @GetMapping("/search-id/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Book getOneById(@PathVariable Long id) {
        return repository.findOneById(id);
    }

    /** 依書名關鍵字查詢資料。 */
    @GetMapping("/search-name/{name}")
    @ResponseStatus(HttpStatus.OK)
    public List<Book> getOneByName(@PathVariable String name) {
        return repository.findOneByName(name);
    }

    /** 新增書籍，成功後寄送 Gmail 通知。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse create(@Valid @RequestBody BookCreateRequest request) {
        // @Valid 會先驗證 request 的書名與價格；通過後才會建立 Book。
        Book createdBook = repository.create(new Book(request.getName(), request.getPrice()));

        // 資料庫新增成功後才寄信，信件內容使用新增後的書籍資料。
        mailService.sendEmail(
                notificationEmail,
                "書籍新增通知",
                "已新增書籍：ID=" + createdBook.getId()
                        + "，書名=" + createdBook.getName()
                        + "，價格=" + createdBook.getPrice());
        return new ApiResponse("新增書籍成功，已寄送 Gmail 通知");
    }

    /** 修改書籍，成功後寄送 Gmail 通知。 */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse update(@PathVariable Long id, @Valid @RequestBody BookCreateRequest request) {
        Book updatedBook = repository.update(id, new Book(request.getName(), request.getPrice()));

        // repository.update() 成功後，才通知收件人。
        mailService.sendEmail(
                notificationEmail,
                "書籍修改通知",
                "已修改書籍：ID=" + updatedBook.getId()
                        + "，書名=" + updatedBook.getName()
                        + "，價格=" + updatedBook.getPrice());
        return new ApiResponse("修改書籍成功，已寄送 Gmail 通知");
    }

    /** 軟刪除書籍，成功後寄送 Gmail 通知。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse delete(@PathVariable Long id) {
        // 目前的 repository.delete() 是軟刪除：資料仍在資料庫，狀態改為已刪除。
        repository.delete(id);

        mailService.sendEmail(
                notificationEmail,
                "書籍刪除通知",
                "已刪除書籍：ID=" + id);
        return new ApiResponse("刪除書籍成功，已寄送 Gmail 通知");
    }
}
