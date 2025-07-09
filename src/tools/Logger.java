package tools;

/**
 * Class to handle all logging capabilities of program.
 */
public class Logger {
	
	/**
	Log for all life time of program execution.
	Will be saved, if some kind of error/exception will appear.
	*/
	public static final StringBuilder runtimeLog = new StringBuilder();
	
	/** Fast phase temporally log collection, before activate user's pop-ups */
	public static final StringBuilder tempLog = new StringBuilder();
	
	/** Fast phase temporally log collection, before activate user's pop-ups */
	public static String previusTempLog = "";
	
	public static String log(String line) {
		runtimeLog.append(line + "\n");
		return line;
	}
	
	public static String printWithTime(String string) {
		return printWithTime(string, HighLevelClock.getDate());
	}
	
	public static String printWithTime(String string, String date) {
		System.out.println(HighLevelClock.getFormattedTime(date) + " " + string);
		return string;
	}
	
	public static String printWithTimeNoNewLine(String string) {
		return printWithTimeNoNewLine(string, HighLevelClock.getDate());
	}
	
	public static String printWithTimeNoNewLine(String string, String date) {
		System.out.print(HighLevelClock.getFormattedTime(date) + " " + string);
		return string;
	}
	
	public static String printError(String string) {
		System.err.println(string);
		return string;
	}
	
	public static String printErrorWithTime(String string) {
		return printErrorWithTime(string, HighLevelClock.getDate());
	}
	
	public static String printErrorWithTime(String string, String date) {
		System.out.print(HighLevelClock.getFormattedTime(date) + " ");
		System.err.println(string);
		return string;
	}

}
