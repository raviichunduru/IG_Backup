package context;//package com.e2e_activecare.context;
//
//import com.e2e_activecare.pojo.DeviceReport;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//public final class DeviceReportHolder {
//
//    private DeviceReportHolder() {}
//    private static final ThreadLocal<DeviceReport> deviceReport = new ThreadLocal<>();
//    private static final Logger LOGGER = LogManager.getLogger(DeviceReportHolder.class);
//
//    public static void set(DeviceReport report) {
//        deviceReport.set(report);
//    }
//
//    public static DeviceReport get() {
//        return deviceReport.get();
//    }
//
//    public static void clear() {
//        deviceReport.remove();
//        LOGGER.info("ThreadLocal - deviceReport, cleared from listener");
//    }
//}