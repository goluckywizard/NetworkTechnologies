import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    static int port = 1234;
    final static long timeout = 5000;
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0) {
            InetAddress address = InetAddress.getByName(args[0]);
            Map<String, Long> activity = new HashMap<>();
            ResponseBool isEdit = new ResponseBool();
            MulticastSocket recvSocket = new MulticastSocket(port);
            MulticastSocket sendSocket = new MulticastSocket();
            sendSocket.joinGroup(address);
            recvSocket.joinGroup(address);
            ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
            InputThread inputThread = new InputThread(recvSocket, activity, isEdit);
            OutputThread outputThread = new OutputThread(sendSocket, activity, address, port);
            CheckThread checkThread = new CheckThread(activity, timeout, isEdit);
            inputThread.start();
            schedule.scheduleAtFixedRate(outputThread, 0, 1, TimeUnit.SECONDS);
            schedule.scheduleAtFixedRate(checkThread, 0, 1, TimeUnit.SECONDS);
        }
    }
}

class InputThread extends Thread {
    final int recvBufSize = 1000;
    MulticastSocket socket;
    Map<String, Long> activity;
    ResponseBool isEdit;

    public InputThread (MulticastSocket socket, Map<String, Long> activity, ResponseBool isEdit) {
        this.socket = socket;
        this.activity = activity;
        this.isEdit = isEdit;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            byte buf[] = new byte[recvBufSize];
            DatagramPacket recv = new DatagramPacket(buf, recvBufSize);
            try {
                socket.receive(recv);
                //System.out.println(recv.getData());
                Date date = new Date();
                if (!activity.containsKey(recv.getAddress() + ":" + recv.getPort())) {
                    isEdit.setEdit(true);
                }
                activity.put(recv.getAddress() + ":" + recv.getPort(), date.getTime());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

class OutputThread implements Runnable {
    MulticastSocket socket;
    Map<String, Long> activity;
    InetAddress address;
    int port;
    public OutputThread (MulticastSocket socket, Map<String, Long> activity, InetAddress address, int port) {
        this.socket = socket;
        this.activity = activity;
        this.address = address;
        this.port = port;
    }

    public void run() {
        String message = "Hello!";
        try {
            socket.send(new DatagramPacket(message.getBytes(), message.length(), address, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class CheckThread implements Runnable {
    long timeout;
    Map<String, Long> activity;
    ResponseBool isEdit;
    public CheckThread (Map<String, Long> activity, long timeout, ResponseBool isEdit) {
        this.activity = activity;
        this.timeout = timeout;
        this.isEdit = isEdit;
    }
    public void run() {
        Date date = new Date();
        for (var a : activity.entrySet()) {
            if (date.getTime() - a.getValue() > timeout) {
                activity.remove(a.getKey());
                isEdit.setEdit(true);
            }
        }
        for (var a : activity.entrySet()) {
            System.out.println(a.getKey());
        }
        System.out.println("____________________________");
    }
}
class ResponseBool {
    private boolean isEdit = true;

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }
}
