package listeners;

import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.OrderType;
import com.e2e_activecare.enums.TestType;
import com.e2e_activecare.annotations.TestMetadata;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class TestInitListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            TestContextHolder.setCurrentTestResult(testResult);

            Method testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
            if (testMethod != null && testMethod.isAnnotationPresent(TestMetadata.class)) {
                TestType testType = testMethod.getAnnotation(TestMetadata.class).testType();
                TestContextHolder.setCurrentTestType(testType);

                OrderType[] orderType = testMethod.getAnnotation(TestMetadata.class).orderType();
                TestContextHolder.setCurrentOrderType(orderType);
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    }
}