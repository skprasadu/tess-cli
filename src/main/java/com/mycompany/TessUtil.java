package com.mycompany;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import lombok.val;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TessUtil {

	public static void main(String[] args) throws FileNotFoundException, IOException, TesseractException {
		extractOcrData(args[0]);
	}

	public static Map<String, String> extractOcrData(String pdfFile)
			throws IOException, FileNotFoundException, TesseractException {
		String regex = getRegex(pdfFile);
		System.out.println("regex=" + regex);

		System.out.println("==============================================");

		String result = getString(pdfFile).replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("'", "")
				.replaceAll("-", "")/*.replaceAll("_", "")*/.replaceAll("( )+", " ").replaceAll("”", "").trim();

		System.out.println("result=" + result);

		System.out.println("==============================================");

		val map = getData(regex, result);

		System.out.println(map);
		return map;
	}

	public static String getRegex(String pdfFile) {
		String[] split = pdfFile.split("\\.");

		String detault = "\\\\w\\*";

		try {
			String tempStr = IOUtils.toString(new FileInputStream("./" + split[0] + ".tem"));
	        System.out.println("regex=" + tempStr); // prints (o,yui)

			String regex = createUniquePlaceholder(tempStr).replaceAll("\r", " ").replaceAll("\n", " ")
					.replaceAll("'", "").replaceAll("-", "")/*.replaceAll("_", "")*/.replaceAll("( )+", " ")
					.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\"", "")
					.replaceAll("\\.", "\\\\.").replaceAll("\\{\\{", "\\(\\?\\<")
					.replaceAll("\\}\\}", "\\>" + detault + "\\)").trim();

			val set1 = getMatch("<twoWords\\w*>", regex);
			for (String st : set1) {
				regex = regex.replaceAll(st + "\\\\w\\*", st + "\"\\\\w\\* \\\\w\\*\"");
			}

			val set = getMatch("<date\\w*>", regex);

			val set2 = getMatch("<others\\w*>", regex);

			val set3 = getMatch("<num\\w*>", regex);
			for (String st : set) {
				regex = regex.replaceAll(st + "\\\\w\\*", st + "\\.\\*");
			}
			for (String st : set2) {
				regex = regex.replaceAll(st + "\\\\w\\*", st + "\\.\\*");
			}
			for (String st : set3) {
				regex = regex.replaceAll(st + "\\\\w\\*", st + "\\.\\*");
			}

			return regex;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static String createUniquePlaceholder(String tempStr) {
		Pattern p = Pattern.compile("\\{\\{\\w*\\}\\}");
		Matcher m = p.matcher(tempStr);

		StringBuffer buf = new StringBuffer(tempStr);

		int count = 0;
		while (m.find()) {
			count++;
			String num = String.format("%02d", count);
			String newStr = m.group().substring(0, m.group().length() - 4) + num + "}}";
			buf.replace(m.start(), m.end(), newStr);
		}

		return buf.toString();
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

	public static Set<String> getMatch(String regex, String data) {

		val set = new HashSet<String>();

		val r = Pattern.compile(regex);
		val matcher = r.matcher(data);

		while (matcher.find()) {
			set.add(matcher.group());
		}

		return set;
	}

	private static Set<String> getNamedGroupCandidates(String regex) {
		val namedGroups = new TreeSet<String>();

		val m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);

		while (m.find()) {
			namedGroups.add(m.group(1));
		}

		return namedGroups;
	}

	public static String getString(String pdfFile) {
		if (pdfFile.endsWith(".pdf")) {
			File image = new File(pdfFile);
			val tessInst = new Tesseract();
			tessInst.setLanguage("eng");
			tessInst.setTessVariable("user_defined_dpi", "300");
			String result = "";
			try {
				result = tessInst.doOCR(image);
		        System.out.println("data=" + result); // prints (o,yui)

			} catch (TesseractException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		} else {

			try {
				return IOUtils.toString(new FileInputStream(pdfFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}
	}
}
