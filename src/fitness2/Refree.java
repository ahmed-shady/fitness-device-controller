/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author aboshady
 */
public class Refree {

    private Socket connection = null;
    private Set<Character> duties;
    //unbounded queueus
    private ConcurrentLinkedQueue<String> reading_queue;
    private ConcurrentLinkedQueue<String> writing_queue;
    private volatile boolean validRefree = true;

    DataAvailableCallback dataAvailableCb;

    public Refree(Socket connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection socket mustn't be null, use no-arg constructor for main refree");
        }
        this.connection = connection;
        this.duties = new HashSet<>();
        this.reading_queue = new ConcurrentLinkedQueue<>();
        this.writing_queue = new ConcurrentLinkedQueue<>();

        Thread write_thread = new Thread() {
            @Override
            public void run() {
                try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())) {
                    while (true) {
                        if (!validRefree) {
                            break;
                        }

                        String nextCommand;
                        while ((nextCommand = Refree.this.writing_queue.poll()) != null) {
                            output.writeUTF(nextCommand);
                        }
                        

                    }
                } catch (IOException ex) {

                    Refree.this.addToReadingQueue("close");
                }
            }

        };
        write_thread.start();

        Thread read_thread = new Thread() {
            @Override
            public void run() {
                try (DataInputStream input = new DataInputStream(connection.getInputStream())) {
                    while (true) {
                        if (!validRefree) {
                            break;
                        }

                        Refree.this.reading_queue.add(input.readUTF());

                    }
                } catch (IOException ex) {
                    if(reading_queue != null)
                        Refree.this.reading_queue.add("close");
                }
                //notifyAll();
            }

        
    }

    ;
    read_thread.start ();

}

public Refree() {
        this.duties = new HashSet<>();
        this.reading_queue = new ConcurrentLinkedQueue<>();
        this.writing_queue = new ConcurrentLinkedQueue<>();
    }

    public void addToReadingQueue(String data) {
        if (reading_queue != null) {
            this.reading_queue.add(data);
        }
        //notifyAll();
    }

    public void addToWritingQueue(String data) {
        if (connection == null) {
            if (dataAvailableCb != null) {
                dataAvailableCb.call(data);
            }
        } else {
            this.writing_queue.add(data);
            //notifyAll();
        }
    }

    public Set<Character> getDuties() {
        return this.duties;
    }

    public void addDuty(Character duty) {
        this.duties.add(duty);
    }

    public void addAllDuties(Set duties) {
        this.duties.addAll(duties);
    }

    public void removeDuty(Character duty) {
        this.duties.remove(duty);
    }

    public boolean isResponsible(Character duty) {
        return this.duties.contains(duty);
    }

    public String getNextCommand() {
        if (reading_queue == null) {
            return null;
        }
        return this.reading_queue.poll();

    }

    public boolean isMainRefree() {
        return this.connection == null;
    }   

    public void setDataAvailableCallback(DataAvailableCallback dataAvailableCb) {
        this.dataAvailableCb = dataAvailableCb;
    }

    public void release() {
        try {
            if (connection != null) {
                connection.close();
                reading_queue = null;
                validRefree = false;
            }
        } catch (IOException ex) {
            Message.error("fatal error", "closing socket failed", null);
        }
    }

}
