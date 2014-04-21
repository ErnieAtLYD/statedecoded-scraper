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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**this class uses regular expression and JSoup to find specific tag and attributes
  from the source code. It Creates an XML file form source code.  */

public class Hierarchy {

    private URL url;
    private String sourceLink;
    private BufferedReader buf;
    private StringBuilder sb;
    private String src;
    private Pattern checkRegex;
    private String regex;
    private Matcher rmatcher;
    private String level;
    private String baseURL;
    private String title;
    private String sectionNum;
    private String parentTitle;
    private ArrayList<String> listOfSections;
    private String pageTitle;
    private ArrayList<String> listofTitles;
    private SectionRetrieval sr;
    private StackTraceElement[] stestrace;
    private String Path;
    private String levelLink;
    private ArrayList<String> levels;
    private String chapterLevel;
    private String pageLevel;

    /** Constructor. It calls on methods that retrieves information 
     * form the string builder containing the source code and 
     * finally calls the method that create the XML File.*/
    public Hierarchy(String Source, String Path) {
        levels = new ArrayList<>();
        listofTitles = new ArrayList<>();
        listOfSections = new ArrayList<>();
        this.sourceLink = Source;
        this.Path = Path;
        baseURL = "http://library.municode.com/HTML/10620/";
        level = "";
        getSrc();
        getLevel();
        trimLinksInfo();
        getParentTitleLine();
        getPageTitle();
        getSectionInfo();
        createXML();
    }
     /** Retrieves the source code through URL connection and appends to a String builder */
    public void getSrc() {
        try {

            URL site = new URL(sourceLink);
            URLConnection cod = site.openConnection();
            buf = new BufferedReader(new InputStreamReader(cod.getInputStream(), "UTF-8"));
            sb = new StringBuilder();
            while (true) {
                int data = buf.read();
                if (data == -1) {
                    break;
                } else {
                    sb.append((char) data);
                }
            }
            src = sb.toString();
        } catch (Exception e) {
            stestrace = e.getStackTrace();
            createErrorLog();
        }
    }
    /** Uses regular expression to find the line that contains links to other web pages. 
     * For each line that matches the regular expression it calls the getTitle, and getSectionNum. 
     it add section number to listofSection array. */
    public void trimLinksInfo() {
        regex = "(?!.*(gt;))<a(\\\"?|\\#?)(?!.*(javascript|target|html#|book|crumb|ref\\.)).+(\\\"?)>\\\"?[A-Za-z].?([A-Za-z0-9].?(.*?)([^<]*))</a>";
        checkRegex = Pattern.compile(regex);
        rmatcher = checkRegex.matcher(sb.toString());
        while (rmatcher.find()) {
            title = getTitle(rmatcher.group().trim());
            sectionNum = getSectionNum(title);
            listOfSections.add(sectionNum);
        }
    }
    /**Retrieves  and redefines the string containing the link and combines the 
     * base URL and the link to produce a absolute path. it also returns internal relative links*/
    public String getLinks(String s) {
        String regex2 = "(?!.*(f=)|([A-Za-z])|(/))([/level?]|[\\#?])[A-Za-z].+?[\\\"?]";
        Pattern checkRegex2 = Pattern.compile(regex2);
        Matcher rmatcher2 = checkRegex2.matcher(s);
        while (rmatcher2.find()) {
            if (rmatcher2.group().trim().substring(0, rmatcher2.group().length() - 1).charAt(0) == '#') {
                return url + rmatcher2.group().trim().substring(0, rmatcher2.group().length() - 1);
            } else {
                return baseURL + rmatcher2.group().trim().substring(1, rmatcher2.group().length() - 1);
            }
        }
        return "";
    }
    /** Retrieves the text between tags from the string that contains the tittle link*/
    public String getTitle(String s) {

        String html = s;
        Document doc = Jsoup.parse(html);
        for (Element title : doc.select("a")) {

            listofTitles.add(title.text());
            return title.text();
        }
        return "";

    }
    /** Retrieves the section from the tittle text*/
    public String getSectionNum(String s) {
        String regex = "[Ss][Ee][Cc][Ss]?[Tt]?[Ii]?[Oo]?[Nn]?[\\.]\\s[A-Za-z0-9].+?\\s";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return "";
    }
    /** Retrieves the line that contains the parent tittle */
    public void getParentTitleLine() {
        String src = sb.toString();
        String regex = "([Mm][Ee][Tt][Aa]\\s.+?\\=\\\")([Pp][Aa][Rr].+)(\\\")";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        String line = "";
        if (matcher.find()) {
            line = matcher.group().trim();
            getParentTitle(line);
        }
    }

    /** Retrieves the parent tittle from the line that contains the parent title */
    public void getParentTitle(String s) {
        String regex = "[Tt][Ee][Nn][Tt]\\=\\\"?.+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            parentTitle = matcher.group().trim().substring(6, matcher.group().length() - 1);
        }
    }

    /**gets the Page Title */
    public void getPageTitle() {
        String html = sb.toString();
        Document doc = Jsoup.parse(html);
        for (Element title : doc.select("title")) {
            if (title.text().trim().replace("\n", "").replace(",", "").replace(" ", "_").length() < 40) {
                pageTitle = title.text().trim().replace("\n", "").replace(",", "").replace(" ", "_");
            } else {
                pageTitle = title.text().trim().replace("\n", "").replace(",", "").replace(" ", "_").substring(0, 40);
            }
        }
    }
    
    /**  Retrieves the level from the page link*/
    public void getLevel() {
        String src = sb.toString();
        String regex2 = "<a\\sclass=\\\"crumb\\\" .+?</";
        Pattern pattern = Pattern.compile(regex2);
        Matcher matcher = pattern.matcher(src);
        level = "";

        while (matcher.find()) {
            levelLink = matcher.group().trim();
            String regex3 = "level[0-9]{1}";
            Pattern pattern2 = Pattern.compile(regex3);
            Matcher matcher2 = pattern2.matcher(levelLink);

            if (matcher2.find()) {
                level = matcher2.group().trim();
                levels.add(level);
            }
        }

        if (levels.size() >= 2) {
            chapterLevel = levels.get(levels.size() - 2).replace("level", "");
            pageLevel = levels.get(levels.size() - 1).replace("level", "");
        } else {
            chapterLevel = "";
            pageLevel = "";
        }
    }
    /** Retrieves the text for each section number */
    public void getSectionInfo() {
        sr = new SectionRetrieval(listOfSections, src, listofTitles, sourceLink, Path);
    }
    
    /** Create the XML File */
    private void createXML() {
        try {
            String fileName = pageTitle.replace(".", "_").replace("/", "_").replace("*", "_");

            File file = new File(Path + "\\" + fileName + ".xml");
            FileWriter fwriter = new FileWriter(file);
            PrintWriter outputFile = new PrintWriter(fwriter);
            outputFile.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            outputFile.println("<law>");
            outputFile.println("\t<structure>");
            outputFile.println("\t\t<unit label=\"chapter\" level=\"" + chapterLevel + "\">" + parentTitle + "</unit>");
            outputFile.println("\t\t<unit label=\"title\" level=\"" + pageLevel + "\">" + pageTitle + "</unit>");
            outputFile.println("\t</structure>");

            outputFile.write(sr.getResults().toString());
            outputFile.close();
        } catch (Exception e) {
            createErrorLog();
        }

    }
    
    /** Create a text file that contains link that failed */
    private void createErrorLog() {

        try {
            File f = new File(Path + "\\ErrorLog.txt");
            if (f.exists() && !f.isDirectory()) {
                FileWriter fw = new FileWriter(f, true);
                PrintWriter outputFile = new PrintWriter(fw);

                outputFile.println(sourceLink);
                outputFile.close();
            } else {
                f.createNewFile();
                FileWriter fw = new FileWriter(f);
                PrintWriter outputFile = new PrintWriter(fw);

                outputFile.println(sourceLink);
                outputFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
