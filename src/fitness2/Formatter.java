/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

/**
 *
 * @author aboshady
 */
public class Formatter {
    private Formatter(){
    
    }
    public static String timeFormat(int time) {
        /*
         fromat is MM:SS
         */
        if(time < 0)
            return timeFormat(0);
        String mm = String.format("%2d", time / 60).replace(' ', '0');
        String ss = String.format("%2d", time % 60).replace(' ', '0');
        return (mm + ":" + ss);
    }

    public static String IntegerFormat(int num, int digits) {
        /*
         fromat is DDDD... 
         */
        if(num < 0)
              return String.format("%"+digits+"s", "");
        
        String formatter = "%" + digits + "d";
        return String.format(formatter, num).replace(' ', '0');
    }

    public static String doubleFormat(double num, int digits) {
        if(num < 0)
             return String.format("%"+digits+"s", "");
        
        int iPart = (int) num;
        double fPart = num - iPart;
        String formatter = "%" + digits + ".1f";
        return String.format(formatter, num).replace(' ', '0');
    }
}
