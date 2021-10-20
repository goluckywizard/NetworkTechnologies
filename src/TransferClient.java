import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class TransferClient {
    static int sizeOfPart = 100;
    public static void main(String[] args) throws IOException {
        String filename = args[0];
        String IPAddress = args[1];
        int port = Integer.parseInt(args[2]);
        //InetAddress serverAddress = InetAddress.getByName(IPAddress);
        File file = new File(filename);
        Socket s = new Socket(IPAddress, port);
        DataInputStream fromServer = new DataInputStream(s.getInputStream());
        DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
        System.out.println("Size of file: " + file.length());
        toServer.writeLong(file.length());
        byte[] filenameBytes = file.getName().getBytes(StandardCharsets.UTF_8);
        toServer.writeInt(filenameBytes.length);
        toServer.write(filenameBytes);
        FileInputStream fromFile = new FileInputStream(file);
        toServer.flush();
        long readYet = 0;
        while (readYet != file.length()) {
            byte[] partOfFile = fromFile.readNBytes(sizeOfPart);
            readYet += partOfFile.length;
            toServer.write(partOfFile);
        }
        toServer.flush();
        boolean isReady = fromServer.readBoolean();
        if (isReady) {
            System.out.println("Файл успешно отправлен!");
        } else {
            System.out.println("Файл отправлен не полностью!");
        }
    }
}
