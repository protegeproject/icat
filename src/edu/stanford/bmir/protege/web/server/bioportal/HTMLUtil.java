package edu.stanford.bmir.protege.web.server.bioportal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTMLUtil {

    private final static String PLACEHOLDER = " ... ";

    public static String makeHTMLLinks(String text) {
        return makeHTMLLinks(text, 0);
    }

    public static String makeHTMLLinks(String text, int maxLength) {
            Matcher matcher = Pattern.compile("(?i)(\\b(http://|https://|www.|ftp://|file:/|mailto:)\\S+)(\\s*)").matcher(text);

            if (matcher.find()) {
                String url = matcher.group(1);
                String endingSpaces = matcher.group(3);

                Matcher dotEndMatcher = Pattern.compile("([\\W&&[^/]]+)$").matcher(url);

                //Ending non alpha characters like [.,?%] shouldn't be included in the url.
                String endingDots = "";
                if (dotEndMatcher.find()) {
                    endingDots = dotEndMatcher.group(1);
                    url = dotEndMatcher.replaceFirst("");
                }

                text = matcher.replaceFirst("<a href='" + url + "'>" + reduceSize(url, maxLength)
                        + "</a>" + endingDots + endingSpaces);
            }
            return text;
    }

     private static String reduceSize(String text, int maxLength) {
            String res = text;
            if (text.length() > maxLength && maxLength > PLACEHOLDER.length()) {
                int firstPartLength = (maxLength - PLACEHOLDER.length()) * 4 / 5;
                if (firstPartLength == 0) {
                    firstPartLength = 1;
                }
                res = text.substring(0, firstPartLength);
                res += PLACEHOLDER;
                res += text.substring(text.length() - (maxLength - res.length()));
            }
            return res;
     }

     public static String replaceEOF(String text) {
         return text == null ? null : text.replaceAll("\n", "<br>");
     }

     public static String replaceSpaces(String text) {
         return text.replaceAll(" ", "%20");
     }

     public static String encodeURI(String text) throws UnsupportedEncodingException {
         return URLEncoder.encode(text, "UTF-8").toString().replaceAll("\\+", "%20");
     }

}
