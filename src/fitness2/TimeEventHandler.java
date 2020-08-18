/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitness2;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 *
 * @author aboshady
 */
class TimeEventHandler extends TimerTask {

    private int time;
    private TickCallBack cb;

    public TimeEventHandler(int time) {
        this.time = time;
    }

    public void setTickCallBack(TickCallBack cb) {
        this.cb = cb;
    }

    @Override
    public void run() {
        time--;
        cb.call(time);

    }

}
