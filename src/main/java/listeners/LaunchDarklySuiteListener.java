package listeners;

import com.basesource.utils.LaunchDarkly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class LaunchDarklySuiteListener implements ISuiteListener {

    public static boolean REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED;
    private static final Logger LOGGER = LogManager.getLogger(LaunchDarklySuiteListener.class);

    @Override
    public void onStart(ISuite suite) {
        REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED = new LaunchDarkly().getInstance()
                                                .enabled("remove-end-customer-name", null, null, true);
        LOGGER.info("Is LaunchDarkly toggle 'remove-end-customer-name' enabled? :  {}", REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED);
    }

    @Override
    public void onFinish(ISuite suite) {
    }
}