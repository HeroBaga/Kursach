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
        jPanel.setLayout(new GridLayout(13, 5, 5, 5));
        Container container = jFrame.getContentPane();

        JButton Host = new JButton("Создать");
        Host.setFocusable(false);
        Host.setEnabled(true);
        JButton Connect = new JButton("Присоедениться");
        Connect.setFocusable(false);
        Connect.setEnabled(true);
        JButton Rules = new JButton("Правила");
        Rules.setFocusable(false);
        Rules.setEnabled(true);
        JButton Exit = new JButton("Выход");
        Exit.setFocusable(false);
        Exit.setEnabled(true);

        jFrame.setPreferredSize(new Dimension(width, height));
        jFrame.setBounds(dimension.width / 2 - width / 2, dimension.height / 2 - height / 2, width, height);
        jFrame.pack();

        jPanel.add(Host); // Номер 0 в контейнере controlPanel (не изменять)
        jPanel.add(Connect); // Номер 1 в контейнере controlPanel (не изменять)
        jPanel.add(Rules);
        jPanel.add(Exit);

        container.add(jPanel, BorderLayout.SOUTH); // Номер 0 в контейнере container (не изменять)

        Host.addActionListener(e -> HostGame(container));
        Connect.addActionListener(e->ConnectGame(container));
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

    public void draw(Graphics g, JPanel field) {
        g.drawImage(image, 0, 0, null);
    }

    public void HostGame(Container container){
System.out.println("Попытка создать сервер");
        new MultiThreadServer();
    }

    public void ConnectGame(Container container) {
//        запускаем подключение сокета по известным координатам и нициализируем приём сообщений с консоли клиента
        try (Socket socket = new Socket("localhost", 3345);
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
             DataInputStream ois = new DataInputStream(socket.getInputStream());) {

            System.out.println("Client connected to socket.");
            System.out.println();
            System.out.println("Client writing channel = oos & reading channel = ois initialized.");

// проверяем живой ли канал и работаем если живой
            while (!socket.isOutputShutdown()) {

// ждём консоли клиента на предмет появления в ней данных
                if (br.ready()) {

// данные появились - работаем
                    System.out.println("Client start writing in channel...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String clientCommand = br.readLine();

// пишем данные с консоли в канал сокета для сервера
                    oos.writeUTF(clientCommand);
                    oos.flush();
                    System.out.println("Clien sent message " + clientCommand + " to server.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
// ждём чтобы сервер успел прочесть сообщение из сокета и ответить

// проверяем условие выхода из соединения
                    if (clientCommand.equalsIgnoreCase("quit")) {

// если условие выхода достигнуто разъединяемся
                        System.out.println("Client kill connections");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

// смотрим что нам ответил сервер на последок перед закрытием ресурсов
                        if (ois.read() > -1) {
                            System.out.println("reading...");
                            String in = ois.readUTF();
                            System.out.println(in);
                        }

// после предварительных приготовлений выходим из цикла записи чтения
                        break;
                    }

// если условие разъединения не достигнуто продолжаем работу
                    System.out.println("Client sent message & start waiting for data from server...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

// проверяем, что нам ответит сервер на сообщение(за предоставленное ему время в паузе он должен был успеть ответить)
                    if (ois.read() > -1) {

// если успел забираем ответ из канала сервера в сокете и сохраняем её в ois переменную,  печатаем на свою клиентскую консоль
                        System.out.println("reading...");
                        String in = ois.readUTF();
                        System.out.println(in);
                    }
                }
            }
// на выходе из цикла общения закрываем свои ресурсы
            System.out.println("Closing connections & channels on clentSide - DONE.");

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}

