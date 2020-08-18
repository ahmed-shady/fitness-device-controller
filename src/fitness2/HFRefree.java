/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.stage.Stage;

/**
 *
 * @author aboshady
 */
public class HFRefree {

    private Socket connection;
    private List<Player> players;
    private ConcurrentLinkedQueue<String> writing_queue;
    private PlayersChangedCallback playersChangedCb;
    DataAvailableCallback dataAvailableCb;

    public HFRefree(Socket connection, List<Player> players) {
        if (connection == null) {
            throw new IllegalArgumentException("connection socket mustn't be null, use no-arg constructor for main refree");
        }
        this.connection = connection;
        this.players = players;
        this.writing_queue = new ConcurrentLinkedQueue<>();
        Thread write_thread = new Thread() {
            @Override
            public void run() {
                try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())) {
                    while (true) {

                        String nextCommand;
                        while ((nextCommand = HFRefree.this.writing_queue.poll()) != null) {
                            output.writeUTF(nextCommand);
                        }

                    }
                } catch (Exception ex) {
                    HFRefree.this.executeCommand("close");
                    //show alert
                }
            }

        };
        write_thread.start();

        Thread read_thread = new Thread() {
            @Override
            public void run() {
                try (DataInputStream input = new DataInputStream(connection.getInputStream())) {
                    while (true) {

                        String command = input.readUTF();
                        HFRefree.this.executeCommand(command);

                    }
                } catch (Exception ex) {
                    System.out.println("close from reading");
                    HFRefree.this.executeCommand("close");

                }
                //notifyAll();
            }

        };
        read_thread.start();

    }

    public void addToWritingQueue(String data) {

        this.writing_queue.add(data);
        //notifyAll();
    }

    public void addDuty(char duty) {
        this.writing_queue.add(String.format("duty %c", duty));

    }

    /*
     possible data:
     <code> <stage> test1 test2 ...
     <test> <value>
    
     */
    public synchronized void executeCommand(String data) {
        String parts[] = data.split(" ");
        if (parts.length == 2) {
            //data fromat: <test> <value>
            dataAvailableCb.call(data);
        } else if (parts[0].equals("close")) {
            //data format: close
            System.out.println("connection has been lost...");
            release();

            System.exit(25);
        } else {
            //data format <code> <stage> <height> <flexible> ...
            int len = parts.length;
            int i = len - 7;

            String code = String.join(" ", Arrays.copyOf(parts, i));
            
            //null means unknown and don't matter for this operation
            Player player = new Player(code, parts[i++], null);

            player.setHeight(Integer.parseInt(parts[i++]));
            player.setFlexible(Double.parseDouble(parts[i++]));
            player.setJump(Integer.parseInt(parts[i++]));
            player.setPushup(Integer.parseInt(parts[i++]));
            player.setSitup(Integer.parseInt(parts[i++]));
            player.setRun(Integer.parseInt(parts[i++]));
            players.add(player);
            if (this.playersChangedCb != null) {
                this.playersChangedCb.call();
            }

        }

    }

    public void setDataAvailableCallback(DataAvailableCallback dataAvailableCb) {
        this.dataAvailableCb = dataAvailableCb;
    }

    public void setPlayersChangedCallback(PlayersChangedCallback playersChangedCb) {
        this.playersChangedCb = playersChangedCb;
    }

    public void release() {
        try {
            connection.close();
        } catch (IOException ex) {
            System.out.println("closing socket failed");
        }
    }

}
