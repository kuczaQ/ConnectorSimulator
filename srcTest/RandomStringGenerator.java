import java.util.Random;

@SuppressWarnings("WeakerAccess")
public class RandomStringGenerator {
    private static final int DEFAULT_LENGTH = 6;

    private String[] characters = "qwertyuiopasdfghjklzxcvbnm1234567890".split(""),
                     numbers    = "1234567890".split(""),
                     letters    = "qwertyuiopasdfghjklzxcvbnm".split("");

    private String[] domains = {
            "@gmail.com",
            "@swmh.de",
            "@ventum.com",
            "@outlook.com",
            "@yahoo.com"
    };

    private Random rand;

    public RandomStringGenerator() {
        rand = new Random();
    }

    public RandomStringGenerator(long seed) {
        rand = new Random(seed);
    }

    public String getString() {
        return getAlphanumericString(DEFAULT_LENGTH);
    }

    public String getString(int length) {
        return getRandom(letters, length);
    }


    public String getAlphanumericString() {
        return getAlphanumericString(DEFAULT_LENGTH);
    }

    public String getAlphanumericString(int length) {
        return getRandom(characters, length);
    }

    public String getNumber() {
        return getNumber(DEFAULT_LENGTH);
    }

    public String getNumber(int length) {
        return getRandom(numbers, length);
    }

    public String getUserName() {
        return getUserName(DEFAULT_LENGTH);
    }

    public String getUserName(int length) {
        return getString(length) + getNumber(rand.nextInt(2) + 1);
    }

    public String getEmail() {
        return getEmail(DEFAULT_LENGTH);
    }

    public String getEmail(int length) {
        return getAlphanumericString(length) + domains[rand.nextInt(domains.length)];
    }

    private String getRandom(String[] source, int length) {
        StringBuilder res = new StringBuilder();

        for (int a = 0; a < length; a++)
            res.append(source[rand.nextInt(source.length)]);

        return res.toString();
    }
}
