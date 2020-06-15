import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ServerLauncher {

    Socket clientSocket = null;
    ServerSocket serverSocket = null;
    ServerThread serverThread = null;
    private Vector<TCPConnection> connections= new Vector<>();
    private HashMap<TCPConnection,TCPConnection> helpConnections = new HashMap<>();
    private DataBase db = Singleton.getCollection().getDbConnection();


    public ServerLauncher() throws SQLException {
        JFrame jFrame = new JFrame("Server");
        jFrame.setResizable(false);
        jFrame.setVisible(false);
        jFrame.setPreferredSize(new Dimension(300,200));
        jFrame.setBounds(900,600,300,300);
        jFrame.setDefaultCloseOperation(jFrame.DO_NOTHING_ON_CLOSE);

        JPanel jPanel = new JPanel();

        JTextArea Players = new JTextArea();
        Players.setFont(new Font("TimesRoman", Font.ITALIC, 14));
        Players.setText("Текущие колличество игроков в лобби:" + db.numberLobby());
        Players.setEditable(false);
        Players.setEditable(false);

        JTextArea Readys = new JTextArea();
        Readys.setFont(new Font("TimesRoman", Font.ITALIC, 14));
        Readys.setText("Готовые игроки:" + db.numberReady());
        Readys.setEditable(false);
        Players.setEditable(false);

        JButton start = new JButton("Запустить");
        start.setFocusable(false);
//        start.setEnabled(false);

        JButton stop = new JButton("Остановить сервер");

        stop.setFocusable(false);
        stop.setEnabled(false);

        JButton Refresh = new JButton("Обновить данные");
        Refresh.setEnabled(true);

        JButton Exit = new JButton("Выход");
        Exit.setEnabled(true);
        Exit.setFocusable(false);

        Container server = jFrame.getContentPane();
        server.setLayout(new FlowLayout(FlowLayout.CENTER));
        jPanel.add(start);
        jPanel.add(stop);
        server.add(Refresh, BorderLayout.SOUTH);
        server.add(Players);
        server.add(Readys);
        server.add(jPanel, BorderLayout.SOUTH);

        server.add(Exit);
        jFrame.pack();
        jFrame.setVisible(true);


        //Слушатели
        start.addActionListener(e -> {
            try {
                stop.setEnabled(true);
                start.setEnabled(false);
                serverSocket = new ServerSocket(8081);
                //bgChecker = new BackgroundChecker();
                serverThread = new ServerThread(this);
                //new Thread(bgChecker).start();
                new Thread(serverThread).start();
                Exit.setEnabled(false);
                System.out.println("Сервер запущен");


            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        stop.addActionListener(e -> {
            try {
                System.out.println("Сервер остановлен");
                stop.setEnabled(false);
                start.setEnabled(true);
                serverThread.stop();
                serverSocket.close();
                db.clearLobby();
                db.clearReady();
                db.clearGame();
                db.clearHistory();
                Exit.setEnabled(true);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Refresh.addActionListener(e -> {
            try {
                Players.setText("Текущие колличество игроков в лобби:" + db.numberLobby());
                Readys.setText("Готовые игроки:" + db.numberReady());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        Exit.addActionListener(e -> {
            jFrame.dispose();
            System.exit(0);
        });

    }

    //TODO Закрытие сокетов клиента
    public class ServerThread implements Runnable {
        ServerLauncher server;
        boolean running = true;
        ServerThread(ServerLauncher server){this.server=server;}
        public void stop(){
            running=false;
        }
        @Override
        public void run() {
            while(running) {
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    //e.printStackTrace();
                    //running=false;
                    break;
                }
                if (!running){break;}
                TCPConnection client = new TCPConnection(clientSocket);
                connections.add(client);
                new Thread(client).start();

            }
        }
    }
}
