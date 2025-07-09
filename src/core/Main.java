package core;

import java.io.File;

import holders.Configuration;
import holders.Profile;
import tools.HighLevelClock;
import tools.Logger;
import tools.Utils;

public class Main {

	public static void main(String[] args) {
		Logger.printWithTime("Initializing Automatic Saves Saver Application...");
		
		// execution options
		String coreConfig = "./games.list";
		String currentProfileName = null;
		boolean skipGameExecution = false;
		boolean showHelp = true;
		
		//args = new String[] { "-p", "CODE VEIN LINUX", "-ne" };
		
		if(args != null) {
			
			int argsSize = args.length;
			showHelp = !(argsSize > 0);
			
			for (int i = 0; i < argsSize; i++) {
				String arg = args[i].replace("--", "-");
				if(arg != null) {
					String fixedArg = arg.replace("--", "-").replace(":", "");
					
					currentProfileName = ProgramLogic.extractProfileName(arg, currentProfileName);
					
					switch (fixedArg) {
					
						// show help and exit
						case "-h"       :
						case "-help"    : showHelp = true; break;
						
						// set custom config file path
						case "-c"       :
						case "-config"  : if((i+1) < argsSize) { coreConfig = args[i+1]; i++; } break;
						
						// test mode
						case "-t"       :
						case "-test"    : ProgramLogic.processTestMode(args, i); break;
						
						// loading profile
						case "-p"       :
						case "profile"  :
						case "-profile" : currentProfileName = ProgramLogic.extractProfileName(args, i, currentProfileName); break;
						
						// flag to skipping program
						case "-n"       :
						case "-ne"      :
						case "-notexec" : skipGameExecution = true; break;
						
						default         : break;
					}
				}
			}
		}
		
		if(showHelp) { 
			System.out.println(
				"Usage: java -jar app.jar -p PROFILE_NAME -c PATH_TO_CONFIG [optional args...]\n\n" +
				"Options:\n" +
				
				"         -h\n" +
				"         -help                   -  Print this help and exit.\n\n" +
				
				"         -c\n" +
				"         -config                 -  Path to config with profiles.\n\n" +
				
				"         -t    IN_PATH OUT_PATH\n" +
				"         -test IN_PATH OUT_PATH  -  IN_PATH - folder (with files) to backup,\n" +
				"                                    OUT_PATH - folder to store output archive\n" +
				"                                    and output files to comare (with source files)\n\n" +
				
				"         -p\n" +
				"         -profile\n" +
				"         profile=                -  Path to file with games profile/profiles.\n\n" +
				
				"         -n\n" +
				"         -ne\n" +
				"         -notexec                -  Skip running of attached game while app starting."
			);
			System.exit(0);
		}
		
		if(currentProfileName != null && currentProfileName.length() > 0) {
			System.out.println("Profile: " + currentProfileName);
		}
		else {
			Logger.printError("ERROR! Can't read name of profile. Exit.");
			System.exit(-1);
		}
		
		if(coreConfig != null && coreConfig.length() > 0) {
			File coreConfigFile = new File(coreConfig);
			
			String configFullPath = coreConfigFile.getAbsolutePath().replace('\\', '/').replace("/./", "/").replace("//", "/").replace("//", "/");
			if(Configuration.os == Configuration.OS.WIN) { configFullPath = configFullPath.replace('/', '\\'); }
			System.out.println("Trying load config: " + configFullPath);
			
			if(coreConfigFile.exists()) {
				
				// CORE PROGRAM EXECUTION
				Profile.load(coreConfigFile, coreConfig, currentProfileName);
				ProgramLogic.runSaver(skipGameExecution);
				
			}
			else {
				Logger.printError("ERROR! Can't read config: File not exist!. Panic exit.");
				System.exit(-1);
			}
		}
		else {
			Logger.printError("ERROR! Can't read config location (its null or empty). Panic exit.");
			System.exit(-1);
		}
		
		System.out.println("PROGRAM_END");
	}
}
