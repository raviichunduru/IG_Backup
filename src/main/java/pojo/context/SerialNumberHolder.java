package pojo.context;

public class SerialNumberHolder {
    private static final ThreadLocal<String> serialNumber = new ThreadLocal<>();

    public static void setSerialNumber(String serial) {
        serialNumber.set(serial);
    }

    public static String getSerialNumber() {
        return serialNumber.get();
    }

    public static void clear() {
        serialNumber.remove();
    }
}
