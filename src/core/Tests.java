package core;

import java.io.File;

import tools.FileTool;
import tools.Hash;
import tools.ZipTask;

public class Tests {
	
	public static boolean testZipping(String source, String storringLocation) {
		
		long curOperationNum = System.nanoTime();
		
		if(source == null || source.length() == 0) {
			System.out.println("Test is NOT Initialized! Reason: Test location of files is NULL!");
			System.exit(-1);
			return false;
		}
		
		File sourceFolderFile = new File(source);
		if(!sourceFolderFile.exists()) {
			System.out.println("Test is NOT Initialized! Reason: Test location of files not exist!");
			System.exit(-1);
			return false;
		}
		
		if(storringLocation == null || storringLocation.length() == 0) {
			System.out.println("Test is NOT Initialized! Reason: Output location for files is NULL!");
			System.exit(-1);
			return false;
		}
		
		String[] inputFileList = FileTool.storeFilesPathesFromFolder(sourceFolderFile);
		
		String archivePath = (storringLocation + "/" + curOperationNum + "/archive.zip");
		String outPath = (storringLocation + "/" + curOperationNum + "/out");
		
		File outputLocationFile = new File(outPath);
		if(!outputLocationFile.mkdirs()) {
			System.out.println("Test is NOT Initialized! Reason: Can't create output folder location: " + outPath);
			System.exit(-1);
			return false;
		}
		
		int sourceFilesNum = inputFileList.length;
		int passedChecks = 0;
		
		if(new ZipTask(source, archivePath, inputFileList, source.length() + (source.endsWith(File.separator) ? 1 : 0)).isValid()) {
			if(ZipTask.unzip(archivePath, outPath)) {
				System.out.println("Collecting unzipped files...");
				String[] outFileList = FileTool.storeFilesPathesFromFolder(outPath);
				
				System.out.println("Comparing... " + outFileList.length);
				for (String sourceFile : inputFileList) {
					for (String unpackedFile : outFileList) {
						if(sourceFile.replace("\\", "/").endsWith(unpackedFile.replace("\\", "/").replace(outPath.replace("\\", "/"), ""))) {
							String hashS = Hash.SHA512.checksum(sourceFile);
							String hashU = Hash.SHA512.checksum(unpackedFile);
							if(hashS.equals(hashU)) {
								System.out.println("GOOD: " + unpackedFile);
								passedChecks++;
							}
							else {
								System.err.println("\nBAD S: " + sourceFile + " | O: " + unpackedFile);
								System.err.println("HS: " + hashS);
								System.err.println("HU: " + hashU);
								System.err.println();
							}
						}
					}
				}
				
				if(passedChecks == sourceFilesNum) {
					System.out.println("Test Done! Successfully Validated " + passedChecks + "/" + sourceFilesNum + " Files."); 
				}
				else if (passedChecks > 0) {
					System.out.println("Test Done! WARNING!!! File Validation is partial: " + passedChecks + "/" + sourceFilesNum);
				}
				else {
					System.out.println("Test Done! CANT VALIDATE ANY FILE!"); 
				}
				System.exit(0);
			}
		}
		
		System.err.println("Test Did not Start!");
		System.exit(-1);
		
		return false;
	}

}
