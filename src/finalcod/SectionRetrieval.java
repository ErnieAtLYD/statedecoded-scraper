/**
* The FinalCOD program implements an application that parses Municipal Ordinances 
* web page HTML source code to a XML format. This application is not meant for 
* anything other than it intended purpose. 
*
* @author  Cristhian Vanegas
* @author  Marco Biffi
* @version 1.0
* @since   2014-04-8
* Contact email: chris1582@gmail.com
*/
package finalcod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.io.StringReader;
import java.util.ArrayList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

/**
 *
 * @author Cristhian
 */
public class SectionRetrieval {

    public String line;
    String section;
    String History;
    String Prefix;
    String experation;
    int textCount = 0;

    StringBuilder sb = new StringBuilder();
    BufferedReader buffer;

    int mainSec = 0;
    String startingSection = "";
    String SBline;
    ArrayList<String> textSections = new ArrayList<>();
    private ArrayList sectionArray1 = new ArrayList<String>();
    private StringBuilder result = new StringBuilder();
    int Number = 0;
    private int nextNumber;
    private char orgChar;
    private String sourcelink = "";
    private String prefix;
    String Path = "";
    private String romanNumeralPattern;
    private String romanDigit;
    ArrayList<String[]> r = new ArrayList<>();
    String[] c;
    private int count;
    RomanNumerals rm = new RomanNumerals();
    /** Constructor. it add text unit next section on page. if no section Number it retrieves the entire body text.  */
    public SectionRetrieval(ArrayList list, String source, ArrayList titles, String slink, String Path) {
        try {
            sourcelink = slink;
            this.Path = Path;
            String src = source.replaceAll("&amp;", "and");
            StringReader s = new StringReader(src);
            buffer = new BufferedReader(s);
            line = buffer.readLine();

            int j = list.size() - 1;
            if (!list.isEmpty()) {
                while (!line.contains(list.get(j).toString())) {
                    line = buffer.readLine();
                }

                //goes through titles
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).equals("")) {
                        continue;
                    }
                    if (i + 1 < list.size()) {
                        result.append("<catch_line>" + titles.get(i).toString().substring(0, titles.get(i).toString().length() - 1) + "</catch_line>" + "\n");
                        line = buffer.readLine();

                        //read until next title
                        while (!line.contains(list.get(i + 1).toString())) {

                            sb.append(line + "\n");
                            line = buffer.readLine();

                        }

                        textTrim(sb);
                        determineTextOrder(textSections);
                    }
                    if (i == list.size()-1) {
                        result.append("<catch_line>" + titles.get(titles.size() - 1).toString().substring(0, titles.get(titles.size() - 1).toString().length() - 1) + "</catch_line>" + "\n");
                        line = buffer.readLine();
                        while (!line.contains("/html")) {
                            sb.append(line + "\n");
                            line = buffer.readLine();

                        }

                        textTrim(sb);
                        determineTextOrder(textSections);
                    }

                    sb.setLength(0);
                    textSections.clear();
                }
            } else {
                while (!line.contains("crumb")) {
                    line = buffer.readLine();
                }
                while (line != null) {
                    sb.append(line + "\n");
                    line = buffer.readLine();

                }
                textTrim(sb);
                result.append("<catch_line>" + "</catch_line>" + "\n");
                determineTextOrder(textSections);
            }
            result.append("</law>" + "\n");
        } catch (Exception e) {
            createErrorLog();
        }

    }
    /* Retrives the text between p tags and table tags */
    private void textTrim(StringBuilder sb) {

        String html = sb.toString();
        Document doc = Jsoup.parse(html);

        Elements para = doc.select("p, table");
        // Elements tables = doc.select("table");
        for (Element ptext : para) {
            Elements lineText = ptext.select("p");
            for (Element lText : lineText) {
                textSections.add(lText.text());
            }
            Elements tableText = ptext.select("table");
            for (Element tText : tableText) {
                textSections.add(tText.toString());
            }
        }

    }
    /** Determines the order of the text depending on there labeling  */
    public void determineTextOrder(ArrayList<String> text) {

        result.append("<text>" + "\n");
        textCount++;
        sectionArray1.clear();
        for (int i = 0; i < text.size(); i++) {

            if (text.get(i).matches("\\([0-9]{1,}\\)|[0-9]{1,}\\.?\\)?")) {

                if (text.get(i).matches("\\([0-9]{1,}\\)")) {

                    try {
                        prefix = text.get(i).replace("(", "").replace(")", "").replace(".", "");
                        result.append("<section prefix = \"" + prefix + "\">" + "\n");
                        String n = text.get(i).replace("(", "").replace(")", "").replace(".", "");
                        Number = Integer.parseInt(n);
                        nextNumber = Number + 1;

                        while (Number != nextNumber && i + 1 != text.size() && !text.get(i + 1).contains("(Ord.")) {

                            if (text.get(i + 1).matches("\\([0-9]{1,}\\)")) {
                                String n2 = text.get(i + 1).replace("(", "").replace(")", "").replace(".", "");

                                if (isNumeric(n2)) {
                                    int n2Int = Integer.parseInt(n2);
                                    if (n2Int == nextNumber) {
                                        Number = Integer.parseInt(n2);
                                    } else {
                                        i++;
                                        sectionArray1.add(text.get(i));

                                    }
                                }
                            } else {
                                i++;
                                sectionArray1.add(text.get(i));

                            }

                        }

                        determineSubsections(sectionArray1);
                        sectionArray1.clear();
                        result.append("</section>" + "\n");
                    } catch (Exception e) {

                    }
                } else if (text.get(i).matches("[0-9]{1,}\\.?\\)?")) {
                    try {

                        prefix = text.get(i).replace("(", "").replace(")", "").replace(".", "");
                        result.append("<section prefix = \"" + prefix + "\">" + "\n");
                        String n = text.get(i).replace("(", "").replace(")", "").replace(".", "");
                        Number = Integer.parseInt(n);
                        nextNumber = Number + 1;
                        while (Number != nextNumber && i + 1 != text.size() && !text.get(i + 1).contains("Ord.")) {

                            if (text.get(i + 1).matches("[0-9]{1,}\\.?\\)?")) {
                                String n2 = text.get(i + 1).replace("(", "").replace(")", "").replace(".", "");

                                if (isNumeric(n2)) {
                                    int n2Int = Integer.parseInt(n2);
                                    if (n2Int == nextNumber) {
                                        Number = Integer.parseInt(n2);
                                    } else {
                                        i++;
                                        sectionArray1.add(text.get(i));

                                    }
                                }
                            } else {
                                i++;
                                sectionArray1.add(text.get(i));

                            }

                        }

                        determineSubsections(sectionArray1);
                        sectionArray1.clear();
                        result.append("</section>" + "\n");
                    } catch (Exception e) {

                    }
                }

            } else if (text.get(i).matches("\\([A-Za-z]{1,}\\)|^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$")) {
                if (text.get(i).matches("\\([A-Za-z]{1,}\\)")) {

                    prefix = text.get(i).replace("(", "").replace(")", "").replace(".", "");
                    result.append("<section prefix = \"" + prefix + "\">" + "\n");
                    orgChar = text.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);
                    if (!isRomanNum(text)) {
                        char c2 = text.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);

                        c2++;

                        String nextorgChar = "";
                        for (int j = 0; j < prefix.length(); j++) {
                            nextorgChar += c2;
                        }

                        if (Character.isUpperCase(orgChar)) {
                            while (orgChar != nextorgChar.charAt(0) && prefix.length() == nextorgChar.length() && i + 1 != text.size() && !text.get(i + 1).contains("(Ord.")) {

                                if (text.get(i + 1).matches("\\([A-Za-z]{1,}\\)")) {

                                    String nextLetter = text.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (nextLetter.equals(nextorgChar)) {
                                        orgChar = c2;
                                    } else {
                                        i++;
                                        sectionArray1.add(text.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray1.add(text.get(i));

                                }

                            }
                        } else {
                            while (orgChar != nextorgChar.charAt(0) && prefix.length() == nextorgChar.length() && i + 1 != text.size() && !text.get(i + 1).contains("(Ord.")) {

                                if (text.get(i + 1).matches("\\([A-Za-z]{1,}\\)")) {

                                    String nextLetter = text.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (nextLetter.equals(nextorgChar)) {
                                        orgChar = c2;
                                    } else {
                                        i++;
                                        sectionArray1.add(text.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray1.add(text.get(i));

                                }

                            }
                        }
                    } else {

                        String n = text.get(i).replace("(", "").replace(")", "").replace(".", "");
                        orgChar = text.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);
                        if (Character.isUpperCase(orgChar)) {
                            if (isSingleRomanNumber(prefix)) {
                                Number = rm.valueOf(n);
                            }
                            nextNumber = Number + 1;

                            sectionArray1.clear();

                            while (Number != nextNumber && i + 1 < text.size() && !text.get(i).contains("Ord.")) {

                                if (text.get(i + 1).matches("\\([A-Za-z]{1,}\\)|^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$")) {

                                    String n2 = text.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (isSingleRomanNumber(n2)) {
                                        int n2Int = rm.valueOf(n2);
                                        if (n2Int == nextNumber) {
                                            Number = rm.valueOf(n2);
                                        } else {
                                            i++;
                                            sectionArray1.add(text.get(i));

                                        }
                                    } else {
                                        i++;
                                        sectionArray1.add(text.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray1.add(text.get(i));

                                }

                            }
                        } else {
                            if (isSingleRomanNumber(prefix)) {
                                Number = rm.valueOf(n);
                            }
                            nextNumber = Number + 1;

                            sectionArray1.clear();

                            while (Number != nextNumber && i + 1 < text.size() && !text.get(i).contains("Ord.")) {

                                if (text.get(i + 1).matches("\\([A-Za-z]{1,}\\)|^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$")) {

                                    String n2 = text.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (isSingleRomanNumber(n2)) {
                                        int n2Int = rm.valueOf(n2);
                                        if (n2Int == nextNumber) {
                                            Number = rm.valueOf(n2);
                                        } else {
                                            i++;
                                            sectionArray1.add(text.get(i));

                                        }
                                    } else {
                                        i++;
                                        sectionArray1.add(text.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray1.add(text.get(i));

                                }

                            }
                        }
                    }

                    determineSubsections(sectionArray1);
                    sectionArray1.clear();
                    result.append("</section>" + "\n");
                } else if (text.get(i).matches("^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$")) {

                    prefix = text.get(i).replace("(", "").replace(")", "").replace(".", "");
                    result.append("<section prefix = \"" + prefix + "\">" + "\n");
                    orgChar = text.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);
                    char c2 = text.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);

                    c2++;

                    String nextorgChar = "";
                    for (int j = 0; j < prefix.length(); j++) {
                        nextorgChar += c2;
                    }

                    if (Character.isUpperCase(orgChar)) {
                        while (orgChar != nextorgChar.charAt(0) && prefix.length() == nextorgChar.length() && i + 1 != text.size() && !text.get(i + 1).contains("(Ord.")) {

                            if (text.get(i + 1).matches("^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$")) {

                                String nextLetter = text.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                if (nextLetter.equals(nextorgChar)) {
                                    orgChar = c2;
                                } else {
                                    i++;
                                    sectionArray1.add(text.get(i));

                                }
                            } else {
                                i++;
                                sectionArray1.add(text.get(i));

                            }

                        }
                    } else {
                        while (orgChar != nextorgChar.charAt(0) && prefix.length() == nextorgChar.length() && i + 1 != text.size() && !text.get(i + 1).contains("(Ord.")) {

                            if (text.get(i + 1).matches("^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$")) {

                                String nextLetter = text.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                if (nextLetter.equals(nextorgChar)) {
                                    orgChar = c2;
                                } else {
                                    i++;
                                    sectionArray1.add(text.get(i));

                                }
                            } else {
                                i++;
                                sectionArray1.add(text.get(i));

                            }

                        }
                    }

                    determineSubsections(sectionArray1);
                    sectionArray1.clear();
                    result.append("</section>" + "\n");
                }
            } else if (text.get(i).contains("<table")) {
                count++;
                result.append("<section prefix=\"" + count + "\"" + " type=\"table\">" + "\n");
                String tablecode = text.get(i).replace("&times", "&#215").replace("&frac14", "&#188").replace("&frac12", "&#189").replace("&frac34", "&#190");
                String table = "";
                for (int j=0; j< tablecode.length(); j++)
                {
                    char character = tablecode.charAt(j);
                    String s = String.format ("\\u%04x", (int)character);
                  //  System.out.println(character);
                   // System.out.println(s);    
                    if (s.equals("\\u2002"))
                    {
                        table += " ";
                    }
                    else
                    table += character;
                    
                }
                result.append(table + "\n");
                result.append("</section>" + "\n");
            } else if (!text.get(i).equals("") && i < text.get(i).length() && !text.get(i).contains("(Ord.") && !text.get(i).contains("Editor's note") && !text.get(i).contains("FOOTNOTE")) {
                while (i < text.size() && !text.get(i).contains("(Ord.")) {
                    sectionArray1.add(text.get(i));
                    i++;
                }
                determineSubsections(sectionArray1);
                if (textCount % 2 == 1) {
                    result.append("</text>" + "\n");
                    textCount++;
                }

            } else if (text.get(i).contains("(Ord.") && !text.get(i).contains("Amendment")) {
                if (textCount % 2 == 1) {
                    result.append("</text>" + "\n");
                    textCount++;
                }
                result.append("<history>" + text.get(i) + "</history>" + "\n");
            } else if (text.get(i).toString().contains("Editor's note")) {
                if (textCount % 2 == 1) {
                    result.append("</text>" + "\n");
                    textCount++;
                }
                result.append("<EditorsNote>" + "\n");
                while (i + 1 < text.size() && !text.get(i + 1).contains("FOOTNOTE") && !text.get(i).contains("<table")) {
                    result.append(text.get(i + 1) + "\n");
                    i++;
                }
                result.append("</EditorsNote>" + "\n");

            } else if (text.get(i).contains("FOOTNOTE")) {
                if (textCount % 2 == 1) {
                    result.append("</text>" + "\n");
                    textCount++;
                }
                result.append("<footnote>" + "\n");
                while (i < text.size()) {
                    result.append(text.get(i).toString() + "\n");
                    i++;
                }

                result.append("</footnote>" + "\n");

            } else if (!text.get(i).contains("") && !text.get(i).contains("Cross reference")) {
                result.append(text.get(i) + "\n");
            } else if (text.isEmpty()) {
                if (textCount % 2 == 1) {
                    result.append("</text>" + "\n");
                    textCount++;
                }
            }
        }
       //if  (text.isEmpty()) {
            if (textCount % 2 == 1) {
                result.append("</text>" + "\n");
                textCount++;
            }
       // }

    }
    /** Recursively finds the text order for all sub label */
    private void determineSubsections(ArrayList<String> section) {

        ArrayList sectionArray2 = new ArrayList<String>();

        for (int i = 0; i < section.size(); i++) {
            if (section.get(i).matches("\\([0-9]{1,}\\)|[0-9]{1,}\\.?\\)?")) {
                if (section.get(i).matches("\\([0-9]{1,}\\)")) {

                    prefix = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                    result.append("<section prefix = \"" + prefix + "\">" + "\n");
                    String n = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                    Number = Integer.parseInt(n);
                    nextNumber = Number + 1;

                    sectionArray2.clear();
                    while (Number != nextNumber && i + 1 < section.size() && !section.get(i).contains("Ord.")) {

                        if (section.get(i + 1).matches("\\([0-9]{1,}\\)")) {
                            String n2 = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                            if (isNumeric(n2)) {
                                int n2Int = Integer.parseInt(n2);
                                if (n2Int == nextNumber) {
                                    Number = Integer.parseInt(n2);
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }
                            }
                        } else {
                            i++;
                            sectionArray2.add(section.get(i));

                        }

                    }

                    determineSubsections(sectionArray2);
                    result.append("</section>" + "\n");

                } else if (section.get(i).matches("[0-9]{1,}\\.?\\)?")) {

                    prefix = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                    result.append("<section prefix = \"" + prefix + "\">" + "\n");
                    String n = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                    Number = Integer.parseInt(n);
                    nextNumber = Number + 1;

                    sectionArray2.clear();

                    while (Number != nextNumber && i + 1 < section.size() && !section.get(i).contains("Ord.")) {

                        if (section.get(i + 1).matches("[0-9]{1,}\\.?\\)?")) {

                            String n2 = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                            if (isNumeric(n2)) {
                                int n2Int = Integer.parseInt(n2);
                                if (n2Int == nextNumber) {
                                    Number = Integer.parseInt(n2);
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }
                            }
                        } else {
                            i++;
                            sectionArray2.add(section.get(i));

                        }

                    }

                    determineSubsections(sectionArray2);
                    result.append("</section>" + "\n");
                }

            } else if (section.get(i).matches("\\([A-Za-z]{1,}\\)|^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$")) {
                if (section.get(i).matches("\\([A-Za-z]{1,}\\)")) {
                    prefix = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                    char c2 = section.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);
                    result.append("<section prefix = \"" + prefix + "\">" + "\n");

                    if (!isRomanNum(section)) {
                        orgChar = section.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);

                        c2++;

                        String nextorgChar = "";
                        for (int j = 0; j < prefix.length(); j++) {
                            nextorgChar += c2;
                        }

                        if (Character.isUpperCase(orgChar)) {
                            sectionArray2.clear();

                            while (orgChar != nextorgChar.charAt(0) && prefix.length() == nextorgChar.length() && i + 1 < section.size()) {

                                if (section.get(i + 1).matches("\\([A-Za-z]{1,}\\)")) {

                                    String nextLetter = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (nextLetter.equals(nextorgChar)) {
                                        orgChar = c2;
                                    } else {
                                        i++;
                                        sectionArray2.add(section.get(i));
                                        ;
                                    }
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }

                            }
                        } else {
                            sectionArray2.clear();
                            while (orgChar != nextorgChar.charAt(0) && prefix.length() == nextorgChar.length() && i + 1 < section.size()) {

                                if (section.get(i + 1).matches("\\([A-Za-z]{1,}\\)")) {

                                    String nextLetter = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (nextLetter.equals(nextorgChar)) {
                                        orgChar = c2;
                                    } else {
                                        i++;
                                        sectionArray2.add(section.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }

                            }

                        }
                    } else {
                        prefix = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                        orgChar = section.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);
                        String n = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                        if (Character.isUpperCase(orgChar)) {
                            if (isSingleRomanNumber(prefix)) {
                                Number = rm.valueOf(n);
                            }
                            nextNumber = Number + 1;

                            sectionArray2.clear();

                            while (Number != nextNumber && i + 1 < section.size() && !section.get(i).contains("Ord.")) {

                                if (section.get(i + 1).matches("\\([A-Z]{1,}\\)|^[A-Z][A-Z]?[A-Z]?\\.$")) {

                                    String n2 = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (isSingleRomanNumber(n2)) {
                                        int n2Int = rm.valueOf(n2);
                                        if (n2Int == nextNumber) {
                                            Number = rm.valueOf(n2);
                                        } else {
                                            i++;
                                            sectionArray2.add(section.get(i));

                                        }
                                    } else {
                                        i++;
                                        sectionArray2.add(section.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }

                            }
                        } else {
                            if (isSingleRomanNumber(prefix)) {
                                Number = rm.valueOf(n);
                            }
                            nextNumber = Number + 1;

                            sectionArray2.clear();

                            while (Number != nextNumber && i + 1 < section.size() && !section.get(i).contains("Ord.")) {

                                if (section.get(i + 1).matches("\\([a-z]{1,}\\)|^[a-z][a-z]?[a-z]?\\.$")) {

                                    String n2 = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (isSingleRomanNumber(n2)) {
                                        int n2Int = rm.valueOf(n2);
                                        if (n2Int == nextNumber) {
                                            Number = rm.valueOf(n2);
                                        } else {
                                            i++;
                                            sectionArray2.add(section.get(i));

                                        }
                                    } else {
                                        i++;
                                        sectionArray2.add(section.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }

                            }
                        }
                    }
                    determineSubsections(sectionArray2);

                    result.append("</section>" + "\n");

                } else if (section.get(i).matches("^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$")) {
                    prefix = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                    char c2 = section.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);
                    result.append("<section prefix = \"" + prefix + "\">" + "\n");

                    if (!isRomanNum(section)) {
                        orgChar = section.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);

                        c2++;

                        String nextorgChar = "";
                        for (int j = 0; j < prefix.length(); j++) {
                            nextorgChar += c2;
                        }

                        if (Character.isUpperCase(orgChar)) {
                            sectionArray2.clear();

                            while (orgChar != nextorgChar.charAt(0) && prefix.length() == nextorgChar.length() && i + 1 < section.size()) {

                                if (section.get(i + 1).matches("^[A-Za-z][A-Za-z]?[A-Za-z]?\\.\\)?$")) {

                                    String nextLetter = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (nextLetter.equals(nextorgChar)) {
                                        orgChar = c2;
                                    } else {
                                        i++;
                                        sectionArray2.add(section.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }

                            }
                        } else {
                            sectionArray2.clear();
                            while (orgChar != nextorgChar.charAt(0) && prefix.length() == nextorgChar.length() && i + 1 < section.size()) {

                                if (section.get(i + 1).matches("^[A-Za-z][A-Za-z]?[A-Za-z]?\\.\\)?$")) {

                                    String nextLetter = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (nextLetter.equals(nextorgChar)) {
                                        orgChar = c2;
                                    } else {
                                        i++;
                                        sectionArray2.add(section.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }

                            }

                        }
                    } else {
                        prefix = section.get(i).replace("(", "").replace(")", "").replace(".", "");

                        String n = section.get(i).replace("(", "").replace(")", "").replace(".", "");
                        orgChar = section.get(i).replace("(", "").replace(")", "").replace(".", "").charAt(0);
                        if (Character.isUpperCase(orgChar)) {
                            if (isSingleRomanNumber(prefix)) {
                                Number = rm.valueOf(n);
                            }
                            nextNumber = Number + 1;

                            sectionArray2.clear();

                            while (Number != nextNumber && i + 1 < section.size() && !section.get(i).contains("Ord.")) {

                                if (section.get(i + 1).matches("\\([A-Z]{1,}\\)|^[A-Z][A-Z]?[A-Z]?\\.$")) {

                                    String n2 = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (isSingleRomanNumber(n2)) {
                                        int n2Int = rm.valueOf(n2);
                                        if (n2Int == nextNumber) {
                                            Number = rm.valueOf(n2);
                                        } else {
                                            i++;
                                            sectionArray2.add(section.get(i));

                                        }
                                    } else {
                                        i++;
                                        sectionArray2.add(section.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }

                            }
                        } else {
                            if (isSingleRomanNumber(prefix)) {
                                Number = rm.valueOf(n);
                            }
                            nextNumber = Number + 1;

                            sectionArray2.clear();

                            while (Number != nextNumber && i + 1 < section.size() && !section.get(i).contains("Ord.")) {

                                if (section.get(i + 1).matches("\\([a-z]{1,}\\)|^[a-z][a-z]?[a-z]?\\.$")) {

                                    String n2 = section.get(i + 1).replace("(", "").replace(")", "").replace(".", "");
                                    if (isSingleRomanNumber(n2)) {
                                        int n2Int = rm.valueOf(n2);
                                        if (n2Int == nextNumber) {
                                            Number = rm.valueOf(n2);
                                        } else {
                                            i++;
                                            sectionArray2.add(section.get(i));

                                        }
                                    } else {
                                        i++;
                                        sectionArray2.add(section.get(i));

                                    }
                                } else {
                                    i++;
                                    sectionArray2.add(section.get(i));

                                }

                            }
                        }
                    }

                    determineSubsections(sectionArray2);

                    result.append("</section>" + "\n");

                }
            } else if (section.get(i).contains("<table")) {

                count++;
                result.append("<section prefix=\"" + count + "\"" + " type=\"table\">" + "\n");
                String tablecode = section.get(i).replace("&times", "&#215").replace("&frac14", "&#188").replace("&frac12", "&#189").replace("&frac34", "&#190");
               // int tablehex = Integer.decode(section.get(i));
                String table = "";
                for (int j=0; j< tablecode.length(); j++)
                {
                    char character = tablecode.charAt(j);
                    String s = String.format ("\\u%04x", (int)character);
                  //  System.out.println(character);
                   // System.out.println(s);    
                    if (s.equals("\\u2002"))
                    {
                        table += " ";
                    }
                    else
                    table += character;
                    
                }
                result.append(table + "\n");
                result.append("</section>" + "\n");
            } else if (section.get(i).contains("Cross reference")) {
                continue;
            } else {
                result.append(section.get(i) + "\n");
            }

        }
    }

    private boolean isRomanNum(ArrayList<String> Rsection) {
        ArrayList<Boolean> areRomans = new ArrayList<>();
        for (String s : Rsection) {

            if (s.matches("\\([A-Za-z]{1,}\\)|^[A-Za-z][A-Za-z]?[A-Za-z]?\\.$") || s.matches("\\([0-9]{1,}\\)|[0-9]{1,}\\.?\\)?")) {

                romanDigit = s.replace("(", "").replace(")", "").replace(".", "").toUpperCase();

                romanNumeralPattern = "^\\.{0,1}M{0,3}(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})"
                        + "(I[XV]|V?I{0,3}|V?I{0,2}J)\\.{0,1}$";
                if (romanDigit.matches(romanNumeralPattern)) {
                    areRomans.add(true);
                } else {
                    areRomans.add(false);
                }
            }

        }
        int boolCount = 0;
        for (Boolean b : areRomans) {
            if (b == true) {
                boolCount++;
            }

        }
        if (boolCount == areRomans.size()) {
            return true;
        } else if (areRomans.get(0) == true && areRomans.size() > 3 && boolCount >= 2) {

            return true;
        }

        return false;
    }

    public boolean isSingleRomanNumber(String s) {
        romanNumeralPattern = "^\\.{0,1}M{0,3}(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})"
                + "(I[XV]|V?I{0,3}|V?I{0,2}J)\\.{0,1}$";
        if (s.toUpperCase().matches(romanNumeralPattern)) {
            return true;
        }
        return false;

    }

    public boolean isNumeric(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            // s is not numeric
            return false;
        }
    }

    public StringBuilder getResults() {
        return result;
    }

    private void createErrorLog() {

        try {
            File f = new File(Path + "\\ErrorLog.txt");
            if (f.exists() && !f.isDirectory()) {
                FileWriter fw = new FileWriter(f, true);
                PrintWriter outputFile = new PrintWriter(fw);

                outputFile.println(sourcelink);

                outputFile.close();
            } else {
                f.createNewFile();
                FileWriter fw = new FileWriter(f);
                PrintWriter outputFile = new PrintWriter(fw);

                outputFile.println(sourcelink);

                outputFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
