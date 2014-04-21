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

import java.util.Comparator;

/**
this is used sort the tree set.
 */
class LinkComaprator<T> implements Comparator<T>{

    

    @Override
    public int compare(T L1, T L2) {
         String Link1 = L1.toString();
         String Link2 = L2.toString();
         
         return Link1.compareToIgnoreCase(Link2);//To change body of generated methods, choose Tools | Templates.
    }
    
}
