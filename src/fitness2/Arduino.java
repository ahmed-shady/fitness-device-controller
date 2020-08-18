package fitness2;

/**
 *
 * @author aboshady
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import jssc.SerialPort;
import jssc.SerialPortException;

public class Arduino {

    /**
     * @param args the command line arguments
     * @throws jssc.SerialPortException
     */
    final static byte PUSHUP_CMD = 'P', PUSHUP_END_CMD = 'p';
    final static byte SITUP_CMD = 'S', SITUP_END_CMD = 's';
    final static byte RUN_COMD = 'R', RUN_END_CMD = 'r';
    final static byte JUMP_CMD = 'J', JUMP_END_CMD = 'j';
    final static byte HEIGHT_CMD = 'H', HEIGHT_END_CMD = 'h';
    final static byte FLEXIBLE_CMD = 'F', FLEXIBLE_END_CMD = 'f';
    final static byte LASER_CMD = 'L';
    final static byte END_CMD = 'E';

    SerialPort port;
    private int data;
    private ArrayList<Integer> heightDataList;
    private ArrayList<Integer> flexibleDataList;
    private ArrayList<Integer> jumpDataList;
    private SerialAvailableCallBack cb;

    public void end() {
        try {
            port.writeByte(END_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void endPushup() {
        try {
            port.writeByte(PUSHUP_END_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void endSitup() {
        try {
            port.writeByte(SITUP_END_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void endRun() {
        try {
            port.writeByte(RUN_END_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void endJump() {
        try {
            port.writeByte(JUMP_END_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void endFlexible() {
        try {
            port.writeByte(FLEXIBLE_END_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void endHeight() {
        try {
            port.writeByte(HEIGHT_END_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void pushup() {
        data = 0;

        try {
            port.writeByte(PUSHUP_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void situp() {
        data = 0;
        try {
            port.writeByte(SITUP_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void run() {
        data = 0;
        try {
            port.writeByte(RUN_COMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void jump() {

        jumpDataList = new ArrayList<>();
        try {
            port.writeByte(JUMP_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }

    }

    public void height() {

        heightDataList = new ArrayList<>();
        try {
            port.writeByte(HEIGHT_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void flexible() {

        flexibleDataList = new ArrayList<>();
        try {
            port.writeByte(FLEXIBLE_CMD);

        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }
    }

    public void laser() {

        try {

            port.writeByte(LASER_CMD);
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            System.exit(0);
        }

    }

    public int getData() {
        return data;
    }

    public void setSerialAvailableCallBack(SerialAvailableCallBack cb) {
        this.cb = cb;
    }

    public void close() {
        try {
            boolean closed = port.closePort();
            if (!closed) {
                System.out.println("not closed");
            }
        } catch (SerialPortException ex) {
            System.out.println("arduino  disconnected");
        }
    }

    public Arduino() {
        data = 0;
        try {
            port = new SerialPort("COM3");
            port.openPort();
            port.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            port.purgePort(SerialPort.PURGE_RXCLEAR);
            port.purgePort(SerialPort.PURGE_TXCLEAR);

            port.addEventListener((ev) -> {
                try {
                    byte[] b = port.readBytes(2);
                    char test = (char) b[0];
                    int value = b[1] + ((b[1] < 0) ? 256 : 0);
                    System.out.println(String.format("arduino test: %c, value: %d", test, value));
                    if (test == HEIGHT_CMD) {
                        heightDataList.add(value);
                        value = calculateHeight();
                    } else if (test == FLEXIBLE_CMD) {
                        flexibleDataList.add(value);
                        value = calculateFlexible();
                    } else if (test == JUMP_CMD) {
                        jumpDataList.add(value);
                        value = calculateJump();
                    } else {

                        value += data;
                        data = value;
                        System.out.println(value);
                    }

                    if (cb != null) {
                        cb.call(test, value);
                    }

                } catch (SerialPortException ex) {
                    System.out.println("error receiving serial...");
                }
            });
        } catch (SerialPortException ex) {
            System.out.println("arduino disconnected");
            Message.error("error", "برجاء توصيل الجهاز واعادة تشغيل البرنامج", null);
            System.exit(0);

        }

    }

    private int calculateHeight() {
        //Collections.sort(dataList);
        OptionalDouble averageHeight
                = heightDataList.stream()
                        .filter(h -> (h > 100))
                        .mapToInt(Integer::intValue)
                        .average();
        if (averageHeight.isPresent()) //return as nearest integer
        {
            return (int) (averageHeight.getAsDouble() + 0.5);
        } else {
            return 0;
        }

    }

    private int calculateFlexible() {
        /*may return null, test it*/
        Optional<Integer> maxFlex
                = flexibleDataList.stream()
                        .filter(i -> Collections.frequency(flexibleDataList, i) >= 2)
                        .collect(Collectors.toSet())
                        .stream()
                        .max(Comparator.comparing(Integer::valueOf));
        if (maxFlex.isPresent()) {
            return maxFlex.get();
        } else {
            return 0;
        }

    }

    private int calculateJump() {
        if (jumpDataList.isEmpty()) {
            return 0;
        }
        return Collections.max(jumpDataList);
    }
}
