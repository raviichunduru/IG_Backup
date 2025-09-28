package utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtil {

    private RandomUtil() {}

    public static String getRandomAlphaNumeric(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        while (length-- > 0) {
            int index = ThreadLocalRandom.current().nextInt(characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }

    public static String getRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        while (length-- > 0) {
            int index = ThreadLocalRandom.current().nextInt(characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }

    public static String getRandomNumeric(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String getRandomIntegerAsString(int digits) {
        if (digits <= 0) {
            throw new IllegalArgumentException("Number of digits must be greater than 0.");
        }
        int lowerBound = (int) Math.pow(10, digits - 1); // Minimum value for the specified number of digits
        int upperBound = (int) Math.pow(10, digits) - 1; // Maximum value for the specified number of digits
        int randomNumber = ThreadLocalRandom.current().nextInt(lowerBound, upperBound + 1);
        return String.valueOf(randomNumber);
    }
}
