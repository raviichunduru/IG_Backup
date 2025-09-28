package context;

import com.e2e_activecare.enums.OrderType;
import com.e2e_activecare.enums.TestType;
import com.e2e_activecare.pojo.DataHolderForDBValidations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Arrays;

public class TestContextHolder {

    private static final Logger LOGGER = LogManager.getLogger(TestContextHolder.class);

    private static final ThreadLocal<DataHolderForDBValidations> dataHolder = ThreadLocal.withInitial(DataHolderForDBValidations::new);
    private static final ThreadLocal<TestType> currentTestType = new ThreadLocal<>();
    private static final ThreadLocal<OrderType[]> currentOrderType = new ThreadLocal<>();
    private static final ThreadLocal<ITestResult> currentTestResult = new ThreadLocal<>();
    private static final ThreadLocal<Integer> csvUploadCount = ThreadLocal.withInitial(() -> 0);


    public static DataHolderForDBValidations getDataHolder() {
        return dataHolder.get();
    }

    public static void setCurrentTestType(TestType testType) {
        currentTestType.set(testType);
        LOGGER.info("Thread local currentTestType is set to : {}", currentTestType.get());
    }

    public static TestType getCurrentTestType() {
        return currentTestType.get();
    }

    public static void setCurrentOrderType(OrderType[] orderType) {
        currentOrderType.set(orderType);
        LOGGER.info("Thread local currentOrderType is set to : {}", Arrays.toString(currentOrderType.get()));
    }

    public static OrderType[] getCurrentOrderTypes() {
        OrderType[] orderTypes = currentOrderType.get();
        if (orderTypes == null || orderTypes.length == 0) {
            throw new IllegalStateException("No OrderType found for current test context.");
        }
        return orderTypes;
    }

    public static void setCurrentTestResult(ITestResult testResult) {
        currentTestResult.set(testResult);
        LOGGER.info("Thread local currentTestResult is set for test: {}", testResult.getMethod().getMethodName());
    }

    public static ITestResult getCurrentTestResult() {
        return currentTestResult.get();
    }

    public static Method getCurrentTestMethod() {
        ITestResult testResult = getCurrentTestResult();
        if (testResult != null) {
            return testResult.getMethod().getConstructorOrMethod().getMethod();
        }
        throw new IllegalStateException("Current test result is not set in ThreadLocal.");
    }

    public static String getCurrentTestName() {
        ITestResult testResult = getCurrentTestResult();
        if (testResult != null) {
            return testResult.getMethod().getMethodName();
        }
        return "UnknownTest";
    }

    public static void clear() {
        dataHolder.remove();
        LOGGER.info("ThreadLocal - dataHolder, cleared from listener");
        currentTestType.remove();
        LOGGER.info("ThreadLocal - currentTestType, cleared from listener");
        currentOrderType.remove();
        LOGGER.info("ThreadLocal - currentOrderType, cleared from listener");
        currentTestResult.remove();
        LOGGER.info("ThreadLocal - currentTestResult, cleared from listener");
        csvUploadCount.remove();
        LOGGER.info("ThreadLocal - csvUploadCount, cleared from listener");
    }

    public static int getCsvUploadCount() {
        return csvUploadCount.get();
    }

    public static void setCsvUploadCount(int count) {
        csvUploadCount.set(count);
    }
}