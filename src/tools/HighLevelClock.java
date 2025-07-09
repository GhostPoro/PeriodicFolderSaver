package tools;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class HighLevelClock extends Clock {
	
	private static long nano_per_second = 1000_000_000L;

    private final ZoneId zoneId;

    public HighLevelClock(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public Clock withZone(ZoneId zoneId) {
        return new HighLevelClock(zoneId);
    }

    @Override
    public Instant instant() {
        long nanos = getAccurateNow();
        return Instant.ofEpochSecond(nanos / nano_per_second, nanos % nano_per_second);
    }
    
    private long getAccurateNow() {
    	long jvm_diff = System.currentTimeMillis() * 1000_000 - System.nanoTime();
    	return System.nanoTime() + jvm_diff;
    }
    
    public static String getDate() {
    	return LocalDateTime.now(new HighLevelClock(ZoneId.systemDefault())).toString();
    }
    
    public static String getDateForFile() {
    	return getDateForFile(getDate());
    }
    
    public static String getDateForFile(String date) {
    	return date.split("\\.")[0].replace("-", "").replace("T", "").replace(":", "");
    }
    
    public static String getFormattedTime() { // .split("\\.")[0]
    	return getFormattedTime(getDate());
    }
    
    public static String getFormattedTime(String date) {
    	String[] parts = date.split("\\.");
    	String mils = ((parts.length > 1 && parts[1] != null) ? parts[1].substring(0,3) : "000");
    	return new String("[" + parts[0].replace('-', '/').replace('T', ' ') + ":" + mils + "]");
    }
}
