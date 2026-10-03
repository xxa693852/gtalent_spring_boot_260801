package student.jia.gtalent_spring_boot_260801.request;

public class OrderCheckRequest {
    private String merchantOrderNo;
    // 保留舊版前端欄位以維持相容性；後端查詢時刻意忽略，改讀資料庫金額。
    private Integer amount;

    // Getters and Setters
    public String getMerchantOrderNo() {
        return merchantOrderNo;
    }

    public void setMerchantOrderNo(String merchantOrderNo) {
        this.merchantOrderNo = merchantOrderNo;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
