/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 *
 * @author aboshady
 */
public class Message {

    private Message() {
    }

    public static void error(String header, String content, Stage owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content);
        alert.setHeaderText(header);
        alert.initOwner(owner);
        alert.showAndWait();
    }
    
    
     public static void info(String header, String content, Stage owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setHeaderText(header);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    
}
