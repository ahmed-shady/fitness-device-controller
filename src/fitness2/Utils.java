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
public class Utils {
    
    private static final String[] stagesInArabic = new String[]{"الابتدائية", "الاعدادية", "الثانوية"};
    private static final String[] stagesInEnglish = new String[]{"Primary", "Preparatory", "Secondary"};
    public static String getStageInArabic(String stage) {
        int index = -1;
        for(int i = 0 ;i < 3;i++)
            if(stagesInEnglish[i].equals(stage))
                index = i;
        
        if(index >= 0)
            return stagesInArabic[index];
        else
            return stage;

    }

    public static String getStageInEnglish(String stage) {
        int index = -1;
        for(int i = 0 ;i < 3;i++)
            if(stagesInArabic[i].equals(stage))
                index = i;
        
        if(index >= 0)
            return stagesInEnglish[index];
        else
            return stage;

    }

}
