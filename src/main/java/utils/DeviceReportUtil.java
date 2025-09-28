package utils;//package com.e2e_activecare.utils;
//
//import com.e2e_activecare.pojo.DeviceReport;
//import com.e2e_activecare.context.DeviceReportHolder;
//
//import java.util.List;
//import java.util.Map;
//
//public class DeviceReportUtil {
//
//    private static DeviceReport getReport() {
//        return DeviceReportHolder.get();
//    }
//
//    public static String getJsonPath() {
//        return getReport().getJsonPath();
//    }
//
//    public static String getMessage() {
//        return getReport().getMessage();
//    }
//
//    private static DeviceReport.AcOrderSubscriptionDataProvider getAcOrderProvider() {
//        Map<String, DeviceReport.ProviderWrapper> providers = getReport().getProviders();
//        if (providers != null && !providers.isEmpty()) {
//            DeviceReport.ProviderWrapper wrapper = providers.values().iterator().next();
//            if (wrapper != null && wrapper.getProviders() != null) {
//                return wrapper.getProviders().getAcOrderSubscriptionDataProvider();
//            }
//        }
//        return null;
//    }
//
//    public static List<DeviceReport.Plan> getPlans() {
//        DeviceReport.AcOrderSubscriptionDataProvider provider = getAcOrderProvider();
//        return provider != null ? provider.getPlans() : List.of();
//    }
//
//    public static List<DeviceReport.Device> getDevices() {
//        DeviceReport.AcOrderSubscriptionDataProvider provider = getAcOrderProvider();
//        if (provider != null && provider.getSubscriptions() != null) {
//            return provider.getSubscriptions().getDevices();
//        }
//        return List.of();
//    }
//
//}