package com.mycompany;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import lombok.val;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TessUtil {

	public static void main(String[] args) throws FileNotFoundException, IOException, TesseractException {
		extractOcrData(args[0]);
	}

	public static String extractOcrData(String pdfFile) throws IOException, FileNotFoundException, TesseractException {
		String regex = IOUtils.toString(TessUtil.class.getResourceAsStream("/copyright-regex.txt"));

		File image = new File(pdfFile);
		val tessInst = new Tesseract();
		tessInst.setLanguage("eng");
		String result = tessInst.doOCR(image).replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("( )+", " ")
				.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\"", "").replaceAll("”", "")
				.replaceAll("\\[", "").replaceAll("\\]", "").trim();
		
		System.out.println("result=" + result);

		val map = getData(regex, result);

		val temData = IOUtils.toString(TessUtil.class.getResourceAsStream("/copyright-template.tem"));

		DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String date = sdf.format(new Date());

		String temp = String.format(temData, date, map.get("licensee"), map.get("licenseeState"),
				map.get("licenseeEntityType"), map.get("licenseeAddress"), map.get("licensor"),
				map.get("licensorState"), map.get("licensorEntityType"), map.get("licensorAddress"),
				map.get("territory"), map.get("purposeDescription"), map.get("workDescription"), map.get("amountText"),
				map.get("amount"), map.get("paymentProcedure"));

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
