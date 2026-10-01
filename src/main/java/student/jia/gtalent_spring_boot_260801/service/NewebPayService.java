package student.jia.gtalent_spring_boot_260801.service;



import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLDecoder;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.NoResultException;
import student.jia.gtalent_spring_boot_260801.constant.NotifyStatus;
import student.jia.gtalent_spring_boot_260801.constant.OrderStatus;
import student.jia.gtalent_spring_boot_260801.constant.PaymentStatus;
import student.jia.gtalent_spring_boot_260801.constant.ResponseMessages;
import student.jia.gtalent_spring_boot_260801.entity.Book;
import student.jia.gtalent_spring_boot_260801.entity.BookOrder;
import student.jia.gtalent_spring_boot_260801.entity.Payment;
import student.jia.gtalent_spring_boot_260801.entity.PaymentNotification;
import student.jia.gtalent_spring_boot_260801.exception.ResourceNotFoundException;
import student.jia.gtalent_spring_boot_260801.response.NewebPayPaymentFormResponse;
import student.jia.gtalent_spring_boot_260801.repository.BookOrderRepository;
import student.jia.gtalent_spring_boot_260801.repository.BookRepository;
import student.jia.gtalent_spring_boot_260801.repository.PaymentRepository;
import student.jia.gtalent_spring_boot_260801.repository.PaymentNotificationRepository;



@Service
public class NewebPayService {

    private final PaymentRepository paymentRepository;
    private final BookOrderRepository bookOrderRepository;
    private final BookRepository bookRepository;
    private final PaymentNotificationRepository paymentNotificationRepository;
    private final String merchantId;
    private final String hashKey;
    private final String hashIv;
    private final String version;
    private final String gatewayUrl;
    private final String queryUrl;
    private final String notifyUrl;
    private final String returnUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // 抓取application.properties裡的藍新金流設定
    public NewebPayService(
            PaymentRepository paymentRepository,
            BookOrderRepository bookOrderRepository,
            BookRepository bookRepository,
            PaymentNotificationRepository paymentNotificationRepository,
            @Value("${newebpay.merchant-id}") String merchantId,
            @Value("${newebpay.hash-key}") String hashKey,
            @Value("${newebpay.hash-iv}") String hashIv,
            @Value("${newebpay.version}") String version,
            @Value("${newebpay.gateway-url}") String gatewayUrl,
            @Value("${newebpay.query-url}") String queryUrl,
            @Value("${newebpay.notify-url}") String notifyUrl,
            @Value("${newebpay.return-url}") String returnUrl,
            ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.bookOrderRepository = bookOrderRepository;
        this.bookRepository = bookRepository;
        this.paymentNotificationRepository = paymentNotificationRepository;
        this.merchantId = merchantId;
        this.hashKey = hashKey;
        this.hashIv = hashIv;
        this.version = version;
        this.gatewayUrl = gatewayUrl;
        this.queryUrl = queryUrl;
        this.notifyUrl = notifyUrl;
        this.returnUrl = returnUrl;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public NewebPayPaymentFormResponse createPaymentForm(Long paymentId) {
        // 產生付款表單前先確認必要設定都有填。
        validateConfig();

        Payment payment = paymentRepository.findById(paymentId)
                            .orElseThrow(() -> new ResourceNotFoundException("payment", ResponseMessages.RESOURCE_NOT_FOUND));

        BookOrder order = bookOrderRepository.findById(payment.getOrderId())
                            .orElseThrow(() -> new ResourceNotFoundException("order", ResponseMessages.RESOURCE_NOT_FOUND));
        
        Book book = findActiveBook(order.getBookId());

        // 組合字串為url
        String url = buildTradeInfo(payment, book);

        // url執行 AES-256-CBC (使用 PKCS7 填充)，並將結果轉換至十六進制
        String tradeInfo = encryptTradeInfo(url);
        
        // 組合字串為hashs
        String hashs = "HashKey=" + hashKey + "&" + tradeInfo + "&HashIV=" + hashIv;

        // 轉成大寫且加密sha256
        String tradeSha = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            tradeSha =  toHex(digest.digest(hashs.getBytes(StandardCharsets.UTF_8))).toUpperCase();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }

        // 表單資料已產生後，付款進入等待使用者完成付款與等待藍新回呼的階段。
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTradeSha(tradeSha);
        paymentRepository.save(payment);

        return new NewebPayPaymentFormResponse(gatewayUrl, merchantId, version, tradeInfo, tradeSha);
    }

    @Transactional
    public String handleNotify(String rowBody) {
        // NotifyURL 第一件事是保存原始資料。
        // 即使後續解密、驗章、更新狀態失敗，也要留下藍新實際送來的內容，方便查帳。
        PaymentNotification notification = new PaymentNotification(rowBody);
        paymentNotificationRepository.save(notification);

        try {
            Map<String, String> formParams = parseFormBody(rowBody);

            // 檢查藍新回傳的 Status 是否為 SUCCESS，若不是就直接回傳 ERROR。
            if(!formParams.get("Status").equals("SUCCESS")){
                notification.setNotifyStatus("FAILED");
                notification.setErrorMessage("藍新回傳的 Status 不是 SUCCESS");
                paymentNotificationRepository.save(notification);
                return "ERROR";
            }

            validateConfig();   

            // 先檢查藍新送來的 MerchantID 是否正確，避免被人偽造。
            if (!formParams.get("MerchantID").equals(merchantId)) {
                notification.setNotifyStatus("FAILED");
                notification.setErrorMessage("藍新回傳的 MerchantID 不正確");
                paymentNotificationRepository.save(notification);
                return "ERROR";
            }

            // 先檢查藍新送來的 TradeSha 是否正確，避免被人偽造。
            String hostTradeSha = generateTradeSha(formParams.get("TradeInfo"));
            if (!hostTradeSha.equals(formParams.get("TradeSha"))) {
                notification.setNotifyStatus("FAILED");
                notification.setErrorMessage("藍新回傳的 TradeSha 不正確");
                paymentNotificationRepository.save(notification);
                return "ERROR";
            }

            // 上面已經驗證成功 所以做一個標記，表示這筆通知已經驗證過了。
            notification.setVerified((byte) 1);

            String decryptedTradeInfo = decryptTradeInfo(formParams.get("TradeInfo"));
            // 將解密後的 TradeInfo 解析成 Map，方便後續依欄位名稱取值。
            Map<String, String> decryptedTradeInfoParams = parseFormBody(decryptedTradeInfo);
            String tradeInfoStatus = decryptedTradeInfoParams.get("Status");
            String tradeInfoMessage = decryptedTradeInfoParams.get("Message");
            String merchantOrderNo = decryptedTradeInfoParams.get("MerchantOrderNo");
            String tradeNo = decryptedTradeInfoParams.get("TradeNo");
            String paymentMethod = decryptedTradeInfoParams.get("PaymentMethod");
            String tradeInfoRespondCode = decryptedTradeInfoParams.get("RespondCode");

            notification.setMerchantOrderNo(merchantOrderNo);
            notification.setProviderTradeNo(tradeNo);

            // 資料庫找出付款單號
            Payment payment = paymentRepository.findByMerchantOrderNo(merchantOrderNo)
                    .orElseThrow(() -> new ResourceNotFoundException("payment", ResponseMessages.RESOURCE_NOT_FOUND));

            // 檢查付款單狀態是否為 PENDING，若不是就直接回傳 ERROR。
            if(!PaymentStatus.PENDING.equals(payment.getPaymentStatus())) {
                notification.setNotifyStatus("FAILED");
                notification.setErrorMessage("付款單狀態不是 PENDING，無法更新");
                paymentNotificationRepository.save(notification);
                return "ERROR";
            }

            // 回寫付款單ID到通知紀錄，方便查帳。
            notification.setPaymentId(payment.getId());

            BookOrder order = bookOrderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("order", ResponseMessages.RESOURCE_NOT_FOUND));

            // 回寫藍新回傳的 TradeNo、付款方式、回傳代碼、回傳訊息到付款單，方便查帳。
            payment.setProviderTradeNo(tradeNo);
            payment.setPaymentMethod(paymentMethod);
            payment.setReturnCode(tradeInfoRespondCode);
            payment.setReturnMessage(tradeInfoMessage);

            // 更改付款單與訂單狀態，若藍新回傳的 Status 是 SUCCESS 就改成 PAID，否則改成 FAILED。
            if ("SUCCESS".equalsIgnoreCase(tradeInfoStatus)) {
                LocalDateTime paidAt = LocalDateTime.now();
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setPaidAt(paidAt);
                order.setOrderStatus(OrderStatus.PAID);
                order.setPaidAt(paidAt);
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                order.setOrderStatus(OrderStatus.FAILED);
            }

            // 訂單跟付款單儲存
            paymentRepository.save(payment);
            bookOrderRepository.save(order);

            // 更新通知紀錄的狀態為 PROCESSED，並記錄處理時間。
            notification.setNotifyStatus(NotifyStatus.PROCESSED);
            notification.setProcessedAt(LocalDateTime.now());
            paymentNotificationRepository.save(notification);

            return "OK";
        } catch (Exception exception) {
            return "ERROR";
        }
    }

    /**
     * 查詢單筆藍新交易並同步本機狀態。訂單必須屬於目前登入者，且金額固定取自資料庫。
     */
    @Transactional
    public String queryAndSyncTradeStatus(String merchantOrderNo, Long buyerMemberId) {
        if (merchantOrderNo == null || merchantOrderNo.isBlank()) {
            throw new IllegalArgumentException("merchantOrderNo is required");
        }

        Payment payment = paymentRepository.findByMerchantOrderNo(merchantOrderNo)
                .orElseThrow(() -> new ResourceNotFoundException("payment", ResponseMessages.RESOURCE_NOT_FOUND));
        BookOrder order = bookOrderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("order", ResponseMessages.RESOURCE_NOT_FOUND));
        if (!Objects.equals(order.getBuyerMemberId(), buyerMemberId)) {
            throw new ResourceNotFoundException("payment", ResponseMessages.RESOURCE_NOT_FOUND);
        }
        return queryAndSyncPayment(payment, order);
    }

    /** 書籍頁重整時，只同步超過 30 分鐘仍在購買中的訂單。 */
    @Transactional
    public List<String> syncExpiredPendingTrades(Long buyerMemberId) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<String> statuses = new ArrayList<>();
        for (BookOrder order : bookOrderRepository
                .findByBuyerMemberIdAndOrderStatusAndCreatedAtLessThanEqual(
                        buyerMemberId, OrderStatus.PENDING_PAYMENT, cutoff)) {
            paymentRepository.findByMerchantOrderNo(order.getOrderNo())
                    .filter(payment -> PaymentStatus.PENDING.equals(payment.getPaymentStatus()))
                    .ifPresent(payment -> {
                        try {
                            statuses.add(queryAndSyncPayment(payment, order));
                        } catch (RuntimeException exception) {
                            // 單筆藍新暫時失敗不影響其他訂單；該筆保留 PENDING，下次重整再查。
                            statuses.add("QUERY_ERROR");
                        }
                    });
        }
        return statuses;
    }

    /** 實際呼叫藍新 QueryTradeInfo，並依 TradeStatus 更新 payments 與 book_orders。 */
    private String queryAndSyncPayment(Payment payment, BookOrder order) {
        if (!PaymentStatus.PENDING.equals(payment.getPaymentStatus())) {
            return payment.getPaymentStatus();
        }

        validateConfig();
        
        // 組合單筆查詢的 PostData 參數
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("MerchantID", merchantId);
        queryParams.put("Version", "1.3");
        queryParams.put("RespondType", "JSON");
        queryParams.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        queryParams.put("MerchantOrderNo", payment.getMerchantOrderNo());
        queryParams.put("Amt", String.valueOf(payment.getAmount()));

        // QueryTradeInfo 直接以表單欄位傳送，不使用 MPG 的 PostData 加密格式。

        // CheckValue 驗章：藍新查詢規格使用 IV、Amt、MerchantID、MerchantOrderNo、Key 的固定順序。
        String checkValueSource = "IV=" + hashIv + "&Amt=" + payment.getAmount()
                + "&MerchantID=" + merchantId + "&MerchantOrderNo=" + payment.getMerchantOrderNo()
                + "&Key=" + hashKey;
        String checkValue = sha256(checkValueSource);

        queryParams.put("CheckValue", checkValue);

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(queryUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(toFormUrlEncoded(queryParams)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("NewebPay query HTTP status: " + response.statusCode());
            }

            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
            if (!"SUCCESS".equalsIgnoreCase(stringValue(body.get("Status")))) {
                throw new IllegalStateException("NewebPay query failed: " + stringValue(body.get("Message")));
            }
            
            // RespondType=JSON 時 Result 是 JSON 物件，可直接讀取交易狀態。
            Object rawResult = body.get("Result");
            if (!(rawResult instanceof Map<?, ?> result)) {
                throw new IllegalStateException("NewebPay query response does not contain Result");
            }
            
            String tradeStatus = stringValue(result.get("TradeStatus"));
            payment.setProviderTradeNo(stringValue(result.get("TradeNo")));
            payment.setPaymentMethod(stringValue(result.get("PaymentType")));
            payment.setReturnCode(stringValue(result.get("RespondCode")));
            payment.setReturnMessage(stringValue(result.get("RespondMsg")));
            
            if (isPaidTradeStatus(tradeStatus)) {
                LocalDateTime paidAt = LocalDateTime.now();
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setPaidAt(paidAt);
                order.setOrderStatus(OrderStatus.PAID);
                order.setPaidAt(paidAt);
            } else if (isFailedTradeStatus(tradeStatus)) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                order.setOrderStatus(OrderStatus.FAILED);
            }
            paymentRepository.save(payment);
            bookOrderRepository.save(order);
            return payment.getPaymentStatus();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("NewebPay query was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("NewebPay query failed", exception);
        }
    }

    private boolean isPaidTradeStatus(String tradeStatus) {
        return "1".equals(tradeStatus) || "SUCCESS".equalsIgnoreCase(tradeStatus)
                || "PAID".equalsIgnoreCase(tradeStatus);
    }

    private boolean isFailedTradeStatus(String tradeStatus) {
        return "0".equals(tradeStatus) || "FAILED".equalsIgnoreCase(tradeStatus)
                || "FAIL".equalsIgnoreCase(tradeStatus);
    }

    private String sha256(String source) {
        try {
            return toHex(MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8))).toUpperCase();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String toFormUrlEncoded(Map<String, String> values) {
        return values.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void validateConfig() {
        if (merchantId.isBlank() || hashKey.isBlank() || hashIv.isBlank()
                || version.isBlank() || gatewayUrl.isBlank() || queryUrl.isBlank()) {
            throw new IllegalStateException("NewebPay config is incomplete");
        }

        if(hashKey.getBytes(StandardCharsets.UTF_8).length != 32) {
            throw new IllegalStateException("NewebPay HashKey must be 32 bytes");
        }

        if(hashIv.getBytes(StandardCharsets.UTF_8).length != 16) {
            throw new IllegalStateException("NewebPay HashIV must be 16 bytes");
        }
    }

    // 組合成url
    private String buildTradeInfo(Payment payment, Book book) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("MerchantID", merchantId);
        params.put("RespondType", "String");
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("Version", version);
        params.put("MerchantOrderNo", payment.getMerchantOrderNo());
        params.put("Amt", String.valueOf(payment.getAmount()));
        params.put("ItemDesc", book.getName());
        params.put("NotifyURL", notifyUrl);
        params.put("ReturnURL", returnUrl);
        String toQueryString = params.entrySet().stream()
                                .map(entry -> entry.getKey() + "=" + entry.getValue())
                                .reduce((a, b) -> a + "&" + b)
                                .orElse("");

        return toQueryString;
    }

    // 將 url 進行 AES 加密，並產生 TradeInfo
    private String encryptTradeInfo(String url) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(hashKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(hashIv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 藍新 MPG 需要的是 hex 字串，不是 Base64。
            return toHex(cipher.doFinal(pkcs7Padding(url.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception exception) {
            throw new IllegalStateException("NewebPay TradeInfo encryption failed", exception);
        }
    }

    private String toHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    private byte[] pkcs7Padding(byte[] source) {
        int paddingSize = 32 - source.length % 32;
        byte[] padded = new byte[source.length + paddingSize];
        System.arraycopy(source, 0, padded, 0, source.length);

        for (int index = source.length; index < padded.length; index++) {
            padded[index] = (byte) paddingSize;
        }

        return padded;
    }

    // 先確認書籍存在且未被軟刪除；不存在就不要建立任何訂單或付款資料。
    private Book findActiveBook(Long bookId) {
        try {
            return bookRepository.findOneById(bookId);
        } catch (NoResultException exception) {
            throw new ResourceNotFoundException("book", ResponseMessages.BOOK_NOT_FOUND);
        }
    }

    private Map<String, String> parseFormBody(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            throw new IllegalArgumentException("NewebPay notify body is empty");
        }

        Map<String, String> formParams = new LinkedHashMap<>();
        for(String pair : rawBody.split("&")) {
            if(pair.isEmpty()) {
                continue;
            }
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = keyValue.length == 2 ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
            formParams.put(key, value);
        }

        return formParams;
    }

    private String generateTradeSha(String tradeInfo) {
        String source = "HashKey=" + hashKey + "&" + tradeInfo + "&HashIV=" + hashIv;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(source.getBytes(StandardCharsets.UTF_8))).toUpperCase();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String decryptTradeInfo(String tradeInfo) {
        try {
            byte[] tradeInfoBytes = HexFormat.of().parseHex(tradeInfo);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(hashKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(hashIv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return new String(removePkcs7Padding(cipher.doFinal(tradeInfoBytes)), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("NewebPay TradeInfo decryption failed", exception);
        }
    }

    private byte[] removePkcs7Padding(byte[] source) {
        if (source.length == 0) {
            throw new IllegalArgumentException("Decrypted TradeInfo is empty");
        }

        int paddingSize = source[source.length - 1] & 0xff;
        if (paddingSize < 1 || paddingSize > 32 || paddingSize > source.length) {
            throw new IllegalArgumentException("Invalid TradeInfo padding");
        }

        for (int index = source.length - paddingSize; index < source.length; index++) {
            if ((source[index] & 0xff) != paddingSize) {
                throw new IllegalArgumentException("Invalid TradeInfo padding");
            }
        }

        byte[] unpadded = new byte[source.length - paddingSize];
        System.arraycopy(source, 0, unpadded, 0, unpadded.length);
        return unpadded;
    }
}

// 1. 做一個api 去藍新金流查詢 單筆訂單交易狀態

// 2. refresh or reload /page/books 的時候 觸發api 去看 購買中 跟現在時間比 超過30min 才要去查

// 3. 查到結果更改資料庫狀態
