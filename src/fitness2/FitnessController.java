package fitness2;

/*imports*/
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author aboshady
 */
public class FitnessController implements Initializable {

    /*non-FXML fields*/
    private Stage stage;
    private PlayerService playerService;
    private List<Player> players;
    private List<Refree> refrees;
    Refree mainRefree;
    private Manager manager;
    private int value;
    private boolean running;
    private final static String[] stages = new String[]{"Primary", "Preparatory", "Secondary"};
    private static final String[] stagesInArabic = new String[]{"الابتدائية", "الاعدادية", "الثانوية"};

    private final static String[] tests = new String[]{"HEIGHT", "FLEXIBLE", "JUMP", "PUSHUP", "SITUP", "RUN"};

    //a negative time means there's no time limit (ends successfully with stop button)
    private final int[] tests_seconds = new int[]{-1, -1, -1, -1, 15, 15};

    //current player shown in the screen
    private int currentPlayerIndex = -1;

    //current test index
    private int currentTest = 3;

    //nan player is used whenever there's no player at the pagination current index
    private final Player nanPlayer = new Player("NAN", "NAN", null);


    /*FXML feilds*/
    @FXML
    private Label heightLabel;

    @FXML
    private ImageView addButton;

    @FXML
    private ImageView searchButton;

    @FXML
    private ImageView stopButton;

    @FXML
    private Label flexibleLabel;

    @FXML
    private ImageView laserButton;

    @FXML
    private ImageView deleteButton;

    @FXML
    private ImageView startButton;

    @FXML
    private Pagination pager;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label countLabel;

    @FXML
    private Label markLabel;

    @FXML
    private Label previousLabel;

    @FXML
    private ImageView previousButton;

    @FXML
    private ImageView enterButton;

    @FXML
    private ImageView nextButton;

    @FXML
    private Label nextLabel;

    @FXML
    private Label currentLabel;

    @FXML
    private ImageView closeButton;

    @FXML
    private MenuItem saveIcon;

    private Timer timer;

    @FXML
    void saveIconClicked(ActionEvent event) {
        // List<String[]> records = new ArrayList<String[]>();
        File reportFile = new File("report.csv");
        try (FileOutputStream writer = new FileOutputStream(reportFile); CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(writer));) {

            //for UTF SUPPORT
            writer.write(0xef);
            writer.write(0xbb);
            writer.write(0xbf);
            // List<String[]> records = new ArrayList<String[]>();
            final String[] columns = new String[]{"الكود", "الاسم", "المرحلة", "الطول", "المرونة", "المرونة", "الوثب ", "الوثب", "الضغط", "الضغط", "البطن", "البطن", "الجرى فى المكان", "الجرى فى المكان", "مجموع الدرجات"};
            final String[] subColumns = new String[]{"", "", "", "", "المسافة", "الدرجة", "المسافة", "الدرجة", "العدات", "الدرجة", "العدات", "الدرجة", "العدات", "الدرجة", ""};
            csvWriter.writeNext(columns);
            csvWriter.writeNext(subColumns);
            String[][] data = new String[players.size()][];

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                double sumMark = 0;
                String code = player.getCode();
                String stage = player.getStage();
                String stageArabic = Utils.getStageInArabic(stage);
                
                String name = player.getName();

                int height = player.getHeight();
                if (height < 0) {
                    height = 0;
                }
                int jump = player.getJump();
                if (jump < 0) {
                    jump = 0;
                }
                int jumpMark = MarkCalculator.getMark(jump, stage, "JUMP");
                if (jumpMark < 0) {
                    jumpMark = 0;
                }

                double flexible = player.getFlexible();
                if (flexible < 0) {
                    flexible = 0;
                }
                int flexibleMark = MarkCalculator.getMark((int) flexible, stage, "FLEXIBLE");
                if (flexibleMark < 0) {
                    flexibleMark = 0;
                }
                int pushup = player.getPushup();
                if (pushup < 0) {
                    pushup = 0;
                }
                int pushupMark = MarkCalculator.getMark(pushup, stage, "PUSHUP");
                int run = player.getRun();
                if (run < 0) {
                    run = 0;
                }
                int runMark = MarkCalculator.getMark(run, stage, "RUN");
                int situp = player.getSitup();
                if (situp < 0) {
                    situp = 0;
                }
                int situpMark = MarkCalculator.getMark(situp, stage, "SITUP");

                sumMark = flexibleMark + jumpMark + pushupMark + runMark + situpMark;
                data[i] = new String[]{code, name, stageArabic, 
                    String.valueOf(height),
                    String.valueOf(flexible), String.valueOf(flexibleMark),
                    String.valueOf(jump), String.valueOf(jumpMark),
                    String.valueOf(pushup), String.valueOf(pushupMark),
                    String.valueOf(situp), String.valueOf(situpMark),
                    String.valueOf(run), String.valueOf(runMark),
                    String.valueOf(sumMark)};

                // csvWriter.writeNext(row);
            }
            //sort based on last row which is the total mark
            Arrays.sort(data, Comparator.comparingDouble(o -> Double.parseDouble(o[14])));

            //write rows
            for (int i = 0; i < data.length; i++) {
                csvWriter.writeNext(data[i]);
            }

        } catch (IOException ex) {

            Message.error("خطأ", "حث خطأ خلال عملية بناء الملف", this.stage);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save report");
        fileChooser.setInitialFileName("تقرير اللعيبة");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        File filepath = fileChooser.showSaveDialog(stage);
        if (filepath != null) {
            try {
                Files.copy(reportFile.toPath(),
                        filepath.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Message.error("خطأ", "لم يتم حفظ التقرير تأكد من اغلاق التقرير السابق", this.stage);
                return;
            }
            Message.info("تم بنجاح", "تم حفظ التقرير", this.stage);
        }

    }

    @FXML
    void closeButtonClicked(MouseEvent event) {
        //end all
        mainRefree.addToReadingQueue("E");
        System.exit(0);

    }

    @FXML
    void addButtonClicked(MouseEvent event) {
        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setTitle("اضافة لاعب");
        codeDialog.setHeaderText("ادخل كود جديد");
        codeDialog.setContentText("الكود ");
        codeDialog.initOwner(stage);
        codeDialog.showAndWait().ifPresent(
                (code) -> {
                    if (playerService.get(code) == null) {

                        List<String> choices = new ArrayList<>(Arrays.asList(stagesInArabic));
                        ChoiceDialog<String> stageDialog = new ChoiceDialog<>("الابتدائية", choices);
                        stageDialog.setTitle("اضافة لاعب");
                        stageDialog.setHeaderText("حدد مرحلة  " + code);
                        stageDialog.setContentText("المرحلة");
                        stageDialog.initOwner(stage);
                        stageDialog.showAndWait().ifPresent(
                                (stage) -> {

                                    TextInputDialog nameDialog = new TextInputDialog();
                                    nameDialog.setTitle("اضافة لاعب");
                                    nameDialog.setHeaderText("ادخل الاسم ");
                                    nameDialog.setContentText("الاسم");
                                    nameDialog.initOwner(this.stage);
                                    nameDialog.showAndWait().ifPresent(
                                            (name) -> {

                                                Player p = new Player(code, Utils.getStageInEnglish(stage), name);
                                                players.add(p);
                                                playerService.save(p);
                                                mainRefree.addToReadingQueue(String.format("add %s", p.toString()));
                                                pageChangedHandler(currentPlayerIndex, Math.abs(currentPlayerIndex));

                                            }
                                    );

                                }
                        );
                    } else {

                        Message.error("Error", "هذا اللاعب موجود", this.stage);

                    }

                }
        );

    }

    @FXML
    void deleteButtonClicked(MouseEvent event) {
        if (currentPlayerIndex < 0 || players.isEmpty()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("رسالة تأكيد");
        alert.setHeaderText("هل حقا تريد حذف اللاعب ؟");
        alert.setContentText("اختر Ok للتأكيد");
        alert.initOwner(stage);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.CANCEL) {
            return;
        }

        Player p = players.get(currentPlayerIndex);
        if (playerService.delete(p)) {
            players.remove(currentPlayerIndex);
            pageChangedHandler(currentPlayerIndex, Math.abs(currentPlayerIndex));
        }
    }

    @FXML
    void laserButtonClicked(MouseEvent event) {
        mainRefree.addToReadingQueue("L");
    }

    @FXML
    void enterButtonClicked(MouseEvent event) {
        TextInputDialog codeDialog = new TextInputDialog(String.valueOf(tests_seconds[currentTest]));
        codeDialog.setTitle("تحديد وقت التمرين");
        codeDialog.setHeaderText("Setting " + tests[currentTest] + " time in seconds\nرقم سالب يعنى غير مقيد بزمن");
        codeDialog.setContentText("الزمن بالثوانى ");
        codeDialog.initOwner(stage);
        codeDialog.showAndWait().ifPresent(
                (t) -> {
                    if (t.matches("\\d*")) {
                        int time = Integer.parseInt(t);
                        tests_seconds[currentTest] = time;
                        showPlayer();
                    }
                }
        );
    }

    @FXML
    void nextButtonClicked(MouseEvent event) {
        int prevTest = currentTest;
        currentTest = (currentTest + 1) % tests.length;
        int nextTest = (currentTest + 1) % tests.length;
        previousLabel.setText(tests[prevTest]);
        currentLabel.setText(tests[currentTest]);
        nextLabel.setText(tests[nextTest]);
        showPlayer();
    }

    @FXML
    void previousButtonClicked(MouseEvent event) {
        int nextTest = currentTest;
        currentTest = (currentTest - 1 + tests.length) % tests.length;
        int prevTest = (currentTest - 1 + tests.length) % tests.length;
        previousLabel.setText(tests[prevTest]);
        currentLabel.setText(tests[currentTest]);
        nextLabel.setText(tests[nextTest]);

        showPlayer();
    }

    @FXML
    void searchButtonClicked(MouseEvent event) {

        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setTitle("بحث بالكود");
        codeDialog.setHeaderText("ادخل كود اللاعب بدقة");
        codeDialog.setContentText("الكود");
        codeDialog.initOwner(stage);
        codeDialog.showAndWait().ifPresent(
                (code) -> {
                    Player p = playerService.get(code);
                    if (p != null) {
                        int pIndex = players.indexOf(p);
                        if (pIndex < 0) {
                            players.add(p);
                            mainRefree.addToReadingQueue(String.format("add %s", p.toString()));
                            pIndex = players.size() - 1;
                        }
                        pager.setCurrentPageIndex(pIndex);
                        pageChangedHandler(currentPlayerIndex, pIndex);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "اللاعب غير موجود");
                        alert.showAndWait();
                    }
                }
        );
    }

    @FXML
    void startButtonClicked(MouseEvent event) {

        Character duty = tests[currentTest].charAt(0);;
        if (currentPlayerIndex < 0 || players.isEmpty() || !mainRefree.isResponsible(duty)) {
            Message.error("error", " كود اللاعب غير صحيح او ان هناك حكم اخر مسئول عن هذاالتمرين", stage);
            System.out.println("negative index or not your duty, " + duty);
            System.out.println(mainRefree.getDuties());
            return;
        }
        //disable start button
        startButton.setDisable(true);
        //disable all othr buttons except stop
        disableButtons();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        //current player perfomring the current test
        Player p = players.get(currentPlayerIndex);
        //start timing in case of tests with time limit 
        int testTime = tests_seconds[currentTest];
        if (testTime > 0) {
            timer = new Timer();
            TimeEventHandler timeHandler = new TimeEventHandler(testTime);
            timeHandler.setTickCallBack(
                    (time) -> {
                        Platform.runLater(() -> {
                            timeLabel.setText(Formatter.timeFormat(time));
                            if (time <= 5) {
                                timeLabel.setTextFill(Color.RED);
                            }
                            if (time <= 0) {

                                timer.cancel();
                                timer.purge();
                                timeLabel.setTextFill(Color.web("0xfafafa"));
                                //end the test with lower case letter
                                mainRefree.addToReadingQueue(tests[currentTest].substring(0, 1).toLowerCase());
                                if (currentTest == 0) {
                                    p.setHeight(value);
                                } else if (currentTest == 1) {
                                    p.setFlexible((double) value / 2);
                                } else {
                                    p.setTest(tests[currentTest], value);
                                }
                                playerService.update(p);
                                showPlayer();
                                enableButtons();
                                startButton.setDisable(false);
                                stopButton.setDisable(true);
                                running = false;
                            }
                        });
                    });

            timer.schedule(timeHandler, 4000l, 1000l);
            //intialize labels
            countLabel.setText(Formatter.IntegerFormat(0, 2));
            markLabel.setText(Formatter.IntegerFormat(0, 2));
            timeLabel.setText(Formatter.timeFormat(tests_seconds[currentTest]));
        }

        //start the test
        value = -1;
        mainRefree.addToReadingQueue(tests[currentTest].substring(0, 1));

        //enable stop button
        stopButton.setDisable(false);
        if (currentTest != 0 && currentTest != 1) {
            running = true;
        }

    }

    @FXML
    void stopButtonClicked(MouseEvent event) {
        if (currentPlayerIndex < 0 || players.isEmpty()) {
            return;
        }
        //disable stop button
        stopButton.setDisable(true);
        //stop the timer
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        //end the test with lower case letter
        mainRefree.addToReadingQueue(tests[currentTest].substring(0, 1).toLowerCase());
        Player p = players.get(currentPlayerIndex);
        if (value != -1) {
            if (tests_seconds[currentTest] > 0) {
                //do nothing
            } else if (currentTest == 0) {
                p.setHeight(value);
            } else if (currentTest == 1) {
                p.setFlexible((double) value / 2);
            } else {
                p.setTest(tests[currentTest], value);
            }
        }

        playerService.update(p);
        showPlayer();
        //enable all other buttons
        startButton.setDisable(false);
        enableButtons();
        running = false;
        showPlayer();
    }

    @FXML
    public void toggleFullScreen(MouseEvent event) {
        if (this.stage != null) {
            if (this.stage.isFullScreen()) {
                this.stage.setFullScreen(false);
            } else {
                this.stage.setFullScreen(true);
            }
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //initialize database
        playerService = new PlayerService();
        playerService.initDB();

        //get all players
        players = Collections.synchronizedList(playerService.getActive());

        refrees = Collections.synchronizedList(new ArrayList<>());
        //create the main refree and add it the refrees arrayList
        mainRefree = new Refree();
        mainRefree.addAllDuties(new HashSet<>(Arrays.asList('H', 'F', 'J', 'P', 'S', 'R')));
        System.out.println("duties of the main refree are: " + mainRefree.getDuties());

        //set the data listener
        mainRefree.setDataAvailableCallback((data) -> {
            if (currentPlayerIndex < 0) {
                return;
            }
            Player p = players.get(currentPlayerIndex);
            int value = Integer.parseInt(data.split(" ")[1]);
            this.value = value;
            Platform.runLater(() -> {
                if (currentTest == 0) {
                    heightLabel.setText(Formatter.IntegerFormat(value, 3));
                } else if (currentTest == 1) {
                    flexibleLabel.setText(Formatter.doubleFormat((double) value / 2, 4));
                } else {
                    countLabel.setText(Formatter.IntegerFormat(value, 2));
                    markLabel.setText(Formatter.IntegerFormat(MarkCalculator.getMark(value, p.getStage(), tests[currentTest]), 2));
                }

            });

        });
        refrees.add(mainRefree);

        //start accepting thread
        new Thread() {
            private final int PORT = 4321;

            @Override
            public void run() {
                //create the server socket
                try (ServerSocket server = new ServerSocket(PORT)) {
                    while (true) {
                        //MAXIMUM of 3 refrees at a time

                        //accept new connections if refrees is less than 3
                        Socket connection = server.accept();

                        //create a refree holding that connection
                        Refree refree = new Refree(connection);

                        //add the refree to the refrees arraylist
                        refrees.add(refree);

                        //write all players data to the new refree
                        for (Player player : players) {
                            refree.addToWritingQueue(player.toString());
                        }

                    }
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }
        }.start();

        manager = new Manager(players, playerService, refrees);
        manager.setPlayersChangedCallback(() -> {
            Platform.runLater(() -> pageChangedHandler(currentPlayerIndex, currentPlayerIndex));

        });
        manager.start();

        //intialize arduino
        //arduino = new Arduino();
        //register page change event
        pager.currentPageIndexProperty().addListener(
                (obs, oldIndex, newIndex) -> {
                    pageChangedHandler((int) oldIndex, (int) newIndex);
                });
        pager.setCurrentPageIndex(0);
        pageChangedHandler(-1, 0);
        stopButton.setDisable(true);

    }

    private void pageChangedHandler(int oldIndex, int newIndex) {

        if (newIndex < players.size() && newIndex >= 0) {
            currentPlayerIndex = newIndex;

        } else {
            currentPlayerIndex = -newIndex;
        }
        showPlayer();

    }

    private void showPlayer() {
        Player player;
        if (players.isEmpty() || currentPlayerIndex < 0) {
            player = nanPlayer;
        } else {
            player = players.get(currentPlayerIndex);
        }
        int testCount = player.getTest(tests[currentTest]);
        statusLabel.setText(String.format("%s >>> %s", player.getCode(), Utils.getStageInArabic(player.getStage())));

        heightLabel.setText(Formatter.IntegerFormat(player.getHeight(), 3));
        flexibleLabel.setText(Formatter.doubleFormat(player.getFlexible(), 4));

        if (!running) {
            countLabel.setText(Formatter.IntegerFormat(testCount, 2));
            markLabel.setText(Formatter.IntegerFormat(MarkCalculator.getMark(testCount, player.getStage(), tests[currentTest]), 2));
            timeLabel.setText(Formatter.timeFormat(tests_seconds[currentTest]));
        }
    }

    public void disableButtons() {
        ImageView buttons[] = new ImageView[]{nextButton, previousButton, enterButton, searchButton,
            laserButton, deleteButton, addButton};
        Arrays.stream(buttons).forEach((b) -> b.setDisable(true));
        pager.setDisable(true);

    }

    public void enableButtons() {
        ImageView buttons[] = new ImageView[]{nextButton, previousButton, enterButton, searchButton,
            laserButton, deleteButton, addButton};
        Arrays.stream(buttons).forEach((b) -> b.setDisable(false));
        pager.setDisable(false);

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
