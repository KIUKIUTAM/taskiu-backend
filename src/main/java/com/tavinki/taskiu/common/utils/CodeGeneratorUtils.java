package com.tavinki.taskiu.common.utils;

import java.security.SecureRandom;

public class CodeGeneratorUtils {

    private CodeGeneratorUtils() {
        throw new IllegalStateException("Utility class");
    }

    // SecureRandom 建立成本較高，建議宣告為 static 重複使用
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateSecureCode() {
        int number = secureRandom.nextInt(1000000);
        return String.format("%06d", number);
    }
}
