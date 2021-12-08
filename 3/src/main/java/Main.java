import com.google.gson.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;



class placeFieldListener implements KeyListener {
    Main parent;

    public placeFieldListener(Main parent) {
        this.parent = parent;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                parent.variantsBox.removeAllItems();
                parent.variantsBox.addActionListener(new comboBoxListener(parent));
                JsonArray variants = parent.NI.getVariants(parent.placeField.getText());
                for (var a : variants) {
                    JsonObject cur = a.getAsJsonObject();

                    parent.variantsBox.addItem(new Point(cur.get("point").getAsJsonObject().get("lat").getAsFloat(),
                                    cur.get("point").getAsJsonObject().get("lng").getAsFloat(),
                                    cur.get("name").toString()));

                    System.out.println(a.toString());
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
class comboBoxListener implements ActionListener {
    Main parent;

    public comboBoxListener(Main parent) {
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            JComboBox cb = (JComboBox)e.getSource();
            Point point = (Point)cb.getSelectedItem();
            if (point == null)
                return;
            parent.weatherArea.setText(parent.NI.getWeather(point));
            Place[] places = parent.NI.getListOfPlaces(point);
            for (int i = 0; i < places.length; i++) {
                parent.NI.getDescriptionByXid(places[i]);
            }
            parent.answerArea.setText("");
            for (int i = 0; i < places.length; i++) {
                parent.answerArea.append(places[i].toString() + "\n");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }
}

public class Main extends JFrame {
    JFrame mainFrame;
    JTextField placeField;
    JComboBox variantsBox;
    NetworkInteraction NI;
    JTextArea answerArea;
    JTextArea weatherArea;

    public static void main(String[] args) throws IOException, InterruptedException {
        new Main();
    }
    public Main() throws IOException, InterruptedException {
        mainFrame = new JFrame("Лабораторная 3");
        mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        Container mainPanel = mainFrame.getContentPane();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        placeField = new JTextField();
        placeField.addKeyListener(new placeFieldListener(this));
        variantsBox = new JComboBox();
        answerArea = new JTextArea("Данные о месте");
        answerArea.setEditable(false);
        weatherArea = new JTextArea("Данные о погоде");
        weatherArea.setEditable(false);
        variantsBox.addActionListener(new comboBoxListener(this));


        mainPanel.add(placeField);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(variantsBox);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(answerArea);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(weatherArea);

        NI = new NetworkInteraction();
        //System.out.println(NI.getWeather(new Point(59, 59, "1")));
        //NI.getListOfPlaces(new Point(1, 1, "1"));

        mainFrame.setSize(400, 400);
        mainFrame.setVisible(true);

    }
}
