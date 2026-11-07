# Codex 變更紀錄

本檔案用來記錄 Codex 對此專案實際造成的檔案變更，方便追蹤、檢閱與交接。

## 維護規則

- Codex 每次新增、修改或刪除專案檔案後，必須在同一回合同步更新本檔案。
- 每筆紀錄須包含日期、變更檔案、變更內容與驗證方式；若沒有執行驗證，必須明確註明。
- 僅記錄 Codex 的變更，不將使用者既有或自行進行的工作區變更歸屬為 Codex。
- 純讀取、分析、搜尋與未造成檔案異動的操作無須記錄。
- 每次使用者提出問題時，Codex 必須在 `QuestionDialogueRecord.md` 追加問題原文與回覆摘要。

## 變更紀錄

### 2026-08-15

- 檔案：`notes/adervice.md`
- 內容：新增專案完善建議，涵蓋 API 正確性、資料存取與交易、寄信可靠性、測試、CI、設定安全與維運，並附上建議實作順序。
- 驗證：確認檔案已建立且內容可讀；未執行建置或測試，因本次僅新增文件。

### 2026-08-15

- 檔案：`AGENT.md`
- 內容：新增 Codex 變更紀錄與後續同步維護規則。
- 驗證：確認檔案已建立；未執行建置或測試，因本次僅新增文件。

### 2026-08-15

- 檔案：`QuestionDialogueRecord.md`、`AGENT.md`
- 內容：建立使用者問題與回覆摘要紀錄檔，並新增每次提問都要同步記錄的規則。
- 驗證：確認兩個文件已更新；未執行建置或測試，因本次僅新增與更新文件。

### 2026-11-03
- 檔案：src/main/java/student/jia/gtalent_spring_boot_260801/deploy.yml、QuestionDialogueRecord.md、AGENT.md
- 內容：修正部署檔四處 SSH/SCP 連線變數及三處 JAR 備份、啟用與回復指令。
- 驗證：核對七處字串替換；未執行部署或建置。

### 2026-11-07
- 檔案：src/main/java/student/jia/gtalent_spring_boot_260801/controller/PythonController.java、QuestionDialogueRecord.md、AGENT.md
- 內容：補齊 GET /calculate，使用 py 執行專案外 D:/Java26/python/calculate.py，傳入 10 與 20，讀取輸出並檢查退出碼，以 application/json 回傳結果。
- 驗證：mvnw.cmd -q -DskipTests compile 成功。使用者已確認腳本輸出 sum=30、multiply=200。尚未啟動完整 Spring Boot 驗證 HTTP。
### 2026-11-07
- 檔案：src/main/java/student/jia/gtalent_spring_boot_260801/controller/PythonController.java、QuestionDialogueRecord.md、AGENT.md
- 內容：依使用者要求，將兩個 @Value 的預設腳本路徑改為 D:/Java26/python/calculate.py 與 D:/Java26/python/call python.py。
- 驗證：確認 application.properties 已設定相同腳本路徑與 python.executable=py。僅修改預設字串，未重新編譯或測試人臉辨識 API。
### 2026-11-07
- 檔案／環境：QuestionDialogueRecord.md、AGENT.md；py 使用的 Python 3.14 環境。
- 內容：修正人臉辨識腳本缺少 cv2，安裝 opencv-python 與 numpy。初次安裝的 OpenCV 5.0 缺少 CascadeClassifier，改裝 opencv-python<5，最終版本 4.14.0.94。
- 驗證：cv2 載入成功，人臉模型載入成功。以 Downloads/1.jpg 測試 POST /api/python/face-detect，HTTP 200，回傳 faceCount=0、faces=[]。