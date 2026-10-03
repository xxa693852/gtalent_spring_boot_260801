package student.jia.gtalent_spring_boot_260801.exception;


public class MemberAccountExcption extends ApiException {

    public MemberAccountExcption(String errorKey, String messageCode) {
        super(errorKey, messageCode);
    }
}