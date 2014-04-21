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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.SortedSet;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**  The LinksCod class is the main for the application. It displays the users interface
and start the the process depending on user selection */

public class LinksCOD {
    public static void main(String[] args) {
        /** buttons array contains all selection for the users interface.*/
        String[] buttons = {"Get all XML", "Single link XML", "Print all links"};
        int rc = JOptionPane.showOptionDialog(null, "What would you like to do?", "Confirmation",
                JOptionPane.WARNING_MESSAGE, 0, null, buttons, buttons[2]);
        
        /** rc==0 the first choice of user selection and display status information */
        if (rc == 0) {
            JOptionPane.showMessageDialog(null, "Select a directory");
            JFileChooser j = new JFileChooser();
            j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            j.showOpenDialog(j);
            String Path = j.getSelectedFile().getAbsolutePath();
            JFrame main = new JFrame();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            main.setSize(350, 150);
            int w = main.getSize().width;
            int h = main.getSize().height;
            int x = (dim.width - w) / 2;
            int y = (dim.height - h) / 2;
            main.setLocation(x, y);
            main.setLayout(new GridLayout(4, 1));
            JLabel links = new JLabel("Currently gathering links. Please wait ...");
            JLabel xmls = new JLabel("Links Gathered! Now Generating Xml Files...");
            JLabel process = new JLabel("Status:");
            JLabel complete = new JLabel("Completed!");
            main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            main.add(process);
            main.setVisible(true);
            main.add(links);
            FindLinks fl = new FindLinks();
            fl.findAllLinks(0, "http://library.municode.com/HTML/10620/level1/PTIIICOOR.html");
            main.add(xmls);
            main.revalidate();
            SortedSet<String> list = fl.getTree();
            for (String link : list) {
                Hierarchy hi = new Hierarchy(link, Path);
            }
            main.add(complete);
            main.revalidate();
        }
        /** rc=1 the second choice is of the user interface is selected 
         * and goes through the procedure of for single xml and link */
        else if (rc == 1) {
            String link = JOptionPane.showInputDialog("Paste a link");
            JFileChooser j = new JFileChooser();
            j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            j.showOpenDialog(j);
            String path = j.getSelectedFile().getAbsolutePath();
            Hierarchy h = new Hierarchy(link, path);
            JOptionPane.showMessageDialog(null, "Complete!"); 
        }
        /** rc=2 the third choice of the user interface 
         * and it goes through the procedure only to print links to file*/
        else if (rc == 2) {
            try {
                JFileChooser j = new JFileChooser();
                j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                j.showOpenDialog(j);
                String path = j.getSelectedFile().getAbsolutePath();
                File file = new File(path + "\\" + "All Links" + ".txt");
                JFrame main = new JFrame();
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                main.setSize(350, 150);

                int w = main.getSize().width;
                int h = main.getSize().height;
                int x = (dim.width - w) / 2;
                int y = (dim.height - h) / 2;
                main.setLocation(x, y);
                main.setLayout(new GridLayout(4, 1));

                // create the labels
                JLabel links = new JLabel("Currently gathering links. Please wait ...");
                JLabel xmls = new JLabel("All Links Gathered Completed!");
                JLabel process = new JLabel("Status:");
                main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                main.add(process);
                main.setVisible(true);
                main.add(links);

                FindLinks fl = new FindLinks();
                fl.findAllLinks(0, "http://library.municode.com/HTML/10620/level1/PTIIICOOR.html");
                main.add(xmls);
                main.revalidate();

                SortedSet<String> list = fl.getTree();

                FileWriter fwriter = new FileWriter(file);
                PrintWriter outputFile = new PrintWriter(fwriter);

                for (String s : list) {
                    outputFile.println(s);
                }
                fwriter.close();
            } catch (Exception e) {
            }
        }

    }

}
