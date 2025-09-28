package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

public final class AssertUtil {

    private AssertUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(AssertUtil.class);

    private static final ThreadLocal<SoftAssertions> softAssertions = ThreadLocal.withInitial(SoftAssertions::new);

    public static SoftAssertions softly() {
        return softAssertions.get();
    }

    public static void assertAll() {
        softAssertions.get().assertAll();
    }

    public static void clear() {
        softAssertions.remove();
        LOGGER.info("ThreadLocal - softAssertions, cleared from listener");
    }
}
