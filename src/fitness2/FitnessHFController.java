package fitness2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class FitnessHFController implements Initializable {

    //the stage that controller belongs to
    private Stage stage;

    //list of all players
    private List<Player> players;

    //nan player used when invalid player index is choosed
    private final Player nanPlayer = new Player("NAN", "NAN", null);
    

    private int currentPlayerIndex = -1;

    //the current value of the currrent test
    int value;

    //current test
    char test;

    //list of valid buttons (buttons that correspond to this controller duties)
    List<ToggleButton> testButtons = new ArrayList<>();

    //the refree object that interact with this controller
    HFRefree hfRefree;

    @FXML
    private Pagination pager;

    @FXML
    private ImageView closeButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label heightLabel;

    @FXML
    private Label flexibleLabel;

    @FXML
    private Label jumpLabel;

    @FXML
    private ImageView searchButton;

    @FXML
    private ToggleButton heightTButton;

    @FXML
    private ToggleGroup testSelect;

    @FXML
    private ToggleButton flexTButton;

    @FXML
    private ToggleButton jumpTButton;

    @FXML
    private ImageView stopButton;

    @FXML
    private ImageView startButton;

    @FXML

    void StopButtonClicked(MouseEvent event) {

        //set statment that will be sent to the main refree to update the database
        String setStmt = null;

        System.out.println("stop button clicked");
        if (currentPlayerIndex < 0 || players.isEmpty()) {
             Message.error("error", " كود اللاعب غير صحيح ", stage);
            return;
        }

        //end the test with lower case letter
        hfRefree.addToWritingQueue(String.valueOf(test).toLowerCase());

        //get the current player
        Player p = players.get(currentPlayerIndex);

        //set the appropriate test
        //also set the setStmt
        if (value != -1) {
            if (test == 'H') {
                p.setHeight(value);
                setStmt = String.format("set %s HEIGHT %d", p.getCode(), p.getHeight());
            } else if (test == 'F') {
                p.setFlexible((double) value / 2);
                setStmt = String.format("set %s FLEXIBLE %d", p.getCode(), (int) (p.getFlexible() * 2));
            } else if (test == 'J') {
                p.setJump(value);
                setStmt = String.format("set %s JUMP %d", p.getCode(), p.getJump());
            }
        } else {
            System.out.println("value is -1");
        }

        //write the setStmt to the main Refree
        if (setStmt != null) {
            hfRefree.addToWritingQueue(setStmt);
        }

        //update screen
        showPlayer();

        //enable all other buttons
        stopButton.setVisible(false);
        startButton.setVisible(true);
        pager.setDisable(false);
        searchButton.setDisable(false);
        testButtons.stream().forEach(t -> t.setDisable(false));
    }

    @FXML
    void closeButtonClicked(MouseEvent event) {
        System.exit(0);
    }

    @FXML
    void searchButtonClicked(MouseEvent event) {
        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setTitle("Search Player");
        codeDialog.setHeaderText("Enter the exact code of the player");
        codeDialog.setContentText("Code:");
        codeDialog.showAndWait().ifPresent(
                (code) -> {
                    Player p = players.stream()
                            .filter(player -> player.getCode().equals(code))
                            .findAny().orElse(null);
                    if (p != null) {
                        int pIndex = players.indexOf(p);
                        pager.setCurrentPageIndex(pIndex);
                        pageChangedHandler(currentPlayerIndex, pIndex);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "this player doesn't exist");
                        alert.showAndWait();
                    }
                }
        );

    }

    @FXML
    void startButtonClicked(MouseEvent event) {

        System.out.println("start button clicked");
        value = -1;
        if (currentPlayerIndex < 0 || players.isEmpty()) {
             Message.error("error", " كود اللاعب غير صحيح ", stage);
            return;
        }

        //disable and enable buttons
        startButton.setVisible(false);
        stopButton.setVisible(true);
        pager.setDisable(true);
        searchButton.setDisable(true);
        testButtons.stream().forEach(t -> t.setDisable(true));

        //start the test by writing the command to the main refree (upper case for starting the test)
        hfRefree.addToWritingQueue(String.valueOf(test).toUpperCase());
    }

    @FXML
    void flexTButtonClicked(MouseEvent event) {
        flexTButton.setSelected(true);
        test = 'F';
    }

    @FXML
    void heightTButtonClicked(MouseEvent event) {
        heightTButton.setSelected(true);
        test = 'H';

    }

    @FXML
    void jumpTButtonClicked(MouseEvent event) {
        jumpTButton.setSelected(true);
        test = 'J';
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        players = Collections.synchronizedList((List<Player>) new ArrayList<Player>());

        String baseAddress = "192.168.1";
        Socket socket = null;
        int timeout = 10;
        while (timeout <= 1000) {
            for (int i = 1; i <= 20; i++) {
                String address = String.format("%s.%d", baseAddress, i);
                System.out.println(address);
                socket = getFitnessSocket(address, 4321, timeout);
                if (socket != null) {
                    break;
                }

            }
            if (socket != null) {
                break;
            }
            timeout *= 10;

        }

        if (socket == null) {
            Message.error("Error", "لم يتم العثور على الحكم الرئيسي تأكد من ال  ip\nيجب ان يكون ال ip للجهاز الرئيسي\nمن 192.168.1.2\nالى 192.168.1.20", stage);
            System.exit(33);
        }
        hfRefree = new HFRefree(socket, players);


        hfRefree.setDataAvailableCallback((data) -> {
            if (currentPlayerIndex < 0) {
                return;
            }
            Player p = players.get(currentPlayerIndex);
            String[] parts = data.split(" ");
            char test = parts[0].charAt(0);
            int value = Integer.parseInt(parts[1]);

            this.value = value;
            System.out.println("value was " + value);
            Platform.runLater(() -> {
                switch (test) {
                    case 'H':
                        heightLabel.setText(Formatter.IntegerFormat(value, 3));
                        break;
                    case 'F':
                        flexibleLabel.setText(Formatter.doubleFormat((double) value / 2, 4));
                        break;
                    case 'J':
                        jumpLabel.setText(Formatter.IntegerFormat(value, 3));
                        break;
                    default:
                        break;
                }

            });

        });

        hfRefree.setPlayersChangedCallback(() -> {
            Platform.runLater(() -> pageChangedHandler(currentPlayerIndex, currentPlayerIndex));

        });

        //register page change event
        pager.currentPageIndexProperty().addListener(
                (obs, oldIndex, newIndex) -> {
                    pageChangedHandler((int) oldIndex, (int) newIndex);
                });

        pager.setCurrentPageIndex(0);
        pageChangedHandler(-1, 0);
        System.out.println("started. ..");
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
        if (currentPlayerIndex < 0 || players.isEmpty()) {
            player = nanPlayer;
        } else {
            player = players.get(currentPlayerIndex);
        }

        statusLabel.setText(String.format("%s >>> %s", player.getCode(), Utils.getStageInArabic(player.getStage())));
        heightLabel.setText(Formatter.IntegerFormat(player.getHeight(), 3));
        flexibleLabel.setText(Formatter.doubleFormat(player.getFlexible(), 4));
        jumpLabel.setText(Formatter.IntegerFormat(player.getJump(), 3));

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setDuties(Set<Character> duties) {
        //collect all test buttons in a single map for easy access
        Map<Character, ToggleButton> map = new HashMap<>();
        map.put('F', flexTButton);
        map.put('H', heightTButton);
        map.put('J', jumpTButton);

        //iterate over all possible tests
        for (char test : map.keySet()) {
            ToggleButton testButton = map.get(test);
            /*
             if the test in duties then add it the refree duties
             */
            if (duties.contains(test)) {
                testButtons.add(testButton);
                testButton.setSelected(true);
                this.test = test;
                hfRefree.addDuty(test);
            } else {
                testButton.setDisable(true);
            }
        }
    }

    public static Socket getFitnessSocket(String hostName, int port, int timeout) {

        // Creates a socket address from a hostname and a port number
        SocketAddress socketAddress = new InetSocketAddress(hostName, port);
        Socket socket = new Socket();

        try {
            socket.connect(socketAddress, timeout);
            return socket;

        } catch (SocketTimeoutException exception) {
            System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
        } catch (IOException exception) {
            System.out.println(
                    "IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());
        }
        return null;
    }

}
