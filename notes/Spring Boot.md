Controller
「收到 HTTP、轉交工作、回傳 HTTP」

Service
「決定這件事情合不合理、業務邏輯怎麼跑」

Repository
「負責跟 DB 拿資料 / 修改資料」

Entity
「Java 世界裡代表資料的物件」

Response
「決定最後要讓使用者看到哪些資料」

Exception
「發生問題時，把錯誤帶出去」

Exception Handler
「把 Exception 統一轉成 HTTP 錯誤回應」

Request
→ 負責接收

Entity
→ 負責代表資料

Response
→ 負責輸出

Service
→ 負責業務邏輯

Repository
→ 負責資料存取

DTO核心一句話
Request 是「我要讓使用者提供什麼」，Entity 是「資料庫要記錄什麼」，Response 是「我要讓使用者看到什麼」。




若傳入的資料錯誤的話((新增書籍失敗)
使用者
↓
HTTP Request
↓
Controller
↓
Service
↓
驗證 / 判斷
↓
發現錯誤
↓
Constant（取得錯誤訊息）
↓
Exception
↓
Exception Handler
↓
HTTP Response
↓
使用者


若傳入的資料正確的話(新增書籍成功)
使用者
↓
HTTP Request
↓
Controller
↓
Service
↓
驗證 / 判斷
↓
建立 Book Entity
↓
Repository
↓
DB
↓
新增成功
↓
Repository
↓
Book Entity
↓
Service
↓
建立 / 整理 BookResponse(回覆結果，但只顯示適合丟給使用者看的資料，排除像建立時間或者是其他不適合印給前端的資料)
↓
Controller
↓
HTTP Response
↓
使用者



新增書籍流程(簡化版)
Request
↓
Controller
↓
Service
↓
建立 Entity
↓
Repository
↓
DB


查詢書籍流程(簡化版)
Service
 ↓
Repository
 ↓
DB
 ↓
資料
 ↓
Repository(Repository 使用 JPA 把資料庫資料取得後，得到一個 Book Entity)
 ↓
Book Entity(代表資料庫中的 Book 資料)
 ↓
Service
 ↓
Response


查詢書籍流程(完整版)
                   使用者
                      │
                      ↓
                HTTP Request
                      │
                      ↓
                 Controller
                      │
                      ↓
                   Service
                      │
              ┌───────┴───────┐
              │               │
           驗證/判斷        業務邏輯
              │               │
              └───────┬───────┘
                      ↓
                 Repository
                      │
                      ↓
                 Database(DB資料庫)
                      │
                      ↓
                Repository((Repository 使用 JPA 把資料庫資料取得後，得到一個 Book Entity)
                      │
                      ↓
                Book Entity
                      │
                      ↓
                   Service
                      │
                      ↓
                Book Response(只顯示適合傳給前端的資料) 
                      │
                      ↓
                 Controller
                      │
                      ↓
                HTTP Response
                      │
                      ↓
                   使用者



後半部分的簡化理解
Book Response
↓
Controller
↓
HTTP Response


後半部分的精確理解
Service
↓
Book Response
↓
Controller
↓
return Book Response
↓
Spring
↓
HTTP Response
↓
使用者


刪除書籍(不回傳的版本(僅僅只印出刪除成功))
使用者
↓
HTTP Request
DELETE /books/5
↓
Controller
↓
Service
↓
Repository
↓
DB
↓
找到 ID=5
↓
執行軟刪除
例如 status = 0
↓
Repository
↓
Service
↓
Controller
↓
HTTP Response
↓
使用者

刪除書籍(回傳書籍資訊的版本)
使用者
↓
HTTP Request
↓
Controller
↓
Service
↓
Repository
↓
DB
↓
軟刪除
↓
Repository
↓
Service
↓
建立 Book Response
↓
Controller
↓
HTTP Response
↓
使用者


刪除書籍(書籍不存在)
使用者
↓
HTTP Request
例如 DELETE /books/5
↓
Controller
↓
Service
↓
Repository
↓
DB
↓
查不到 ID=5 的書籍
↓
Repository 將「查不到」的結果交給 Service
↓
Service 判斷
「這本書不存在」
↓
取得 Constant 的錯誤訊息
「找不到書籍」
↓
拋出 Exception
↓
Exception Handler
↓
整理成錯誤 Response
↓
HTTP Response
↓
使用者


修改書籍(修改成功)
使用者
↓
HTTP Request
↓
Controller
↓
Service
↓
Repository
↓
DB 查找 ID=5
↓
取得 Book Entity
↓
修改 Book Entity
↓
Repository
↓
DB 儲存修改
↓
Service
↓
Book Response（如果 API 需要）
↓
Controller
↓
HTTP Response
↓
使用者


修改書籍(ID不存在)      
使用者
↓
HTTP Request
↓
Controller
↓
Service
↓
Repository
↓
DB 查詢 ID=999
↓
找不到
↓
Repository
↓
Service
↓
判斷書籍不存在
↓
Exception
↓
Constant 提供錯誤訊息
↓
Exception Handler
↓
HTTP Response
↓
使用者


修改書籍(書名空白)
使用者
↓
HTTP Request
↓
Controller
↓
驗證(這裡的驗證可能是Request DTO 就先驗證了，如果沒有@NotBlank，則會由後續Service進行判斷/驗證)
↓
發現 title 是空白
↓
Exception
↓
Constant 提供錯誤訊息
↓
Exception Handler
↓
HTTP Response
↓
使用者


驗證錯誤流程

使用者
↓
HTTP Request
↓
Controller
↓
Request
↓
Validation
│
├── ❌ 基本資料錯誤
│      ↓
│   錯誤處理
│      ↓
│   HTTP Response
│
└── ✅ 基本格式正確
       ↓
     Service
       ↓
   業務規則判斷
       │
       ├── ❌ account 已存在
       │      ↓
       │   Exception
       │      ↓
       │   Exception Handler
       │      ↓
       │   HTTP Response
       │
       └── ✅ account 沒問題
              ↓
           Entity
              ↓
         Repository
              ↓
             DB


找不到ID=999的使用者
使用者
↓
HTTP Request
↓
Controller
↓
Service
↓
Repository
↓
DB
↓
Repository
↓
Service
↓
Exception(Exception 裡帶著錯誤資訊，這裡的錯誤訊息主要是存放在Constant，他是存放錯誤訊息的來源)
↓
Exception Handler
↓
HTTP Response
↓
使用者



