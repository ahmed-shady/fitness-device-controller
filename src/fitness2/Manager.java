/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author aboshady
 */
public class Manager extends Thread {

    private List<Player> players;
    private final PlayerService playerService;
    //PlayersChangedCallback object
    PlayersChangedCallback playersChangedCb;

    Arduino arduino;
    private final List<Refree> refrees;

    public Manager(List<Player> players, PlayerService playerService, List<Refree> refrees) {
        arduino = new Arduino();
        this.players = players;
        this.playerService = playerService;
        this.refrees = refrees;
        arduino.setSerialAvailableCallBack(
                (test, value) -> {
                    //detrmine who has duty quals command
                    Refree refree = refrees.stream().filter(r -> r.getDuties().contains(test)).findAny().orElse(null);
                    if (refree == null) {
                        System.out.println(String.format("no refree is responisble for duty %c", test));
                    } else {
                        refree.addToWritingQueue(String.format("%c %d", test, value));
                    }
                    //add the the value as a string to the writing_queue

                }
        );

    }

    @Override
    public void run() {

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException x) {
            }

            ArrayList<Refree> refreesCopy = new ArrayList<>(refrees);
            for (Refree refree : refreesCopy) {
                //check if another thread remove a refree
                //ignore the refree
                //don't worry in the next scan the refree will be removed
                if (!refrees.contains(refree)) {
                    continue;
                }

                String command;
                do {
                    command = refree.getNextCommand();

                    if (command != null) {
                        System.out.println("command is " + command);
                        this.executeCommand(command, refree);
                    }
                } while (command != null);

            }

        }

    }

    public void setPlayersChangedCallback(PlayersChangedCallback playersChangedCb) {
        this.playersChangedCb = playersChangedCb;
    }

    public void executeCommand(String data, Refree refree) {
        String parts[] = data.split(" ");
        String command = parts[0];
        if (command.equals("H")) {
            arduino.height();
        } else if (command.equals("F")) {
            arduino.flexible();
        } else if (command.equals("P")) {
            arduino.pushup();
        } else if (command.equals("S")) {
            arduino.situp();
        } else if (command.equals("R")) {
            arduino.run();
        } else if (command.equals("J")) {
            arduino.jump();
        } else if (command.equals("L")) {
            arduino.laser();
        } else if (command.equals("h")) {
            arduino.endHeight();
        } else if (command.equals("f")) {
            arduino.endFlexible();
        } else if (command.equals("p")) {
            arduino.endPushup();
        } else if (command.equals("s")) {
            arduino.endSitup();
        } else if (command.equals("r")) {
            arduino.endRun();
        } else if (command.equals("j")) {
            arduino.endJump();
        } else if (command.equals("E")) {
            arduino.end();
        } else if (command.equals("set")) {
            //get the value and the code
            String c = "";
            int len = parts.length;

            //get the code
            for (int i = 1; i < len - 2; i++) {
                c += parts[i] + " ";
            }
            String code = c.substring(0, c.length() - 1);
            Player player = players.stream().filter(p -> p.getCode().equals(code)).findFirst().orElse(null);
            if (player == null) {
                System.out.println(String.format("the player %s doesn't exist", code));
                return;
            }

            //get the excerize
            String test = parts[len - 2];
            int value = Integer.parseInt(parts[len - 1]);
            if (test.equals("FLEXIBLE")) {
                player.setFlexible((double) value / 2);

            } else if (test.equals("HEIGHT")) {
                player.setHeight(value);
            } else {
                player.setTest(test, value);
            }

            playerService.update(player);
            this.playersChangedCb.call();
            //get the player with code <code> from players array
            //set the test with the value <value>
            //update the playerSerivce
            //call playerChangedCallback passed from main thread
        } else if (command.equals("add")) {
            String playerData = data.substring(4);
            refrees.stream().filter(r -> !r.isMainRefree()).forEach(r -> r.addToWritingQueue(playerData));
        }else if (command.equals("duty")) {
            char duty = parts[1].charAt(0);
            //get main refree
            Refree mainRefree = refrees.stream().filter(r -> r.isMainRefree()).collect(Collectors.toList()).get(0);
            if (mainRefree.isResponsible(duty)) {
                refree.addDuty(duty);
                mainRefree.removeDuty(duty);
            } else {
                //close th refree if his duty is assigned to another refree
                refree.addToReadingQueue("close");
            }

        } else if (command.equals("close")) {
            //close refree socket but it's closed !
            System.out.println("removing a refree...");
            refree.release();
            //get the main refree
            Refree mainRefree = refrees.stream().filter(r -> r.isMainRefree()).collect(Collectors.toList()).get(0);
            mainRefree.addAllDuties(refree.getDuties());
            for(char duty: refree.getDuties()){
                switch(duty){
                    case 'F':
                        arduino.endFlexible();
                        break;
                    case 'H':
                        arduino.endHeight();
                        break;
                        
                    case 'J':
                        arduino.endJump();
                        break;
                    default:
                        break;
                      
                }
            }
            refrees.remove(refree);
        }

    }

}
