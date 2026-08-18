# 專案完善建議

本文件以目前的「書籍 CRUD + MySQL/Flyway + SMTP 寄信」實作為基礎，只提出建議，未變更既有程式碼或設定。

## 優先處理（正確性與對外行為）

1. **修正更新後回傳物件不正確的問題**
   `BookRepositoryImpl.update()` 更新的是 `existingBook`，卻回傳傳入的 `book`。因此 `BookController` 寄更新通知時，`updatedBook.getId()` 會是 `null`。應回傳受 JPA 管理且已更新的 `existingBook`，並以測試鎖定此行為。

2. **統一「查無資料」為 HTTP 404**
   `findOneById()` 使用 `getSingleResult()`；查無資料時會拋出 JPA 的例外，最後被通用例外處理成 400。`ResourceNotFoundException` 目前也回傳 400。建議所有不存在的書籍都回傳 404，並讓依 ID 查詢與更新、刪除的回應格式一致。

3. **避免用通用 400 掩蓋伺服器錯誤**
   `@ExceptionHandler(Exception.class)` 一律回應 400，會讓資料庫、SMTP 或程式錯誤被誤判為用戶輸入問題。建議：已知的輸入錯誤回 400/422、資源不存在回 404、未預期錯誤回 500，且伺服器端保留完整 log 與 request trace ID。

4. **讓 API 回傳 DTO，而非 JPA Entity**
   `GET /books/search-id/{id}` 與名稱搜尋直接回傳 `Book`，會暴露 `status`、`deletedAt` 等內部欄位，也與列表端的 `BookResponse` 不一致。建議讀取 API 都使用 DTO；未來欄位調整時較不會破壞前端契約。

5. **將寄信失敗與資料異動解耦**
   新增、更新、刪除在資料庫成功後同步寄信；SMTP 暫時失敗可能導致使用者收到錯誤，但資料其實已寫入。建議至少明確定義規則：資料成功即成功，通知改成非同步；較完整的做法是 transactional outbox + 重試機制，避免漏信或重複信。

## 資料存取與商業邏輯

1. **把交易控制放到 Service 層**
   Repository 目前手動建立、提交與回滾交易，程式重複且容易在例外路徑遺漏處理。可建立 `BookService`，用 `@Transactional` 管理新增、更新、刪除；Repository 專注查詢與持久化。

2. **善用 Spring Data JPA 與型別安全查詢**
   可考慮由 `JpaRepository` / `Pageable` 取代手寫 native SQL、`List<?>` 與手動轉型。這會降低 SQL 字串錯誤，並提供分頁、排序、Optional 查詢等標準能力。若保留原生 SQL，應集中 SQL、明確指定傳回型別並補足測試。

3. **補強軟刪除模型**
   目前用 `status` 的 `Byte` 表示啟用/刪除，語意不夠清楚。建議使用 `boolean deleted` 或 enum，並以常數取代 `0`、`1`。所有查詢（包含未來的計數、搜尋、關聯資料）都必須一致排除已刪除資料。

4. **新增適當資料庫索引與約束**
   目前常以 `status` 篩選、以 `id` 排序及以名稱搜尋。可依實際資料量與查詢計畫評估 `(status, id)` 複合索引；名稱若要支援高效率模糊搜尋，需另評估前綴搜尋、全文索引或搜尋服務。也可規定價格上限、名稱長度與是否允許重名。

5. **定義金額資料型別**
   `Integer price` 適合「整數元」。若日後可能有小數、幣別或稅額，應改用 `BigDecimal`，並在資料庫設定 precision/scale 與統一的四捨五入規則。

## API 設計與驗證

1. **調整查詢路由與分頁規格**
   `search-id`、`search-name` 可改為較一致的資源設計，例如 `GET /books/{id}` 與 `GET /books?name=...&page=...&size=...`。對 page、size 建議使用 Bean Validation 回傳明確錯誤，而不是靜默改成預設值；並可支援受限的 `sort` 欄位。

2. **統一成功與失敗回應格式**
   現在有 `ApiResponse`、`Book`、`BookResponse`、字串回應並存，`ResponseMessages` 看似使用代碼但 API 未回傳代碼。建議定義固定欄位，例如 `code`、`message`、`data`、`errors`、`timestamp`、`traceId`，並讓 `/mail/send` 同樣遵守。

3. **補足輸入限制與正規化**
   書名已有必填驗證，建議加上最大長度、trim、禁止純控制字元；名稱搜尋應限制長度，且要考慮 `%`、`_` 作為 LIKE 萬用字元時的預期行為。路徑參數 ID 也可驗證必須為正整數。

4. **加入 OpenAPI 文件與範例**
   提供 Swagger/OpenAPI，可清楚列出路由、請求/回應 JSON、HTTP 狀態碼與錯誤碼。再附上 curl 或 Postman collection，能讓測試與交接更順暢。

## 測試與品質守門

1. **補上端對端 API 測試**
   現有測試只有 `contextLoads()`。至少涵蓋：新增成功與驗證失敗、分頁邊界、依 ID 查無資料、更新後資料與 email ID、軟刪除後不可查詢、重複刪除、錯誤 HTTP method，以及 SMTP 失敗時的既定策略。

2. **使測試可獨立重複執行**
   測試目前依賴外部 MySQL 環境變數與 repeatable seed SQL，容易受本機資料殘留影響。建議用 Testcontainers 啟動隔離的 MySQL，或至少使用專屬測試資料庫、每次清理資料與驗證 Flyway migration。寄信端應 mock `JavaMailSender`，避免測試真的連 SMTP。

3. **建立 CI 流程**
   專案目前沒有可見的 GitHub Actions workflow。建議每個 PR 執行 Maven test、程式格式檢查、靜態分析與依賴弱點掃描；合併前以這些檢查作為品質門檻。

4. **加入程式風格與靜態分析**
   可選用 Spotless/Checkstyle、SpotBugs、PMD 或 SonarQube，先固定格式、未使用程式碼、空值風險與例外處理等基本規則。這也能避免目前註解編碼或格式不一致的問題再次出現。

## 設定、安全性與維運

1. **區分 development、test、production profile**
   建議分出 `application-dev.properties`、`application-test.properties`、`application-prod.properties`。生產環境關閉 `spring.jpa.show-sql` 與格式化 SQL，並明確設定資料庫連線池、逾時、日誌等級。

2. **完善環境變數範本**
   `.env.example` 尚未列出 `DB_URL`、`TEST_DB_URL` 與 `BOOK_NOTIFICATION_TO`。補齊全部可覆寫設定、提供無敏感資料的範例與啟動說明，可減少新環境設定錯誤。

3. **保護寄信 API**
   `POST /mail/send` 若直接對外開放，可能被濫用為寄信跳板。應至少加入認證與授權、收件人網域或白名單限制、頻率限制、內容長度限制，以及寄送稽核紀錄；外部 API 建議使用 Spring Security。

4. **加入健康檢查、監控與結構化日誌**
   建議使用 Spring Boot Actuator（health、metrics），在 log 以 JSON 或固定欄位記錄 request ID、路徑、狀態碼、耗時與例外；避免寫入密碼、SMTP token 或完整個資。可再串接監控與告警。

5. **整理專案文件與授權資訊**
   `pom.xml` 的 description、URL、license、developer、SCM 欄位仍是空白，且根目錄沒有 README。建議補上：專案用途、Java/Maven/MySQL 前置需求、環境變數、建庫與 migration、啟動與測試指令、API 文件位置、寄信設定與已知限制。

## 建議實作順序

1. 先修正更新回傳 ID、404/500 狀態碼與 DTO 回傳，並為它們補測試。
2. 將交易移到 Service、決定寄信失敗策略，建立可重複的整合測試環境。
3. 統一 API 契約、補 OpenAPI 與 README。
4. 最後補 CI、監控、安全控管與資料庫效能調校。
