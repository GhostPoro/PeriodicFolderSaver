package core;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.regex.Pattern;

import holders.Configuration;
import holders.Configuration.OS;
import holders.Profile;
import tools.FileTool;
import tools.HighLevelClock;
import tools.Logger;
import tools.Utils;
import tools.ZipTask;

public class ProgramLogic {
	
	public static Pattern NUM = Pattern.compile("\\d+");
	
	public static String[] simpleShellExec(String cmd) {
		Logger.printErrorWithTime("EXEC: " + cmd);
		try {
			Process process = null;
			if(Configuration.os == OS.WIN) {
				process = Runtime.getRuntime().exec(cmd);
			}
			else {
				String[] commands = { "bash", "-c", cmd };
				process = Runtime.getRuntime().exec(commands);
			}
			final String[] holder = new String[2];
			storeShellOutput(process.getInputStream(), holder, 0);
			storeShellOutput(process.getErrorStream(), holder, 1);
			process.waitFor();
			
			// possible finish of exec
			return holder;
			
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
		catch (InterruptedException ite) {
			ite.printStackTrace();
			return null;
		}
	}
	
	public static boolean run(String cmd) {
		Logger.printErrorWithTime("START APP: " + cmd);
		
		boolean URI_APP = cmd.contains("://");
		try {
			if(URI_APP) {
				Desktop.getDesktop().browse(new URI(cmd.replace("\"", "")));
			}
			else {
				Process process = null;
				if(Configuration.os == OS.WIN) {
					process = Runtime.getRuntime().exec(cmd);
				}
				else {
					String[] commands = { "bash", "-c", cmd };
					process = Runtime.getRuntime().exec(commands);
				}
				return true;
			}
		}
		catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static boolean storeShellOutput(InputStream stream, final String[] holder, int idx) {
		if(stream != null) {
			
			Thread readerThread = new Thread() {
			    public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
					StringBuilder strBuilder = new StringBuilder();
					try {
						String output = reader.readLine();
						while (output != null) {
							strBuilder.append(output);
							output = reader.readLine();
						}
						
						// when done
						if((holder != null) && (idx > -1) && (idx < holder.length)) {
							holder[idx] = strBuilder.toString();
						}
						
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
			    }
			};
			readerThread.start();
			return true;
		}
		return false;
	}
	
	public static boolean checkExecOut(String exec, String a, String b) {
		String[] out = ProgramLogic.simpleShellExec(exec);
		if((out != null) && (out.length > 1)) {
			String[] words = Utils.trimSpaces(((out[0] == null) ? "" : out[0]).replace('\n', ' ') + " " + ((out[1] == null) ? "" : out[1]).replace('\n', ' ')).split(" ");
			int wsize = words.length;
			if(wsize > 1) {
				int wsizemo = (wsize - 1);
				for (int i = 0; i < wsizemo; i++) {
					if(words[i].equals(a) && words[i + 1].equals(b)) {
						return true;
					}
				}
			}
		}
		System.err.println("ERROR! Can't read EXEC output!");
		return false;
	}
	
	public static boolean checkExistence(String path) {
		if(path != null) {
			File dummy = new File(path);
			return (dummy.exists() && dummy.isFile());
		}
		return false;
	}
	
	public static int getNumFiles(String path) {
		return getNumFiles(new File(path));
	}
	
	private static int getNumFiles(File dir) {
		if(dir != null) {
			String[] list = dir.list();
			if(list != null) {
				return list.length;
			}
		}
		return -1;
	}
	
	public static boolean runSaver(boolean skipAppExec) {
		Profile profile = Configuration.PROFILE;
		if(profile != null) {
			//System.out.println("run");
			
			boolean APP_RUNNING = (skipAppExec || (profile.processCMD == null));
			
			String logDate = HighLevelClock.getFormattedTime();
			
			// restoring from backup before App loading
			if(profile.restoreBeforeStart) {
				String[] storedBackupFiles = FileTool.storeFilesPathesFromFolder(profile.targetFolderPath);
				
				//System.out.println("Size: " + storedBackupFiles.length);
				
				int backupSize = storedBackupFiles.length;
				if(backupSize > 0) {
					
					long latesSaveNum = -1;
					String latesSaveFilePath = null;
					
					// picking most recent file
					for (int bfi = 0; bfi < backupSize; bfi++) {
						String bkpFile = storedBackupFiles[bfi];
						// looking for files of current profile
						if(bkpFile.endsWith(profile.preferredFilesName + "_standard_backup.zip")) {
							int nameSplitter = (bkpFile.replace('\\', '/').lastIndexOf('/') + 1);
							if((nameSplitter < bkpFile.length()) && (nameSplitter > 0)) {
								String zipName = bkpFile.substring(nameSplitter);
								String zipDate = zipName.substring(0, 14);
								if(NUM.matcher(zipDate).matches()) {
									long testZipDate = Long.parseLong(zipDate);
									if(testZipDate > latesSaveNum) {
										latesSaveNum = testZipDate;
										latesSaveFilePath = bkpFile;
									}
								}
							}
						}
					}

					// Found standard backup for current profile -> can restore (if needed)
					if(latesSaveFilePath != null) {
						
						// RUN APP BEFORE FILE OPERATION (if there there any timers)
						if(profile.pauseBeforeRestore > 0 || profile.restoreTimePeriod > 0) {
							run(Utils.quotePath(profile.processCMD));
							APP_RUNNING = true;
						}
						
						// waiting while Game starts...
						waitSeconds(profile.pauseBeforeRestore);
						
						if(profile.backupBeforeRestore) {
							
							// TODO: VALIDATE SOURCE BACKUP HERE
							
							String sourceBackupDate = HighLevelClock.getDate();
							
							String sourceBackupZipFilePath = new String(profile.targetFolderPath + File.separator + HighLevelClock.getDateForFile(sourceBackupDate) + "_" + profile.preferredFilesName + "_source_data_backup.zip");
							
							boolean sourceBackuped = false;
							
							int attmpts = 0;
							final int maxAttmpts = 3;
							
							do {
								ZipTask sourceBackup = new ZipTask(profile.sourceFolderPath, sourceBackupZipFilePath);
								sourceBackuped = sourceBackup.isValid();
								attmpts++;
							} while (attmpts < maxAttmpts && !sourceBackuped);
							
							if(sourceBackuped) {
								Logger.printWithTime("Bakuped Source Saves!", sourceBackupDate);
							}
							else {
								Logger.printErrorWithTime("ERROR! Can't Backup Source Files Before Override, exiting to prevent data loss.", sourceBackupDate);
								System.exit(-1);
							}
							
						}
						
						// Here we only if no need to backup before restore, or if backup of source data is successful
						System.out.println("Restore sec: " + profile.restoreTimePeriod);
						
						String restoringDate = HighLevelClock.getDate();
						
						// if need to restoring several times during run, but not one time before game start
						if(profile.restoreTimePeriod > 0) {
						
							// litte hack for waiting
							int taskCounter = 0;
							int pouseCounter = 0;
							
							int totalAttempts = 0;
							int validAttempts = 0;
							boolean lastAttempt = false;
							
							Logger.printWithTime("Starting Restore Session for Save: " + latesSaveFilePath, restoringDate);
							Logger.printWithTime("Time: " + profile.restoreTimePeriod + " Pause: " + profile.restoreAttemptsTimePause, restoringDate);
							
							// restoring attempts
							while(taskCounter < profile.restoreTimePeriod) {
								if(pouseCounter >= profile.restoreAttemptsTimePause) {
									lastAttempt = false;
									totalAttempts++;
									
									File revriteDirectiory = new File(profile.sourceFolderPath);
									if(FileTool.deleteDirectory(revriteDirectiory)) {
										revriteDirectiory.mkdirs();
										Logger.printWithTime("Deleted!");
									}
									
									if(ZipTask.unzip(latesSaveFilePath, profile.sourceFolderPath)) {
										validAttempts++;
										lastAttempt = true;
										Logger.printWithTime("Restored!");
									}
									else {
										Logger.printErrorWithTime("Restore Failed!");
									}
									pouseCounter = 0;
								}
								
								
								pouseCounter++;
								taskCounter++;
								waitSeconds(1);
							}
							
							Logger.printWithTime("Restoring Session Finished. Total Attemps: " + totalAttempts + " | Valid Attampts: " + validAttempts + " | Last Attampt Valid: " + lastAttempt);
						}
						else {
							// just restore and run APP
							if(ZipTask.unzip(latesSaveFilePath, profile.sourceFolderPath)) {
								Logger.printWithTime("Restored Save: " + latesSaveFilePath);
								
								if(!APP_RUNNING) {
									run(Utils.quotePath(profile.processCMD));
									APP_RUNNING = true;
								}
							}
							else {
								Logger.printErrorWithTime("ERROR! Can't restore Saves from: '" + latesSaveFilePath + "'. Exiting to prevent data loss.");
								System.exit(-1);
							}
						}
						
						
					}
				}
			}
			
			String sourceBackupZipFilePath = new String(profile.targetFolderPath + File.separator + HighLevelClock.getDateForFile() + "_" + profile.preferredFilesName + "_standard_backup.zip");
			
			// just backup data and start app
			ZipTask basicBackup = new ZipTask(profile.sourceFolderPath, sourceBackupZipFilePath);
			if(basicBackup.isValid()) {
				Logger.printWithTime("Initial Saves Backup Done!");
			}
			else {
				Logger.printErrorWithTime("ERROR! Can't backup Saves inside: '" + profile.sourceFolderPath + "'. Exiting to prevent data loss.");
				return false; //System.out.println();
			}
			
			if(!APP_RUNNING) {
				run(Utils.quotePath(profile.processCMD));
				APP_RUNNING = true;
			}
			
			if(profile.peorodicalBackupTimeOut > 0) {
				Configuration.PROCESSING = true;
				
				Thread readerThread = new Thread() {
				    public void run() {
				    	
				    	int timer = 0;
				    	
				    	Logger.printWithTime("Backuping Deamon Started.");
				    	while(Configuration.PROCESSING) {
				    		timer++;
				    		if(timer > profile.peorodicalBackupTimeOut) {
				    			
				    			String sourceBackupZipFilePath = new String(profile.targetFolderPath + File.separator + HighLevelClock.getDateForFile() + "_" + profile.preferredFilesName + "_standard_backup.zip");
				    			
				    			ZipTask periodicalBackup = new ZipTask(profile.sourceFolderPath, sourceBackupZipFilePath);
				    			if(periodicalBackup.isValid()) {
									Logger.printWithTime("Saved!");
								}
								else {
									Logger.printErrorWithTime("Save Failed!");
								}
				    			timer = 0;
				    		}
				    		waitSeconds(1);
				    	}
				    	Logger.printWithTime("Backuping Deamon Stopped.");
				    }
				};
				readerThread.start();
				
				Scanner sc = new Scanner(System.in);
				String input = null;
				
				do {
					input = sc.nextLine();
					if(input.length() > 0) {
						if(input.toLowerCase().equals("exit") || input.toLowerCase().equals("quit")) {
							Configuration.PROCESSING = false;
							input = null;
							Logger.printWithTime("Bye!");
						}
						else if(input.toLowerCase().equals("save")) {
							
							Logger.printWithTimeNoNewLine("Saving manually...");
							
			    			String manualSaveSourceBackupZipFilePath = new String(profile.targetFolderPath + File.separator + HighLevelClock.getDateForFile() + "_" + profile.preferredFilesName + "_standard_backup.zip");
			    			
			    			ZipTask manualSaveBackupZipTask = new ZipTask(profile.sourceFolderPath, manualSaveSourceBackupZipFilePath);
			    			if(manualSaveBackupZipTask.isValid()) {
								System.out.println(" Done!");
							}
							else {
								System.err.println(" Save Failed!");
								// TODO: log here manual save error 
							}
						}
						else if(input.toLowerCase().equals("h") || input.toLowerCase().equals("help")) {
							System.out.println("Available Commands:\n\nhelp or h - print this\nsave - manual save\nexit - stop service execution\n\n");
						}
						else {
							Logger.printWithTime("Unknown command: " + input);
							System.out.println();
						}
					}
					
				} while(input != null || Configuration.PROCESSING);
				
			}
			
			// Successful program exit
			return true;
		}
		Logger.printErrorWithTime("Warning! Can't find any Saves to restore inside: " + profile.targetFolderPath);
		return false; //System.out.println();
	}
	
	public static String extractProfileName(String[] args, int idx, String profileName) {
		if(profileName != null) { return profileName; }
		if(args != null && args.length > 0) {
			int size = args.length;
			int nextOne = (idx + 1);
			int nextTwo = (idx + 2);
			if(nextOne < size) {
				String optOne = args[nextOne];
				if(optOne != null && optOne.length() > 0) {
					if(nextTwo < size && optOne.equals("=")) {
						return args[nextTwo];
					}
					return optOne;
				}
			}
			
		}
		return null;
	}
	
	public static String extractProfileName(String arg, String profileName) {
		if(profileName != null) { return profileName; }
		if(arg != null && arg.length() > 0) {
			if(arg.contains("profile")) {
				String[] parts = arg.split("=");
				int length = parts.length;
				if(parts.length > 1 && parts[0].trim().endsWith("profile")) {
					StringBuilder out = new StringBuilder();
					boolean first = true;
					for (int i = 1; i < length; i++) {
						out.append(parts[i]);
						if(!first) {
							out.append("=");
						}
						first = false;
					}
					return out.toString();
				}
			}
		}
		return null;
	}
	
	
	public static boolean processTestMode(String[] args, int idx) {
		if(args != null) {
			int argsSize = args.length;
			boolean oneExist = ((idx + 1) < argsSize);
			boolean twoExist = ((idx + 2) < argsSize);
			if(oneExist && twoExist) {
				Tests.testZipping(args[idx+1], args[idx+2]);
			}
			else {
				System.err.println("ERROR! Cant retrive " + (oneExist ? "1st" : "") + (twoExist ? ((oneExist ? " and " : "") + "2nd") : "") + " test parameter" + ((oneExist && twoExist) ? "s." : "."));
				System.err.println("    Usage: -t SOURSE_FOLDER_PATH OUTPUT_STORAGE_FOLDER_PATH");
				System.exit(-1);
			}
		}
		System.err.println("ERROR! Supplied ARGS are NULL!");
		System.exit(-1);
		return false;
	}

	public static OS getOperetionSystemType() {
		OS currentOS = OS.LINUX;
        String osName = System.getProperty("os.name");
        if(osName.toLowerCase().contains("mac")) {
        	currentOS = OS.MAC;
        }
        else if(osName.toLowerCase().contains("win")) {
        	currentOS = OS.WIN;
        }
        return currentOS;
	}
	
	public static boolean waitSeconds(int seconds) {
		if(seconds > 0) {
			try {
				Thread.sleep(1000 * seconds);
				return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
