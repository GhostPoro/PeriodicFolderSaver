package tools;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import holders.Configuration;
import holders.Configuration.OS;

public class Utils {
	
	public static boolean openFileInSystem(String path) {
		return openFileInSystem(new File(path));
	}
	
	public static boolean openFileInSystem(File file) {
        // first check if Desktop is supported by Platform or not
        if(!Desktop.isDesktopSupported()) {
            System.err.println("Desktop is not supported");
            return false;
        }
        if(file.exists()) {
        	try {
				Desktop.getDesktop().open(file);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
        }
		return false;
	}
    
    public static String readFileToLine(String fileName) {

		StringBuilder shaderSource = new StringBuilder();
		
    	FileReader fr = null;
    	BufferedReader br = null;
		
		try{
	    	fr = new FileReader(new File(fileName));
	    	br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null){
				shaderSource.append(line).append("\n");
			}
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
		finally {
			try {
				if (br != null) { br.close(); }
				if (fr != null) { fr.close(); }
			} catch (IOException e) { e.printStackTrace(); }
		}
		return shaderSource.toString();
	
    }

    public static List<String> readAllLines(File file, String filePath) {
    	
    	FileReader fr = null;
    	BufferedReader br = null;
    	
    	if (file.exists()) {
	        List<String> list = new ArrayList<>();
	        //System.out.println("Utils.fileName = " + fileName);
	        try {
	        	fr = new FileReader(file);
	        	br = new BufferedReader(fr);
	        	
	            String line;
	            while ((line = br.readLine()) != null) {
	            	if (!line.startsWith("#") && line.length() > 0) {
	            		list.add(line);
		        	}
	            }
	        } catch (IOException ioe) {
				ioe.printStackTrace();
			}
	        finally {
	        	try {	
	        			if (br != null) { br.close(); }
	        			if (fr != null) { fr.close(); }
					} catch (IOException e) { e.printStackTrace(); }
	        }
	        return list;
    	}
    	else {
    		System.out.println("Error! File " + filePath + " Unexist!");
    		return null;
    	}
        
    }
    
    public static boolean writeLinesToFile(String fileName, List<String> lines) {
    	StringBuilder sb = new StringBuilder();
    	int length = lines.size();
    	for (int li = 0; li < length; li++) { sb.append(((li == 0) ? "" : "\n") + lines.get(li)); }
    	return writeLinesToFile(fileName, sb.toString());
    }
    
    public static boolean writeLinesToFile(String fileName, String line) {
    	try {
    		FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(fileName);
		        outputStream.write(new String(line).getBytes());
		        outputStream.close();
			}
			finally { if (outputStream != null) { outputStream.close(); } }
    	}
		catch (IOException e) { e.printStackTrace(); }
    	return true;
    }
    
    public static String quotePath(String path) {
    	if(path != null && path.startsWith("\"") && path.endsWith("\"")) {
    		return fixingEncoding(path);
    	}
    	return ("\"" + fixingEncoding(path) + "\"");
    }
    
    public static String maskQuotes(String line) {
    	OS os = Configuration.os;
    	return ((os == OS.WIN) ? line : line.replace("\"", "\\\""));
    }
    
    public static String trimSpaces(String text) {
    	return text.replaceAll("\t", " ").replaceAll("^ +| +$|( )+", "$1").trim();
    }
    

    public static String fixingEncoding(String input) {
		try {
			return new String(input.getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return input;
    }
    
    public static String getExtensionFromPath(String path) {
    	if(path != null) {
    		int pos = path.lastIndexOf('.');
    		if(pos > 0 && ((pos + 1) < path.length()) ) {
    			return path.substring(pos + 1);
    		}
    	}
    	return "";
    }
    
    public static String addZerosToName(int num, int maxLength) {
    	String numString = Integer.toString(num);
    	int numLength = numString.length() + 1;
    	int size = maxLength - numLength;
    	while(size >= 0) {
    		numString = "0" + numString;
    		size--;
    	}
    	return numString;
    }
    
    public static String numericName(int num, int max) {
    	int maxLength = Integer.toString(max).length();
    	String numString = Integer.toString(num);
    	int numLength = numString.length() + 1;
    	
    	int size = maxLength - numLength;
    	while(size >= 0) {
    		numString = "0" + numString;
    		size--;
    	}
    	
    	return numString;
    }
    
    public static int[] fillArray(int[] array, int what) {
    	int size = array.length;
    	for (int i = 0; i < size; i++) {
			array[i] = what;
		}
    	return array;
    }
    
    public static int[][] fillArray(int[][] array, int what) {
    	int sizeX = array.length;
    	for (int x = 0; x < sizeX; x++) {
    		int sizeY = array[0].length;
    		for (int y = 0; y < sizeY; y++) {
    			array[x][y] = what;
    		}
		}
    	return array;
    }
    
    public static boolean inArray(int[] array, int val) {
    	if(array == null) { return false; }
    	int asize = array.length;
    	if(asize < 1) { return false; }
    	for (int i = 0; i < asize; i++) {
			if(val == array[i]) {
				return true;
			}
		}
    	return false;
    }
    
    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }
    
    public static int[] listIntToArray(List<Integer> list) {
        int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
        return result;
    }
    

}
