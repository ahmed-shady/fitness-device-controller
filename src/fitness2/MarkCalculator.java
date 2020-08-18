/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author aboshady
 */
public class MarkCalculator {

    private static final ArrayList<HashMap<String, Integer[]>> constants;

    static {
        constants = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            constants.add(new HashMap());
        }

        //Primary constants
        constants.get(0).put("RUN", new Integer[]{12, 31});
        constants.get(0).put("PUSHUP", new Integer[]{11, 30});
        constants.get(0).put("SITUP", new Integer[]{13, 32});
        //constants.get(0).put("JUMP", new Integer[]{13, 32});
        
        
        //Preparatory constants
        constants.get(1).put("RUN", new Integer[]{24, 33});
        constants.get(1).put("PUSHUP", new Integer[]{21, 40});
        constants.get(1).put("SITUP", new Integer[]{23, 32});
        constants.get(1).put("JUMP", new Integer[]{131, 230});

        //Secondary Constants
        constants.get(2).put("RUN", new Integer[]{26, 35});
        constants.get(2).put("PUSHUP", new Integer[]{34, 53});
        constants.get(2).put("SITUP", new Integer[]{25, 34});
        //constants.get(2).put("JUMP", new Integer[]{13, 32});
        constants.get(2).put("FLEXIBLE", new Integer[]{1, 20    });
    }

    public static int getMark(int count, String stage, String test) {
        switch (stage) {
            case "Primary":
                return getPrimaryMark(count, test);
            case "Preparatory":
                return getPreparatoryMark(count, test);
            case "Secondary":
                return getSecondaryMark(count, test);
            default:
                return -1;
        }
       
        
    }

    private static int getPrimaryMark(int count, String test) {
        Integer[] limits = constants.get(0).get(test);
        if (limits == null) {
            return -1;
        }
        int min = limits[0];
        int max = limits[1];
        if (count > max) {
            return 10;
        } else if (count < min) {
            return 0;
        } else {
            return (count - min) / 2 + 1;
        }
    }

    private static int getPreparatoryMark(int count, String test) {

        Integer[] limits = constants.get(1).get(test);
        if (limits == null) {
            return -1;
        }
        int min = limits[0];
        int max = limits[1];
        if (count > max) {
            return 10;
        } else if (count < min) {
            return 0;
        } else {
            int dividend;
            if(test.equals("PUSHUP"))
                    dividend = 2;
            else if(test.equals("JUMP"))
                    dividend = 10;
            else
                    dividend = 1;
            return (count - min) / dividend + 1;
        }
    }

    private static int getSecondaryMark(int count, String test) {

        Integer[] limits = constants.get(2).get(test);
        if (limits == null) {
            return -1;
        }
        int min = limits[0];
        int max = limits[1];
        if (count > max) {
            return 10;
        } else if (count < min) {
            return 0;
        } else {
                        int dividend;
            if(test.equals("PUSHUP") || test.equals("FLEXIBLE"))
                    dividend = 2;
            else
                    dividend = 1;
            return (count - min) / dividend + 1;
        }
    }
}
