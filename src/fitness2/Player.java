/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

import java.util.Objects;


/**
 *
 * @author aboshady
 */
class Player {

    private final String code;
    private String stage;
    private int height;
    private double flexible;
    private int pushup;
    private int situp;
    private int run;
    private int jump;
    private String name;

    public int getJump() {
        return jump;
    }

    public void setJump(int jump) {
        this.jump = jump;
    }
    public Player(String code, String stage, String name) {
        this.code = code;
        this.stage = stage;
        this.name = name;
        this.height = -1;
        this.flexible = -1;
        this.pushup = -1;
        this.situp = -1;
        this.run= -1;
        this.jump = -1;
    }

    public String getCode() {
        return this.code;
    }

    public String getStage() {
        return this.stage;
    }
    
    public String getName(){
        return this.name;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getFlexible() {
        return flexible;
    }

    public void setFlexible(double flexible) {
        this.flexible = flexible;
    }

    public int getPushup() {
        return pushup;
    }

    public void setPushup(int pushupCount) {
        this.pushup = pushupCount;
    }

    public int getSitup() {
        return situp;
    }

    public void setSitup(int situpCount) {
        this.situp = situpCount;
    }

    public int getRun() {
        return run;
    }

    public void setRun(int runCount) {
        this.run = runCount;
    }

    public int getTest(String test) {
        if (test.equals("PUSHUP")) {
            return this.getPushup();
        } else if (test.equals("SITUP")) {
            return this.getSitup();
        } else if (test.equals("RUN")) {
            return this.getRun();
        }else if (test.equals("JUMP")) {
            return this.getJump();
        } else {
            return -1;
        }
    }

    public void setTest(String test, int data) {

        if (test.equals("PUSHUP")) {
            this.setPushup(data);
        } else if (test.equals("SITUP")) {
            this.setSitup(data);
        } else if (test.equals("RUN")) {
            this.setRun(data);
        }else if (test.equals("JUMP")) {
            this.setJump(data);
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s %d %f %d %d %d %d", code, stage, height, flexible, jump, pushup, situp, run); 
    }
    @Override
    public boolean equals(Object p){
        if(p instanceof Player) {
            Player player = (Player) p;
            return code.equals(player.code);
        } else
            throw new IllegalArgumentException();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.code);
        return hash;
    }
}
