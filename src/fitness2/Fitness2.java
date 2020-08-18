/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author aboshady
 */
public class Fitness2 extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        List<String> choices = new ArrayList<>(Arrays.asList(new String[]{"الرئيسية", "الطول والمرونة والوثب العريض", "الطول والمرونة", "الوثب العريض"}));
        ChoiceDialog<String> stageDialog = new ChoiceDialog<>("الرئيسية", choices);
        stageDialog.setTitle("مهمام الحكم");
        stageDialog.setHeaderText("اختر مهام الحكم المطلوبة من القائمة");
        stageDialog.setContentText("المهام");
        stageDialog.showAndWait().ifPresent(
                (choice) -> {
                    try {
                        Scene scene = null;

                        switch (choices.indexOf(choice)) {
                            case 0:
                                {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("design/fitness_all.fxml"));
                                    scene = new Scene((Pane) loader.load());
                                    FitnessController controller = loader.<FitnessController>getController();
                                    controller.setStage(stage);
                                    break;
                                }
                            case 1:
                                {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("design_hfj/fitness_hfj.fxml"));
                                    scene = new Scene((Pane) loader.load());
                                    FitnessHFController controller = loader.<FitnessHFController>getController();
                                    controller.setStage(stage);
                                    controller.setDuties(new HashSet<>(Arrays.asList('H', 'F', 'J')));
                                    break;
                                }
                            case 2:
                                {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("design_hfj/fitness_hfj.fxml"));
                                    scene = new Scene((Pane) loader.load());
                                    FitnessHFController controller = loader.<FitnessHFController>getController();
                                    controller.setStage(stage);
                                    controller.setDuties(new HashSet<>(Arrays.asList('H', 'F')));
                                    break;
                                }
                            case 3:
                                {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("design_hfj/fitness_hfj.fxml"));
                                    scene = new Scene((Pane) loader.load());
                                    FitnessHFController controller = loader.<FitnessHFController>getController();
                                    controller.setStage(stage);
                                    controller.setDuties(new HashSet<>(Arrays.asList('J')));
                                    break;
                                }
                            default:
                                System.exit(2);
                        }

                        stage.setScene(scene);


                        stage.setOnCloseRequest((t) -> {
                            System.exit(0);
                        });

                        stage.show();
                    } catch (IOException ex) {
                        System.exit(3);
                    }

                }
        );

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
