package fa25.group.evtrainticket.utils;

import java.security.SecureRandom;

public class TicketUtils {

    // Bỏ các ký tự dễ nhầm: O, 0, I, 1, L
    private static final String CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8; // Độ dài mã vé
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}