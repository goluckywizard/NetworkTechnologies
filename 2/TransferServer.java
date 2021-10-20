import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TransferServer {
    public static void main(String[] args) throws IOException {
        printInfo info = new printInfo();
        int port = Integer.parseInt(args[0]);
        System.out.println("port: " + port);
        File uploadsDir = new File("uploads");
        if (uploadsDir.mkdir()) {
            System.out.println("Папка uploads создана!");
        }
        ServerSocket s = new ServerSocket(port);
        while (true) {
            Socket clientSocket = s.accept();
            ClientProcessor client = new ClientProcessor(uploadsDir, clientSocket, info);
            client.start();
        }
    }
}

class ClientProcessor extends Thread {
    long filesize;
    long uploadSize;

    File uploads;
    Socket socket;
    int sizeOfPart = 100;
    printInfo info;
    ClientProcessor(File uploads, Socket s, printInfo info) {
        this.uploads = uploads;
        this.socket = s;
        this.info = info;
    }
    public void run() {
        InputStream input;
        DataInputStream dataInput;
        try {
            ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
            SpeedTest speedTest = new SpeedTest(info);
            input = socket.getInputStream();
            dataInput = new DataInputStream(input);
            long fileSize;
            int filenameSize;
            byte[] filename;
            fileSize = dataInput.readLong();
            filenameSize = dataInput.readInt();
            filename = dataInput.readNBytes(filenameSize);
            String name = new String(filename, StandardCharsets.UTF_8);
            speedTest.setFilename(name);
            System.out.println(fileSize);
            System.out.println(name);
            File file = new File(uploads + File.separator + name);
            file.createNewFile();
            long loadSize = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            Date date = new Date();
            long startTime = date.getTime();
            speedTest.setStart(startTime);
            schedule.scheduleAtFixedRate(speedTest, 3, 3, TimeUnit.SECONDS);
            while (loadSize != fileSize) {
                byte[] newPart;
                //System.out.print((fileSize - loadSize) + ":");
                if ((fileSize - loadSize) >= (long)sizeOfPart) {
                    newPart = dataInput.readNBytes(sizeOfPart);
                } else {
                    //System.out.print();
                    newPart = dataInput.readNBytes((int) (fileSize % sizeOfPart));
                }
                //newPart = dataInput.readNBytes(sizeOfPart);
                fileOutputStream.write(newPart);
                loadSize += newPart.length;
                //System.out.print(loadSize);
                speedTest.plusUploadBytes(newPart.length);
                //System.out.println(loadSize + " " + fileSize);
            }
            this.filesize = fileSize;
            this.uploadSize = loadSize;
            schedule.shutdownNow();
            speedTest.run();
            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                //System.out.println("finally");
                DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
                toClient.writeBoolean(filesize == uploadSize);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class SpeedTest implements Runnable {
    long start;
    long previousUploadBytes = 0;
    long uploadBytes = 0;
    long previousTime;
    String filename;
    printInfo info;

    public SpeedTest(printInfo info) {
        this.info = info;
    }

    public SpeedTest(String filename, printInfo info) {
        this.filename = filename;
        this.info = info;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setStart(long start) {
        this.start = start;
        previousTime = start;
    }

    public void plusUploadBytes(long uploadBytes) {
        //this.previousUploadBytes = this.uploadBytes;
        this.uploadBytes += uploadBytes;
    }

    public void run() {
        Date date = new Date();
        long now = date.getTime();
        synchronized (info) {
            info.print("Для файла " + filename + ":");
            try {
                info.print("Моментальная скорость: " + uploadBytes / ((now - previousTime) / 1000));
            } catch (ArithmeticException err) {
                System.out.println("Не удалось посчитать моментальную скорость (слишком быстро)");
            }
            //System.out.println();
            previousTime = now;
            previousUploadBytes += uploadBytes;
            try {
                info.print("Средняя скорость: " + previousUploadBytes / ((now - start) / 1000));
            } catch (ArithmeticException err) {
                System.out.println("Не удалось посчитать среднюю скорость (слишком быстро)");
            }
            info.print("_______________________________________");
            //System.out.println();
            uploadBytes = 0;
        }
    }
}

class printInfo {
    public void print(String str) {
        System.out.println(str);
    }
}
