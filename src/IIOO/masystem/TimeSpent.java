package IIOO.masystem;

public class TimeSpent {
    private static long START_TIME;

    public static void startTime() {
        START_TIME = System.currentTimeMillis();
    }

    private static float timeSpentF() {
        return (System.currentTimeMillis() - START_TIME) / 1000f;
    }

    public static String timeSpent() {
        return String.format("%3.2f s", timeSpentF());
    }
}
