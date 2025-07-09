package holders;

import java.io.File;
import java.util.List;

import core.ProgramLogic;
import tools.Logger;
import tools.Utils;

public class Profile {
	
	/** Name of current profile */
	public final String name;
	
	/** Source Folder of current profile Game/Program which will be stored, and replaced (if needed) */
	public final String sourceFolderPath;
	
	/** Folder where will be stored saves for current profile */
	public final String targetFolderPath;
	
	/** Process execution command prompt to run by saver */
	public final String processCMD;
	
	// restore before/while start
	
	/** This flag will make program DELETING SORCE SAVES, BEFORE (or while) program will start target process (will be skipped if no saves stored) */
	public final boolean restoreBeforeStart;
	
	/**  Time (in seconds) for pause, between attempts to restore save file from backup. */
	public final int pauseBeforeRestore;
	
	/** This flag will make backup of currently stored saves before replacing */
	public final boolean backupBeforeRestore;
	
	/** Time (in seconds) in which program will make attempts to restore (replace) source saves with files from backups WHILE GAME RUNNIG. If set to less than 1, will do nothing. */
	public final int restoreTimePeriod;
	
	/**  Time (in seconds) for pause, between attempts to restore save file from backup. */
	public final int restoreAttemptsTimePause;
	
	// storing options
	
	/** Time (in seconds) for pause, between attempts to backup save files.
	 * Minimum (and default) value is 1 second.
	 * If its value higher than 0 - will spawn process to consistently backup
	 * Game saves, while Game running.
	 * By default program will backup only before process start. */
	public final int peorodicalBackupTimeOut;
	
	/** Time (in seconds, min 1, max INF) for pause, between attempts to save source game save files, in case if file is busy (being written outside). */
	public final int pauseBeforeNextReadAttempt;
	
	/** Numerical limit in seconds of maximum attempts of attempts to read possibly 'busy' file (occupied by other process) in current backupping attempt. */
	public final int maxReadAttempts;
	
	/** Name of created by program Archive files . */
	public final String preferredFilesName;
	
	public Profile(String profileName, String inSourceFolderPath, String inTargetFolderPath, String cmd, int pauseBeforeRestoring, boolean inRestoreBeforeStart, boolean inBackupBeforeRestore, int inRestoreTimePeriod, int inRestoreAttemptsTimePause, int inPeorodicalBackupTimeOut, int readAttemptTimeout, int inMaxReadAttempts, String filesNames) {
		this.name = profileName;
		
		this.sourceFolderPath = inSourceFolderPath;
		this.targetFolderPath = inTargetFolderPath;
		
		this.pauseBeforeRestore  = pauseBeforeRestoring;
		this.restoreBeforeStart  = inRestoreBeforeStart;
		this.backupBeforeRestore = inBackupBeforeRestore;
		this.restoreTimePeriod   = inRestoreTimePeriod;
		this.restoreAttemptsTimePause = inRestoreAttemptsTimePause;
		
		this.peorodicalBackupTimeOut = inPeorodicalBackupTimeOut;
		
		this.pauseBeforeNextReadAttempt = readAttemptTimeout;
		this.maxReadAttempts = inMaxReadAttempts;
		
		this.processCMD = cmd;
		
		this.preferredFilesName = filesNames;
	}
	
	public static Profile load(File configFile, String configFilePath, String profileName) {
		List<String> fileLines = Utils.readAllLines(configFile, configFilePath);
		int fileLinesSize = fileLines.size();
		
		boolean profileExist = false;
		
		int startLineIndex = -1;
		
		// Searching for profile header [PROFILE_NAME]
		for (int li = 0; li < fileLinesSize && !profileExist; li++) {
			String line = fileLines.get(li).trim();
			if(line.startsWith("[") && line.endsWith("]") && line.contains(profileName)) {
				profileExist = true;
				startLineIndex = li;
			}
		}
		
		// if no profile name in this file
		if(!profileExist) {
			Logger.printError("ERROR! Can't find Profile '" + profileName + "' in supplied config: " + configFilePath + " Fatal exit.");
			System.exit(-1);
		}
		
		// Required options
		boolean appRunCommandSet = false;
		boolean pathToSourceSavesSet = false;
		boolean pathToBackupFolderSet = false;
		
		//
		boolean nextProfileFound = false;
		
		String temp_target_app_run_cmd = null;
		String temp_source_saves_folder_path = null;
		String temp_backup_saves_folder_path = null;
		
		int temp_pause_before_restore = 22;
		
		boolean temp_restore_at_start = false;
		boolean temp_backup_before_restore = true;
		
		int temp_restore_period_sec = 120;
		int temp_timeout_between_restore_attempts_sec = 5;
		
		int temp_read_file_timeout_sec = 5;
		int temp_read_file_max_attempts = 10;
		
		int temp_pause_between_backup_saves_sec = 600; // 10 min
		
		String temp_preferred_files_names = profileName.trim().toLowerCase().replace(" ", "_");
		
		
		// if profile found -> process until found next profile or end of file
		for (int li = startLineIndex + 1; li < fileLinesSize && !nextProfileFound; li++) {
			String line = fileLines.get(li);
			
			// skip comments
			line = line.split("#" )[0].trim();
			//line = line.split("//")[0].trim(); disabled because steam path use '//' as web path to run app
			
			// skip empty lines (process only if there is content)
			if(line.length() > 0) {
			
				//System.out.println("l: " + line);
				if(line.startsWith("[")) {
					nextProfileFound = true;
				}
				else { // process lines here
					
					if(line.startsWith("target_app_run_cmd")) {
						temp_target_app_run_cmd = getValueFromFile("target_app_run_cmd", line);
						if(temp_target_app_run_cmd != null && temp_target_app_run_cmd.length() > 0) {
							appRunCommandSet = true;
						}
						else {
							Logger.printError("ERROR! Specified App execution cmd is empty. Fatal exit.");
							System.exit(-1);
						}
					}
					else if(line.startsWith("source_saves_folder_path")) {
						temp_source_saves_folder_path = getValueFromFile("source_saves_folder_path", line);
						if(temp_source_saves_folder_path != null && temp_source_saves_folder_path.length() > 0) {
							File sourseLocationFile = new File(temp_source_saves_folder_path);
							if(sourseLocationFile.exists()) {
								pathToSourceSavesSet = true;
							}
							else {
								Logger.printError("ERROR! Supplied location '" + sourseLocationFile.getAbsolutePath() + "' of target files NOT exist!. Fatal exit.");
								System.exit(-1);
							}
						}
						else {
							Logger.printError("ERROR! Supplied string '" + temp_source_saves_folder_path + "' for target files for backup location is empty!. Fatal exit.");
							System.exit(-1);
						}
					}
					else if(line.startsWith("backup_saves_folder_path")) {
						temp_backup_saves_folder_path = getValueFromFile("backup_saves_folder_path", line);
						if(temp_backup_saves_folder_path != null && temp_backup_saves_folder_path.length() > 0) {
							File backupFolderFile = new File(temp_backup_saves_folder_path);
							if(backupFolderFile.exists() && backupFolderFile.isDirectory()) {
								pathToBackupFolderSet = true;
							}
							else {
								Logger.printError("ERROR! Invalid Backup Location: '" + backupFolderFile.getAbsolutePath() + "' - " + (backupFolderFile.exists() ? "Not a directory" : "Not exist") + ". Fatal exit.");
								System.exit(-1);
							}
						}
						else {
							Logger.printError("ERROR! Supplied string '" + temp_backup_saves_folder_path + "' for backup location is empty!. Fatal exit.");
							System.exit(-1);
						}
					}
					else if(line.startsWith("restore_at_start")) {
						temp_restore_at_start = getValueFromFile("restore_at_start", line, false);
					}
					else if(line.startsWith("backup_before_restore")) {
						temp_backup_before_restore = getValueFromFile("backup_before_restore", line, true);
					}
					else if(line.startsWith("restore_period_sec")) {
						temp_restore_period_sec = getValueFromFile("restore_period_sec", line, 0, Integer.MAX_VALUE, 120);
					}
					else if(line.startsWith("pause_before_restore_attempt_sec")) {
						temp_pause_before_restore = getValueFromFile("pause_before_restore_attempt_sec", line, 0, Integer.MAX_VALUE, 5);
					}
					else if(line.startsWith("timeout_between_restore_attempts_sec")) {
						temp_timeout_between_restore_attempts_sec = getValueFromFile("timeout_between_restore_attempts_sec", line, 1, Integer.MAX_VALUE, 5);
					}
					else if(line.startsWith("read_file_timeout_sec")) {
						temp_read_file_timeout_sec = getValueFromFile("read_file_timeout_sec", line, 1, Integer.MAX_VALUE, 5);
					}
					else if(line.startsWith("read_file_max_attempts")) {
						temp_read_file_max_attempts = getValueFromFile("read_file_max_attempts", line, 0, 100, 10);
					}
					else if(line.startsWith("pause_between_backup_saves_sec")) {
						temp_pause_between_backup_saves_sec = getValueFromFile("pause_between_backup_saves_sec", line, 1, Integer.MAX_VALUE, 600);
					}
					else if(line.startsWith("preferred_files_names")) {
						temp_preferred_files_names = getValueFromFile("preferred_files_names", line);
						if(temp_preferred_files_names == null || temp_preferred_files_names.length() == 0) {
							profileName.trim().toLowerCase().replace(" ", "_");
						}
					}
				}
			}
		}
		
		if(!appRunCommandSet) {
			System.out.println();
			Logger.printError("Warning! Current profile configuration '" + profileName + "'");
			Logger.printError("does not contain (or not provided) path to related application");
			Logger.printError("to target backup files, so its not possibleto run it,");
			Logger.printError("and run sage will be skipped.");
			Logger.printError("If You need to run application with backup process");
			Logger.printError("initiation - set 'target_app_run_cmd' variable.");
			System.out.println();
		}
		
		if(pathToSourceSavesSet && pathToBackupFolderSet) {
			Configuration.PROFILE = new Profile(
					profileName,
					
					temp_source_saves_folder_path,
					temp_backup_saves_folder_path,
					
					temp_target_app_run_cmd,
					
					temp_pause_before_restore,
					
					temp_restore_at_start,
					temp_backup_before_restore,
					
					temp_restore_period_sec,
					temp_timeout_between_restore_attempts_sec,
					
					temp_pause_between_backup_saves_sec,
					
					temp_read_file_timeout_sec,
					temp_read_file_max_attempts,
					
					temp_preferred_files_names
			);
		}
		else {
			Logger.printError("ERROR! One of core parameters is invalid:");
			Logger.printError("App CMD (" + appRunCommandSet + "): " + temp_target_app_run_cmd);
			Logger.printError("Saves Location (" + pathToSourceSavesSet + "): " + temp_source_saves_folder_path);
			Logger.printError("Backup Location (" + pathToBackupFolderSet + "): " + temp_backup_saves_folder_path);
			Logger.printError("Fatal exit.");
			System.exit(-1);
		}
		
		return Configuration.PROFILE;
	}
	
	private static int getValueFromFile(String name, String line, int min, int max, int basicVal) {
		String val = line.replace(name, "").trim();
		if(val.startsWith("=")) {
			val = val.substring(1).trim();
		}
		
		boolean valid = false;
		
		int returnValue = 0;
		
		try {
			returnValue = Integer.parseInt(val);
			valid = true;
		}
		catch (NumberFormatException e) {}
		
		if(!valid) {
			Logger.printError("Warning! Can't parse numeric parameter '" + name  + "'. Setting default(" + basicVal + ") Value: " + val);
			return basicVal;
		}
		
		if(returnValue > max) {
			Logger.printError("Warning! Supplied parameter for '" + name  + "' is bigger than possible Maximum (" + max + "). Setting default (" + basicVal + ") value. Config Value: " + val);
			return basicVal;
		}
		
		if(returnValue < min) {
			Logger.printError("Warning! Supplied parameter for '" + name  + "' is less than possible Minimum (" + min + "). Setting default (" + basicVal + ") value. Config Value: " + val);
			return basicVal;
		}
		
		return returnValue;
	}
	
	private static boolean getValueFromFile(String name, String line, boolean basicVal) {
		String flag = line.replace(name, "").trim();
		if(flag.startsWith("=")) {
			flag = flag.substring(1).trim();
		}
		
		if(flag.length() == 0) {
			Logger.printError("Warning! Nothind given as a parameter for '" + name  + "'. Setting default (" + basicVal + ") value. Config Value: " + flag);
			return basicVal;
		}
		
		String lowFlag = flag.toLowerCase();
		
		if(lowFlag.startsWith("y")) {
			return true;
		}
		
		if(lowFlag.startsWith("t")) {
			return true;
		}
		
		if(ProgramLogic.NUM.matcher(flag).matches()) {
			return (Integer.parseInt(flag) > 0);
		}
		
		return false;
	}
	
	private static String getValueFromFile(String name, String line) {
		String out = line.replace(name, "").trim();
		if(out.startsWith("=")) {
			return out.substring(1).trim();
		}
		return out;
	}
	

	private int getInsideIntValue(String name) {
		switch (name) {
			case "read_file_timeout_sec" : return this.pauseBeforeNextReadAttempt;
			default                      : return -1;
		}
	}
	
	private static int getDefaultIntValue(String name) {
		switch (name) {
			case "read_file_timeout_sec" : return  1;
	
			default                      : return -1;
		}
	}
	
	public static int getIntValue(String name) {
		
		// load from profile
		Profile localProfile = Configuration.PROFILE;
		if(localProfile != null) {
			return localProfile.getInsideIntValue(name);
		}
		
		// load default value by name
		return  getDefaultIntValue(name);
	}
	

	


}
