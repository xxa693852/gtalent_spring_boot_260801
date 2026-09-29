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