package com.mycompany;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.apache.commons.io.IOUtils;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import lombok.val;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TessUtil2 {

	public static void main(String[] args) throws FileNotFoundException, IOException, TesseractException {
		String regex = getRegex(args[0]).replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("( )+", " ");
		String data = getString(args[0]).replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("( )+", " ");
		
		if(args.length > 1) {
			System.out.println("regex=" + regex);
			System.out.println("data=" + data);
		}
	    DiffMatchPatch dmp = new DiffMatchPatch();
	    LinkedList<DiffMatchPatch.Diff> diff = dmp.diffMain(regex, data);
	    
	    LinkedList<DiffMatchPatch.Diff> diff1 = new LinkedList<>();
	    
	    dmp.diffCleanupSemantic(diff);
	    
	    boolean entered = false;
	    boolean keyFound = false;
	    LinkedHashMap<String, String> lhp = new LinkedHashMap<>();
	    String currentKey = "";
	    for(DiffMatchPatch.Diff d : diff) {
	    	//check if exact Match found 
	    	String text = d.text.trim();
	    	if(d.operation == DiffMatchPatch.Operation.DELETE ){
				if(Pattern.matches("\\{\\{\\w*\\}\\}", text)) {
					currentKey = text;
					keyFound = true;
				} else if(d.text.trim().startsWith("{{")) {
		    		diff1.add(d);
	    			entered = true;
	    		} 
	    	}
    		if(d.operation == DiffMatchPatch.Operation.INSERT) {
	    		if(keyFound) {
	    			lhp.put(currentKey.substring(2, currentKey.length() - 2), text);
	    			currentKey = "";
	    			keyFound = false;
	    		} else if(entered) {
    	    		diff1.add(d);
    	    		entered = false;
    			}
    		}
	    }
	    
	    LinkedList<DiffMatchPatch.Diff> diff2 = new LinkedList<>();
		DiffMatchPatch.Diff currentDiff= null;
	    for(DiffMatchPatch.Diff d : diff1) {
			//if any one does not exist
				//add it and its next element to a different linked list list
	    	if(currentDiff != null) {
	    		diff2.add(currentDiff);
	    		diff2.add(d);
	    		currentDiff = null;
	    	}
	    	val set = getMatch("\\{\\{(\\w*)\\}\\}", d.text.trim(), 1);
	    	if(!set.isEmpty()) {
	    		//check if any element in the set is already in the linkedhashmap list, if yes
	    		for(String s: set) {
	    			if(!lhp.keySet().contains(s)) {
	    				currentDiff = d;
	    				break;
	    			}
	    		}
	    		
	    	}
	    }
	    System.out.println("diff2=" + diff2);
	    // for each element in the new list, 
	    	//check which ones does not exist in the hashmap, and 
	    	//try to extract the key/ value from it and its subsequent lists
	    
	    System.out.println(lhp);
	}

	private static String getRegex(String pdfFile) throws FileNotFoundException, IOException {
		String[] split = pdfFile.split("\\.");

		String regex = IOUtils.toString(new FileInputStream("./" + split[0] + ".tem"));
		return regex;
	}

	private static String getString(String pdfFile) throws TesseractException, FileNotFoundException, IOException {
		if (pdfFile.endsWith(".pdf")) {
			File image = new File(pdfFile);
			val tessInst = new Tesseract();
			tessInst.setLanguage("eng");
			tessInst.setTessVariable("user_defined_dpi", "300");
			String result = "";

			result = tessInst.doOCR(image);

			return result;
		} else {
			return IOUtils.toString(new FileInputStream(pdfFile));
		}
	}
	
	public static Set<String> getMatch(String regex, String data, int index) {

		val set = new HashSet<String>();

		val r = Pattern.compile(regex);
		val matcher = r.matcher(data);

		while (matcher.find()) {
			set.add(matcher.group(index));
		}

		return set;
	}

}
