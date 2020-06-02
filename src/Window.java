import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Window extends JPanel {
    private static Socket clientDialog;
    private Image image;
    BufferedImage background;
    int width = 1024;
    int height = 680;


    public Window() {
        try {
            image = ImageIO.read(new File("./src/Background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        JFrame jFrame = new JFrame("Perudo");

        jFrame.setVisible(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jField = new JPanel();
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(1, 1, 5, 5));
        Container container = jFrame.getContentPane();

        JMenuBar mainMenu = new JMenuBar();
        JMenu main = new JMenu("Приложение");
        JMenuItem Connect = new JMenuItem("Присоедениться");
        main.add(Connect);
        JMenuItem Rules = new JMenuItem("Правила");
        main.add(Rules);
        JMenuItem Exit = new JMenuItem("Выйти");
        main.add(Exit);
        mainMenu.add(main);
        jFrame.setJMenuBar(mainMenu);


//        JButton Host = new JButton("Создать");
//        Host.setFocusable(false);
//        Host.setEnabled(true);
//        JButton Connect = new JButton("Присоедениться");
//        Connect.setFocusable(false);
//        Connect.setEnabled(true);
//        JButton Rules = new JButton("Правила");
//        Rules.setFocusable(false);
//        Rules.setEnabled(true);
//        JButton Exit = new JButton("Выход");
//        Exit.setFocusable(false);
//        Exit.setEnabled(true);

        jFrame.setPreferredSize(new Dimension(width, height));
        jFrame.setBounds(dimension.width / 2 - width / 2, dimension.height / 2 - height / 2, width, height);
        jFrame.pack();

//        jPanel.add(Host); // Номер 0 в контейнере controlPanel (не изменять)
//        jPanel.add(Connect); // Номер 1 в контейнере controlPanel (не изменять)
//        jPanel.add(Rules);
//        jPanel.add(Exit);

        container.add(jPanel, BorderLayout.NORTH); // Номер 0 в контейнере container (не изменять)
//        container.add(jField, BorderLayout.NORTH);
        Connect.addActionListener(e -> ConnectGame(container));
        Exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jFrame.dispose();
            }
        });

        jFrame.setPreferredSize(new Dimension(width, height));
        jFrame.pack();
        jFrame.setResizable(false);
        jFrame.setVisible(true);
        jFrame.requestFocus();

    }

    public void draw(Graphics g, Container field) {
        g.drawImage(image, 0, 0, null);
    }

    public void ConnectGame(Container container) {
        draw(container.getGraphics(), container);
        Socket clientSocket;
        ObjectOutputStream out;
        ObjectInputStream in;

        try {

            clientSocket = new Socket("localhost", 8189);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            out.writeObject("Hello there");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

