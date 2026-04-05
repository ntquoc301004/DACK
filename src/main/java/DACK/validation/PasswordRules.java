package DACK.validation;

/**
 * Quy tắc mật khẩu dùng chung (đăng ký, đổi mật khẩu, đặt lại mật khẩu).
 */
public final class PasswordRules {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 16;

    /**
     * Ít nhất một chữ thường, một chữ hoa, một chữ số và một ký tự đặc biệt (không phải chữ/số).
     */
    public static final String REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{" + MIN_LENGTH + "," + MAX_LENGTH + "}$";

    public static final String MESSAGE =
            "Mật khẩu " + MIN_LENGTH + "–" + MAX_LENGTH
                    + " ký tự, gồm chữ hoa, chữ thường, số và ít nhất một ký tự đặc biệt";

    private PasswordRules() {
    }
}
