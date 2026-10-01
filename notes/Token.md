                 ┌───────────────┐
                 │   JWT Token   │
                 │ AAAAA / RRRRR │
                 └───────┬───────┘
                         │
                    SHA-256
                         ↓
                 ┌───────────────┐
                 │   AuthToken   │
                 │ Java Entity   │
                 └───────┬───────┘
                         │
                   Repository
                         ↓
                 ┌───────────────┐
                 │  auth_tokens  │
                 │   MySQL DB    │
                 └───────────────┘


JWT
真正的 Token。

AuthToken
Java 裡面代表「這組 Token 登入狀態」的 Entity。

auth_tokens
MySQL 裡真正存資料的資料表。

                    JwtService
                         │
        ┌────────────────┼────────────────┐
        ↓                ↓                ↓
      產生             解析              Hash
        │                │                │
        ↓                ↓                ↓
 Access Token         Claims          Token Hash
 Refresh Token        │
        │             │
        │       驗證 Signature
        │       驗證 Expiration
        │       取得 ownerId
        │       取得 tokenType
        │
        └───────────────┐
                        ↓
                 Access / Refresh


Claims 就是：

JWT 裡面的資料。
例如：
JWT
├─ ownerType = MEMBER
├─ ownerId = 10
├─ tokenType = ACCESS
├─ issuedAt = ...
└─ expiration = ...
這些資料就叫：
Claims
所以：
Claims claims = jwtService.parse(accessToken);
白話：
「把這顆 Token 拆開，拿出裡面的資料。」
接下來就可以：
claims.get("ownerId")
取得會員 ID。




正常流程其實是兩層驗證
這個你一定要記住：

              Access Token
                    ↓
          ┌─────────┴─────────┐
          ↓                   ↓
     JwtService          DB / Repository
          ↓                   ↓
     JWT 本身合法？       系統還允許嗎？
          ↓                   ↓
       Signature             revoked
       expiration            deletedAt
       tokenType             Token Hash
          └─────────┬─────────┘
                    ↓
              兩邊都 OK
                    ↓
                 放行

所以不是：
「JWT 驗證成功就結束。」
而是：
JWT 驗證 + 系統 Token 狀態確認。


先把幾個 Class 的工作分開
目前我們看到的幾個東西容易混在一起，所以先切乾淨：
MemberService
    ↓
「這個會員能不能登入？」

JwtService
    ↓
「Token 怎麼產生、怎麼驗證？」

AuthToken
    ↓
「這一組 Token 的資料長什麼樣？」

AuthTokenRepository
    ↓
「把 Token 狀態存進 / 查出 DB」

AuthInterceptor
    ↓
「每次 Request 進來時，先檢查 Token」

如果以後老師問：

Access Token 過期後，系統如何透過 Refresh Token 延續登入？

你可以回答：

Access Token 過期後，由 AuthInterceptor 發現，接著取得 Request 中的 Refresh Token，透過 JwtService 驗證 Refresh Token，再透過 AuthTokenRepository 確認 DB 中的 Token 狀態仍有效。驗證成功後撤銷舊 Refresh Token，產生新的 Access Token 與 Refresh Token，將新的 Token Hash 存入 DB，最後透過 Response 回傳新的 Token，讓使用者繼續保持登入。

這個程度就已經不是只會背「Access 短、Refresh 長」了，而是真的把你這個專案的雙 Token 流程串起來了。



把「登入 → 使用 → Refresh → 登出」全部串起來

現在我們已經可以畫完整了：

【登入】

MemberService
↓
JwtService
↓
A1 + R1
↓
AuthToken
↓
Repository
↓
auth_tokens
revoked = false

↓

【正常使用】

Request
↓
AuthInterceptor
↓
JwtService
↓
驗證 A1
↓
Repository
↓
確認 revoked = false
↓
Controller

↓

【Access 過期】

A1
↓
AuthInterceptor
↓
A1 過期
↓
R1
↓
JwtService 驗證
↓
Repository 查 DB
↓
R1 有效
↓
R1 撤銷
↓
產生 A2 + R2
↓
存新的 Token Hash
↓
Response

↓

【登出】

Logout Request
↓
Controller
↓
Service
↓
Repository
↓
revoked = true
↓
Response

↓

【之後再拿舊 Token】

A2
↓
AuthInterceptor
↓
JwtService
↓
JWT 本身可能還沒過期
↓
Repository
↓
revoked = true
↓
❌ 拒絕
十一、你現在已經可以回答一個很重要的問題
Q：為什麼 JWT 明明還沒過期，登出後卻不能使用？

你現在可以回答：

因為 JWT 的有效期限只是判斷 Token 本身是否過期，登出時後端會把對應的 AuthToken 標記為 revoked，之後 AuthInterceptor 驗證 Token 時除了確認 JWT 本身合法，也會透過 AuthTokenRepository 查詢 DB 的 Token 狀態，因此即使 JWT 尚未過期，只要 revoked = true，就會拒絕請求。
這個答案已經很完整了。