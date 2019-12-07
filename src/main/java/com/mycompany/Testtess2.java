package com.mycompany;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import lombok.val;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Testtess2 {

	public static void main(String[] args) throws FileNotFoundException, IOException, TesseractException {
		extractOcrData(args[0]);
	}

	private static String extractOcrData(String pdfFile) throws IOException, FileNotFoundException, TesseractException {
		String regex = IOUtils.toString(new FileInputStream("./regex.txt"));

		File image = new File(pdfFile);
		val tessInst = new Tesseract();
		tessInst.setLanguage("eng");
		String result = tessInst.doOCR(image).replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("( )+", " ")
				.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\"", "").replaceAll("”", "")
				.replaceAll("\\[", "").replaceAll("\\]", "").trim();

		val map = getData(regex, result);

		System.out.println(map);

		val temData = IOUtils.toString(new FileInputStream("./template.tem"));

		val r = Pattern.compile("\\[\\{(?<name>\\w*)\\}\\]");
		val matcher = r.matcher(temData);

		String temp = temData;
		while (matcher.find()) {
			temp = temp.replaceAll("\\[\\{" + matcher.group(1) + "\\}\\]", map.get(matcher.group(1)));
		}

		System.out.println(temp);
		return temp;
	}

	public static Map<String, String> getData(String regex, String data) {

		val map = new HashMap<String, String>();

		val r = Pattern.compile(regex);
		val matcher = r.matcher(data);

		val set = getNamedGroupCandidates(regex);

		while (matcher.find()) {
			for (String st : set) {
				map.put(st, matcher.group(st));
			}
		}

		return map;
	}

	private static Set<String> getNamedGroupCandidates(String regex) {
		val namedGroups = new TreeSet<String>();

		val m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);

		while (m.find()) {
			namedGroups.add(m.group(1));
		}

		return namedGroups;
	}
}
