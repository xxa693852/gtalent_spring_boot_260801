# 問題與對話紀錄

本檔案記錄使用者提出的問題，以及 Codex 的回覆摘要。

## 2026-08-15

### 問題

以後我詢問的任何問題，請都幫我記錄下來到 `QuestionDialogueRecord.md`。

### 回覆摘要

已建立此檔案。往後每次使用者提問時，Codex 會追加記錄問題與回覆摘要。

### 2026-11-03
- 問題：修正暫放於 Java 原始碼目錄的 deploy.yml 錯誤。
- 回覆：修正七處連線變數與檔案操作指令；檔案保留原位置，未啟用 GitHub Actions。

### 2026-11-07
- 問題：已在終端機成功執行 calculate.py，提供 PythonController.java 路徑以繼續串接。
- 回覆摘要：補齊 Controller 的 GET /calculate，採用已驗證可用的 py 與外部腳本路徑，固定傳入 10、20，回傳 JSON。Maven 編譯成功，請啟動 Spring Boot 後透過 /calculate 驗證 HTTP。
### 2026-11-07
- 問題：將 PythonController 的兩個 @Value 腳本預設路徑改為本機路徑。
- 回覆摘要：已替換為 D:/Java26/python/calculate.py 與 D:/Java26/python/call python.py，並核對現有 application.properties 設定一致。
### 2026-11-07
- 問題：呼叫 call python.py 出現 ModuleNotFoundError: No module named cv2。
- 回覆摘要：安裝相容的 OpenCV 4.x，確認模型能載入並實測 API 成功回傳 HTTP 200。測試圖片是課堂截圖，結果未偵測到人臉。