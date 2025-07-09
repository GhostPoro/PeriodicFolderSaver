package tools;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import holders.Configuration;
import holders.Profile;

public class ZipTask {

	//private final List<String> filesListInDir;

	private final String sourcePath;
	private final String outZipFilePath;
	
	private final File sourceLocationFile;
	
	private final boolean valid;
	
	public ZipTask(String inputLocationPath, String outputZipFilePath) {
		this(inputLocationPath, outputZipFilePath, null, -1);
	}
	
	public ZipTask(String inputLocationPath, String outputZipFilePath, String[] inputFileList, int sourceLength) {

		boolean inPathValid = false;
		boolean outPathValid = true;

		// input location path option errors
		if (inputLocationPath == null) {
			Logger.printError("ERROR! (ZipTask) Input Location Path is NULL!");
		}
		else if (inputLocationPath.length() == 0) {
			Logger.printError("ERROR! (ZipTask) Input Location Path is EMPTY!");
		}
		else {
			// initialize Input Location File, if String Path OK
			inPathValid = true;
		}
		
		// set final INPUT variables
		if(!inPathValid) {
			Logger.printError("Input Location Path: " + inputLocationPath);
			sourceLocationFile = null;
			sourcePath = null;
		}
		else {
			sourcePath = inputLocationPath;
			sourceLocationFile = new File(sourcePath);
		}
		
		// if Path OK, but File not exist
		if(inPathValid && !sourceLocationFile.exists()) {
			Logger.printError("ERROR! (ZipTask) Input File/Directory (" + sourceLocationFile.getAbsolutePath() + ") NOT EXIST!");
			inPathValid = false;
		}

		// output file path option errors
		if (outputZipFilePath == null) {
			Logger.printError("Warning! (ZipFiles->zip) Specified output zip file path is NULL. Will use generated from input path (if possible)");
			outPathValid = false;
		}
		else if (outputZipFilePath.length() == 0) {
			Logger.printError("Warning! (ZipFiles->zip) Specified output zip file path is empty. Will use generated from input path (if possible)");
			outPathValid = false;
		}
		
		if(!outPathValid) {
			if(inPathValid) {
				outZipFilePath = getOutputFileNameFromLocation(sourceLocationFile);
				Logger.printError("Generated Path For Output Zip File: " + outZipFilePath);
			}
			else {
				outZipFilePath = null;
			}
			
		}
		else {
			outZipFilePath = outputZipFilePath;
		}
		
		if(inPathValid && outZipFilePath != null) {
			//this.filesListInDir = new ArrayList<String>();
			
			File outZipFile = new File(outZipFilePath);
			String testOutZipFilePath = null;
			
			// create new name for file if target file exist
			while(outZipFile.exists()) {
				testOutZipFilePath = outZipFile.getAbsolutePath();
				
				if(testOutZipFilePath.toLowerCase().endsWith(".zip")) {
					testOutZipFilePath = (testOutZipFilePath.substring(0, testOutZipFilePath.length() - 4) + "_" + System.currentTimeMillis() + ".zip");
				}
				else {
					testOutZipFilePath = (testOutZipFilePath + "_" + System.currentTimeMillis() + ".zip");
				}
				
				outZipFile = new File(testOutZipFilePath);
			}
			
			if(inputFileList == null || inputFileList.length == 0) {
				inputFileList = FileTool.storeFilesPathesFromFolder(sourceLocationFile);
				sourceLength = (sourceLocationFile.getAbsolutePath().length() + 1);
			}
			
			this.valid = this.zip(inputFileList, outZipFile, sourceLength);
		}
		else {
			//this.filesListInDir = null;
			this.valid = false;
		}
	}
	
	public boolean isValid() {
		return valid;
	}

	public String getSourceLocationPath() {
		return sourcePath;
	}

	public String getOutputZipFilePath() {
		return outZipFilePath;
	}

	private String getOutputFileNameFromLocation(File inFile) {
		if(inFile == null) { return null; }
		if(inFile.exists()) { return inFile.getAbsolutePath(); }
		return null;
	}

	/**
	 * This method zips the directory
	 * 
	 * @param sourceFile
	 * @param outputZipFile
	 */
	private boolean zip(String[] filesToArchive, File outputZipFile, int startLength) {
		if(filesToArchive.length == 0) { return false; }
		try {
			//this.populateFilesList(sourceFile);
			
			// make sure that is final location for zip exist
			String outZipFilePath = outputZipFile.getAbsolutePath();
			int endFolderSplitterIndex = outZipFilePath.lastIndexOf(File.separatorChar);
			if(endFolderSplitterIndex > 0) {
				File saveFolderLocation = new File(outZipFilePath.substring(0, endFolderSplitterIndex));
				if(saveFolderLocation.exists()) {
					// continue
				}
				else if(saveFolderLocation.mkdirs()) {
					// create n continue
				}
				else {
					Logger.printError("ERROR! (ZipFiles->zip) Can't create Parent directries for saving Zip File: " + outZipFilePath);
					return false;
				}
			}
			else {
				Logger.printError("ERROR! (ZipFiles->zip) Can't save '" + outZipFilePath + "' zip! Reason: Can't extract save location.");
				return false;
			}
			
			int fileWaitTime = (Profile.getIntValue("read_file_timeout_sec") * 1000);
			
			// now zip files one by one
			// create ZipOutputStream to write to the zip file
			FileOutputStream fos = new FileOutputStream(outputZipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			
			// process collected files
			//int size = filesListInDir.size();
			int size = filesToArchive.length;
			for (int fpi = 0; fpi < size; fpi++) {
				//String currentFilePath = filesListInDir.get(fpi);
				String currentFilePath = filesToArchive[fpi];
				
				String insideFileName = currentFilePath.substring(startLength, currentFilePath.length());
				if(insideFileName.startsWith("/") || insideFileName.startsWith("\\")) {
					insideFileName = insideFileName.substring(1);
				}
				
				//System.out.println("Zipping: " + currentFilePath + " AS " + insideFileName);
				
				// for ZipEntry we need to keep only relative file path, so we used substring on absolute path
				zos.putNextEntry(new ZipEntry(insideFileName));
				
				FileInputStream fis = null;
				int readErrors = 0;
				boolean readingFile = false;
				
				do {
					try {
						// attempting to read source file
						readingFile = true;
						
						// read the file and write to ZipOutputStream
						fis = new FileInputStream(currentFilePath);
						
						if(fis != null) {
							int len; byte[] buffer = new byte[1024];
							while ((len = fis.read(buffer)) > 0) {
								zos.write(buffer, 0, len);
							}
							
							// done reading file
							readingFile = false;
						}
					}
					catch (FileNotFoundException e) {
						readErrors++;
						Thread.sleep(fileWaitTime);
					}
				} while (readingFile && readErrors < Configuration.MAX_READ_ERRORS);
				
				if(readErrors >= Configuration.MAX_READ_ERRORS) {
					Logger.printErrorWithTime("WARNING!!! Can't access file '" + currentFilePath + "' Skipping...");
				}
				
				if(readErrors > 0) {
					Logger.printErrorWithTime("WARNING!!! During Backup appear some (" + readErrors + ") errors!");
				}
				
				zos.closeEntry();
				if(fis != null) { fis.close(); }
			}
			
			zos.close();
			fos.close();
			
			return true;
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public static boolean unzip(String zipFilePath, String destDir) {
		
		if(zipFilePath == null || zipFilePath.length() == 0) {
			Logger.printError("ERROR! (ZipFiles->unzip) Invalid Input Zip File Path: " + zipFilePath);
			return false;
		}
		
		File inputZipFile = new File(zipFilePath);
		if(!inputZipFile.exists()) {
			Logger.printError("ERROR! (ZipFiles->unzip) No Zip File: " + inputZipFile.getAbsolutePath());
			return false;
		}
		
		File dir = new File(destDir);

		// create output directory if it doesn't exist
		if (!dir.exists()) { dir.mkdirs(); }

		FileInputStream fis;
		
		// buffer for read and write data to file
		byte[] buffer = new byte[1024];
		
		try {
			
			fis = new FileInputStream(inputZipFile);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			
			while (ze != null) {
				
				String fileName = ze.getName();
				File newFile = new File(destDir + File.separator + fileName);
				//System.out.println("Unzipping to " + newFile.getAbsolutePath());
				
				// create directories for sub directories in zip
				int len = 0; new File(newFile.getParent()).mkdirs();
				
				FileOutputStream fos = new FileOutputStream(newFile);
				
				int readErrors = 0;

				do {
					try { len = zis.read(buffer); } catch (EOFException e) { readErrors++; }
					if(len > 0) { fos.write(buffer, 0, len); }
				} while (len > 0);
				
				if(readErrors > 0) {
					Logger.printError("ERROR! (ZipFiles->unzip) Unexpected end of ZLIB input stream. Read Errors (" + readErrors + ") in File: " + fileName);
				}
				
				// close this ZipEntry
				fos.close();
				try {
					zis.closeEntry();
					ze = zis.getNextEntry();
				} catch (EOFException e) {
					ze = null;
				}
				
			}
			
			// close last ZipEntry
			zis.closeEntry();
			zis.close();
			fis.close();
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

}
