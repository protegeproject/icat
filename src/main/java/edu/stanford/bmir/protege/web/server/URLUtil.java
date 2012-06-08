package edu.stanford.bmir.protege.web.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class URLUtil {

	public static BufferedReader read(String url) throws Exception {
		return new BufferedReader(
				new InputStreamReader(
						new URL(url).openStream()));
	}

	public static String getURLContent(String url) {
		StringBuffer urlString = new StringBuffer();

		try {
			BufferedReader reader = read(url);
			String line = reader.readLine();

			while (line != null) {
				urlString.append(line);
				line = reader.readLine(); 
			}
		} catch (Exception e) {			
		}
		return urlString.toString();
	}

	public static void main (String[] args) throws Exception{
		//BufferedReader reader = read(args[0]);
		String url = "http://rest.bioontology.org/bioportal/search/Thyroiditis"; 
		System.out.println(getURLContent(url));
	}

}
