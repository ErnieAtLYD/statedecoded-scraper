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

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JFileChooser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 This class recursively finds all the links in sub link to Web Page. 
 */
public class FindLinks {

    int level;
    SortedSet<String> linkset = new TreeSet<String>(new LinkComaprator<String>());

    public void findAllLinks(int lvl, String l) {
        level = lvl; //controls the number of times the this this recusion can occur.

        try {
            /** use jsoup to find html tag and attributes that reference  a link*/
            String url = l;
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                if (!link.attr("abs:href").contains("#") && !link.attr("abs:href").isEmpty()
                        && !link.attr("abs:href").contains(".png")
                        && !link.attr("abs:href").contains("book.html")
                        && !link.attr("abs:href").contains("toc")) {
                    if (!linkset.contains(link.attr("abs:href"))) {
                        linkset.add(link.attr("abs:href"));
                        
                        findAllLinks(level++, link.attr("abs:href"));
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SortedSet<String> getTree() {
        return linkset;
    }
}
