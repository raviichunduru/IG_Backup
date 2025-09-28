package listeners;


import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.TestType;
import com.e2e_activecare.utils.PerformanceMetricUtil;
import com.e2e_activecare.utils.AssertUtil;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import com.e2e_activecare.pojo.context.SerialNumberHolder;

import static com.e2e_activecare.context.TestContextHolder.getCurrentTestType;
import static com.e2e_activecare.utils.AzureUtil.deleteDevice;
import static com.e2e_activecare.utils.BusinessLogicUtil.isRunningTestPerfCUJ;
import static com.e2e_activecare.utils.DataCleanupUtil.performSNRDeletion;
import static com.e2e_activecare.utils.FileUtil.clearUploadedFiles;
import static com.e2e_activecare.utils.WaitUtil.*;

public class TestCleanupListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            if (getCurrentTestType() != TestType.PARTNER_DEVICETRANSFER_AC_TO_ACPI
                    && getCurrentTestType() != TestType.PARTNER_DEVICETRANSFER_ACPI_TO_AC) {
                deleteDevice();
                performSNRDeletion();
            }
            WAIT.remove();
            SHORT_WAIT.remove();
            LONG_WAIT.remove();
            SerialNumberHolder.clear();
            AssertUtil.clear();
            clearUploadedFiles();
            TestContextHolder.clear();
            if(isRunningTestPerfCUJ()) {
                PerformanceMetricUtil.clear();
            }
        }
    }
}
