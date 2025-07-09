package holders;

import core.ProgramLogic;

public class Configuration {
	
	public static enum OS { WIN, LINUX, MAC };
	
	public static final int MAX_RECURTION_DEPTH = 16;

	public static boolean PROCESSING = false;
	
	public static final OS os = ProgramLogic.getOperetionSystemType();
	
	public static final int MAX_READ_ERRORS = 30;
	
	public static Profile PROFILE;
}
