import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.Vector;

public class Window extends JPanel {
//    private Image image;
//    BufferedImage background;
    String url = "https://en.wikipedia.org/wiki/Dudo";
    int width = 1024;
    int height = 680;
    Socket clientSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    User user;

    JFrame jFrame = new JFrame("Perudo");

    public Window() throws IOException {
        /*try {
            image = ImageIO.read(new File("./src/Background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        clientSocket = new Socket("localhost", 8081);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());


        jFrame.setVisible(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(1, 1, 5, 5));

        JMenuBar mainMenu = new JMenuBar();
        JMenu main = new JMenu("Приложение");
        JMenuItem Connect = new JMenuItem("Присоединиться");
        main.add(Connect);
        JMenuItem Rules = new JMenuItem("Правила");
        main.add(Rules);
        JMenuItem Exit = new JMenuItem("Выйти");
        main.add(Exit);
        mainMenu.add(main);
        jPanel.add(mainMenu);

        Container container = jFrame.getContentPane();

        jFrame.setPreferredSize(new Dimension(width, height));
        jFrame.setBounds(dimension.width / 2 - width / 2, dimension.height / 2 - height / 2, width, height);
        jFrame.pack();

        container.add(jPanel, BorderLayout.NORTH); // Номер 0 в контейнере container (не изменять)

        Rules.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException ex) {
                System.err.println("Ошибка страницы. " + ex.getLocalizedMessage());
            }


        });
        Connect.addActionListener(e -> ConnectLobby(container));

        Exit.addActionListener(actionEvent -> jFrame.dispose());

        jFrame.setPreferredSize(new Dimension(width, height));

        jFrame.setResizable(false);
        jFrame.setVisible(true);


    }

//    public void draw(Graphics g, Container field) {
//        g.drawImage(image, 0, 0, null);
//    }

    public void ConnectLobby(Container container) {
        //Для отрисовки
        //draw(container.getGraphics(), container);

        JPanel controlPanel = (JPanel) container.getComponent(0);
        JMenuBar jmenubar = (JMenuBar) controlPanel.getComponent(0);
        JMenu jmenu = (JMenu) jmenubar.getComponent(0);
        JMenuItem connect = (JMenuItem) jmenu.getItem(0);
        JTextArea Players = new JTextArea();
        JTextArea Readys = new JTextArea();

        connect.setEnabled(false);
        enterLobby();
        if (isInLobby()) {
            JDialog PreGame = new JDialog();
            PreGame.setTitle("Лобби");
            PreGame.setModal(true);

            Players.setFont(new Font("TimesRoman", Font.ITALIC, 14));

            Players.setText("Текущие колличество игроков в лобби:" + getLobbyNumber());
            Players.setEditable(false);
            Players.setEditable(false);

            Readys.setFont(new Font("TimesRoman", Font.ITALIC, 14));
            Readys.setText("Готовые игроки:" + getReadyNumber());
            Readys.setEditable(false);
            Players.setEditable(false);

            JPanel ControlButton = new JPanel();
            JPanel PlayerTypeField = new JPanel();
            PlayerTypeField.setLayout(new GridLayout(2, 1, 1, 1));
            PlayerTypeField.add(Players, BorderLayout.NORTH);
            PlayerTypeField.add(Readys, BorderLayout.SOUTH);


            JButton LeaveLobby = new JButton("Покинуть лобби");
            LeaveLobby.setFocusable(false);
            JButton StartGame = new JButton("Начать игру");
            StartGame.setFocusable(false);
            StartGame.setEnabled(false);
            JButton Refresh = new JButton("Обновить лобби");

            ControlButton.add(StartGame);
            ControlButton.add(LeaveLobby);

            ButtonGroup group = new ButtonGroup();
            JRadioButton CheckforReady = new JRadioButton("Я готов", false);
            JRadioButton CheckforNotReady = new JRadioButton("Не готов", true);
            group.add(CheckforReady);
            group.add(CheckforNotReady);
            CheckforNotReady.setFocusable(false);
            CheckforReady.setFocusable(false);

            Refresh.addActionListener(e -> {
                if (isInGame()) {
                    PreGame.dispose();
                    user.setgameId(getGameId());
                    user.setColor(getMyColor());
                    InGame(container);
                } else {
                    Players.setText("Текущие колличество игроков в лобби:" + getLobbyNumber());
                    Readys.setText("Готовые игроки:" + getReadyNumber());
                }


            });

            CheckforReady.addActionListener(e -> {
                if (CheckforReady.isSelected()) {
                    CheckforNotReady.setSelected(false);
                    LeaveLobby.setEnabled(false);
                    StartGame.setEnabled(true);
                    readyToGame();


                }
            });

            CheckforNotReady.addActionListener(e -> {
                if (CheckforNotReady.isSelected()) {
                    CheckforReady.setSelected(false);
                    LeaveLobby.setEnabled(true);
                    StartGame.setEnabled(false);
                    notReadyToGame();
                }

            });

            LeaveLobby.addActionListener(e -> {
                leaveLobby();
                PreGame.dispose();
            });

            StartGame.addActionListener(e -> {
                if (!isInGame()) {
                    if (getLobbyNumber() == getReadyNumber() && getReadyNumber() >= 3) {
                        createGame();
                        PreGame.dispose();
                        user.setgameId(getGameId());
                        user.setColor(getMyColor());
                        InGame(container);
                    } else {
                        JDialog WaitError = new JDialog();
                        WaitError.setTitle("Error");
                        WaitError.setModal(true);
                        WaitError.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        JTextArea WaitErrorArea = new JTextArea();
                        WaitErrorArea.setFont(new Font("TimesRoman", Font.ITALIC, 30));
                        WaitErrorArea.setText("Минимальное количество игроков для игры 3");
                        WaitErrorArea.setEditable(false);
                        Container errorContentPane = WaitError.getContentPane();
                        errorContentPane.add(WaitErrorArea);
                        WaitError.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
                        WaitError.setResizable(false);
                        WaitError.pack();
                        WaitError.setVisible(true);
                    }

                } else {
                    JDialog WaitError = new JDialog();
                    WaitError.setTitle("Error");
                    WaitError.setModal(true);
                    WaitError.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    JTextArea WaitErrorArea = new JTextArea();
                    WaitErrorArea.setFont(new Font("TimesRoman", Font.ITALIC, 30));
                    WaitErrorArea.setText("Вы уже находитесь в игре, нажмите кнопку обновить, чтобы войти в игру.");
                    WaitErrorArea.setEditable(false);
                    Container errorContentPane = WaitError.getContentPane();
                    errorContentPane.add(WaitErrorArea);
                    WaitError.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
                    WaitError.setResizable(false);
                    WaitError.pack();
                    WaitError.setVisible(true);
                }

            });

            JPanel LobbyPanel = new JPanel();
            LobbyPanel.add(CheckforReady);
            LobbyPanel.add(CheckforNotReady);
            LobbyPanel.add(Refresh);
            Container temp = PreGame.getContentPane();
            temp.setLayout(new FlowLayout(FlowLayout.CENTER));
            temp.add(PlayerTypeField, BorderLayout.CENTER);
            temp.add(Refresh, BorderLayout.PAGE_END);
            temp.add(LobbyPanel, BorderLayout.PAGE_END);
            temp.add(ControlButton, BorderLayout.PAGE_END);
            PreGame.setResizable(false);
            PreGame.setPreferredSize(new Dimension(350, 170));
            PreGame.setBounds(700, 400, 350, 170);
            PreGame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            PreGame.pack();
            PreGame.setVisible(true);
        }

        if (!isInLobby() && !isInGame()) {
            connect.setEnabled(true);

        }
    }

    public void InGame(Container container) {
        JPanel controlPanel = (JPanel) container.getComponent(0);
        JMenuBar jmenubar = (JMenuBar) controlPanel.getComponent(0);
        JMenu jmenu = (JMenu) jmenubar.getComponent(0);
        JMenuItem connect = (JMenuItem) jmenu.getItem(0);

        //Создание игрового поля
        JPanel TheTable = new JPanel();
        TheTable.update(getGraphics());
        TheTable.revalidate();
        TheTable.repaint();
        jFrame.revalidate();
        jFrame.repaint();

        final int[] n = {1}; // счётчик для отправленных чисел

        TheTable.setLayout(new GridLayout(3, 3, 5, 5));
        JLabel Players = new JLabel("Игроки");
        JLabel Table = new JLabel("Стол");
        JLabel History = new JLabel("История");

        JTextArea CurrentDice = new JTextArea("", 20, 10);
        CurrentDice.setEditable(false);
        CurrentDice.setFocusable(false);
        CurrentDice.setLineWrap(true);
        JScrollPane CurrentDicePane = new JScrollPane(CurrentDice);

        String playersColorsAndDices = getPlayersColorsAndDices();

        Vector<String[]> vectorColorsDices = new Vector<String[]>();
        String[] s_playersColorsDices = playersColorsAndDices.split("\\%");
        for (int i = 0; i < s_playersColorsDices.length; i++) {
            vectorColorsDices.add(s_playersColorsDices[i].split("\\$"));
        }
        String text_for_CurrentDice = "";
        for (int i = 0; i < vectorColorsDices.size(); i++) {
            text_for_CurrentDice += vectorColorsDices.get(i)[0] + " - " + vectorColorsDices.get(i)[1] + "\n";
        }
        CurrentDice.setText(text_for_CurrentDice);


        JLabel UrColor = new JLabel("Ваш цвет - " + user.getColor());

        JTextArea HistoryArea = new JTextArea("", 20, 10);
        HistoryArea.setEditable(false);
        HistoryArea.setFocusable(false);
        HistoryArea.setLineWrap(true);
        JScrollPane HistoryAreaPane = new JScrollPane(HistoryArea);


        JButton ExitGame = new JButton("Покинуть игру");

        JPanel Dices = new JPanel();
        JLabel UrDice = new JLabel("Ваши кубики");
        JTextArea FirstDice = new JTextArea("", 1, 1);
        JTextArea SecondDice = new JTextArea("", 1, 1);
        JTextArea ThirdDice = new JTextArea("", 1, 1);
        JTextArea FourthDice = new JTextArea("", 1, 1);
        JTextArea FifthDice = new JTextArea("", 1, 1);
        FirstDice.setEditable(false);
        FirstDice.setEditable(false);
        SecondDice.setEditable(false);
        SecondDice.setEditable(false);
        ThirdDice.setEditable(false);
        ThirdDice.setFocusable(false);
        FourthDice.setFocusable(false);
        FourthDice.setFocusable(false);
        FifthDice.setFocusable(false);
        FifthDice.setFocusable(false);
        Dices.setLayout(new FlowLayout(FlowLayout.CENTER));
        Dices.add(UrDice);
        Dices.add(FirstDice);
        Dices.add(SecondDice);
        Dices.add(ThirdDice);
        Dices.add(FourthDice);
        Dices.add(FifthDice);

        int myResult = getMyResult();
        String myResultString = String.valueOf(myResult);
        char[] myResultStringArray = myResultString.toCharArray();
        JTextArea[] dicesAreas = {FirstDice, SecondDice, ThirdDice, FourthDice, FifthDice};
        for (int i = 0; i < myResultString.length(); i++) {
            dicesAreas[i].setText(String.valueOf(myResultStringArray[i]));
        }

        JPanel ControlBet = new JPanel();
        JPanel Bets = new JPanel();
        String[] turn = getTurn();
        String color = "";

        if (turn[0].equals(user.getUuid())) {
            color = "Ваш ход";
        } else {
            color = "Ходит " + turn[1];
        }

        JTextArea StatusArea = new JTextArea(color, 1, 10);
        StatusArea.setEditable(false);
        StatusArea.setFocusable(false);
        JButton Perudo = new JButton("Блеф");
        Perudo.setEnabled(false);
        TextField DiceValue = new TextField("", 1);    //Значение на кубике
        TextField NumberOfDice = new TextField("", 3); //Количество кубиков
        DiceValue.setEditable(true);
        NumberOfDice.setEditable(true);
        DiceValue.setFocusable(true);
        NumberOfDice.setFocusable(true);
        Perudo.setEnabled(false);
        JButton SetBet = new JButton("Сделать ставку");
        SetBet.setEnabled(false);
        JButton RefreshGame = new JButton("Обновить историю");

        if (color.equals("Ваш ход")) {
            Bets.setEnabled(true);
            DiceValue.setEditable(true);
            NumberOfDice.setEditable(true);
            SetBet.setEnabled(true);
            Perudo.setEnabled(true);
        }

        Bets.setLayout(new FlowLayout(FlowLayout.TRAILING));
        Bets.add(DiceValue);
        Bets.add(NumberOfDice);
        Bets.add(SetBet);

        ControlBet.setLayout(new FlowLayout(FlowLayout.CENTER));
        ControlBet.add(StatusArea);
        ControlBet.add(RefreshGame);
        ControlBet.add(Perudo);
        ControlBet.add(Bets);

        TheTable.add(Players);
        TheTable.add(Table);
        TheTable.add(History);
        TheTable.add(CurrentDicePane);
        TheTable.add(UrColor);
        TheTable.add(UrColor);
        TheTable.add(HistoryAreaPane);
        TheTable.add(ExitGame);
        TheTable.add(Dices);
        TheTable.add(ControlBet);

        //Слушатели кнопок
        ExitGame.addActionListener(e -> {
            JDialog ExitDialog = new JDialog();
            ExitDialog.setTitle("Выход из игры");
            ExitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            ExitDialog.setModal(true);
            JTextArea textArea = new JTextArea();
            textArea.setFont(new Font("TimesRoman", Font.BOLD, 14));
            textArea.setText("Вы уверены, что хотите покинуть игру?");
            textArea.setEditable(false);
            JButton OK = new JButton("Да");
            JButton cancel = new JButton("Отмена");

            OK.addActionListener(ex -> {
                StatusArea.setText("Вы покинули игру");
                connect.setEnabled(true);
                TheTable.removeAll();
                container.remove(1);
                jFrame.revalidate();
                jFrame.requestFocus();
                jFrame.pack();
                ExitDialog.dispose();
                imOutOfGame();
                tryToDeleteGame();
            });

            cancel.addActionListener(ex -> {
                ExitDialog.dispose();
            });

            JPanel panel = new JPanel();
            panel.add(OK);
            panel.add(cancel);
            Container container1 = ExitDialog.getContentPane();
            container1.add(textArea);
            container1.add(panel);
            ExitDialog.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
            ExitDialog.pack();
            ExitDialog.setVisible(true);
        });

        RefreshGame.addActionListener(e -> {
            Vector<String[]> history = getHistory();
            String text = "";
            if (!history.get(0)[0].equals("")) {
                for (int i = 0; i < history.size(); i++) {
                    String[] s = history.get(i);
                    if (s[1].equals("-1") && s[2].equals("-1")) {
                        text += "Игрок " + s[3] + " оспорил ставку" + "\n" + "Начался новый раунд\n";

                    } else {
                        text += "Игрок " + s[3] + " делает ставку: " + s[2] + " количеством " + s[1] + "\n";
                    }
                }
            } else {
                text = "";
            }
            HistoryArea.setText(text);

            String[] turn2 = getTurn();
            String color2 = "";
            if (turn2[0].equals(user.getUuid())) {
                color2 = "Ваш ход";
                Bets.setEnabled(true);
                SetBet.setEnabled(true);
                DiceValue.setEditable(true);
                NumberOfDice.setEditable(true);
                if (!getLastDiceAndValue()[0].equals("-1") && !getLastDiceAndValue()[1].equals("-1")) {
                    Perudo.setEnabled(true);
                } else {
                    Perudo.setEnabled(false);
                }
                Bets.revalidate();


            } else {
                color2 = "Ходит " + turn2[1];
                Bets.setEnabled(false);
                SetBet.setEnabled(false);
                DiceValue.setEditable(false);
                NumberOfDice.setEditable(false);
                Perudo.setEnabled(false);
                Bets.revalidate();
            }
            StatusArea.setText(color2);
            String playersColorsAndDices2 = getPlayersColorsAndDices();
            Vector<String[]> vectorColorsDices2 = new Vector<String[]>();
            String[] s_playersColorsDices2 = playersColorsAndDices2.split("\\%");
            for (int i = 0; i < s_playersColorsDices2.length; i++) {
                vectorColorsDices2.add(s_playersColorsDices2[i].split("\\$"));
            }
            String text_for_CurrentDice2 = "";
            for (int i = 0; i < vectorColorsDices2.size(); i++) {
                text_for_CurrentDice2 += vectorColorsDices2.get(i)[0] + " - " + vectorColorsDices2.get(i)[1] + "\n";
            }
            CurrentDice.setText(text_for_CurrentDice2);
            int myResult2 = getMyResult();
            String myResultString2 = String.valueOf(myResult2);
            char[] myResultStringArray2 = myResultString2.toCharArray();
            JTextArea[] dicesAreas2 = {FirstDice, SecondDice, ThirdDice, FourthDice, FifthDice};
            for (int i = 0; i < dicesAreas2.length; i++) {
                dicesAreas2[i].setText("");
            }
            for (int i = 0; i < myResultString2.length(); i++) {
                dicesAreas2[i].setText(String.valueOf(myResultStringArray2[i]));
            }
            for (int i = 0; i < vectorColorsDices2.size(); i++) {
                if (user.getColor().equals(vectorColorsDices2.get(i)[0]) && vectorColorsDices2.get(i)[1].equals("0")) {
                    for (int j = 0; j < dicesAreas2.length; j++) {
                        dicesAreas2[j].setText("");
                    }
                    StatusArea.setText("Вы проиграли");
                    Bets.setEnabled(false);
                    SetBet.setEnabled(false);
                    DiceValue.setEditable(false);
                    NumberOfDice.setEditable(false);
                    Perudo.setEnabled(false);
                    Bets.revalidate();

                }
            }
            if (isGameOver()) {
                for (int i = 0; i < vectorColorsDices2.size(); i++) {
                    if (user.getColor().equals(vectorColorsDices2.get(i)[0]) && !vectorColorsDices2.get(i)[1].equals("0")) {
                        for (int j = 0; j < dicesAreas2.length; j++) {
                            dicesAreas2[j].setText("");
                        }
                        StatusArea.setText("Вы выиграли");
                        Bets.setEnabled(false);
                        SetBet.setEnabled(false);
                        DiceValue.setEditable(false);
                        NumberOfDice.setEditable(false);
                        Perudo.setEnabled(false);
                        Bets.revalidate();

                    }
                }
            }

        });

        Perudo.addActionListener(e -> {

            perudo();
            setBet("-1", "-1");

            Vector<String[]> history = getHistory();
            String text = "";
            if (!history.get(0)[0].equals("")) {
                for (int i = 0; i < history.size(); i++) {
                    String[] s = history.get(i);
                    if (s[1].equals("-1") && s[2].equals("-1")) {
                        text += "Игрок " + s[3] + " оспорил ставку" + "\n" + "Начался новый раунд\n";

                    } else {
                        text += "Игрок " + s[3] + " делает ставку: " + s[2] + " количеством " + s[1] + "\n";
                    }
                }
            } else {
                text = "";
            }
            HistoryArea.setText(text);

            String[] turn2 = getTurn();
            String color2 = "";
            if (turn2[0].equals(user.getUuid())) {
                color2 = "Ваш ход";
                Bets.setEnabled(true);
                SetBet.setEnabled(true);
                DiceValue.setEditable(true);
                NumberOfDice.setEditable(true);
                Perudo.setEnabled(true);
                Bets.revalidate();


            } else {
                color2 = "Ходит " + turn2[1];
                Bets.setEnabled(false);
                SetBet.setEnabled(false);
                DiceValue.setEditable(false);
                NumberOfDice.setEditable(false);
                Perudo.setEnabled(false);
                Bets.revalidate();
            }
            StatusArea.setText(color2);
            String playersColorsAndDices2 = getPlayersColorsAndDices();
            Vector<String[]> vectorColorsDices2 = new Vector<String[]>();
            String[] s_playersColorsDices2 = playersColorsAndDices2.split("\\%");
            for (int i = 0; i < s_playersColorsDices2.length; i++) {
                vectorColorsDices2.add(s_playersColorsDices2[i].split("\\$"));
            }
            String text_for_CurrentDice2 = "";
            for (int i = 0; i < vectorColorsDices2.size(); i++) {
                text_for_CurrentDice2 += vectorColorsDices2.get(i)[0] + " - " + vectorColorsDices2.get(i)[1] + "\n";
            }
            CurrentDice.setText(text_for_CurrentDice2);
            int myResult2 = getMyResult();
            String myResultString2 = String.valueOf(myResult2);
            char[] myResultStringArray2 = myResultString2.toCharArray();
            JTextArea[] dicesAreas2 = {FirstDice, SecondDice, ThirdDice, FourthDice, FifthDice};
            for (int i = 0; i < dicesAreas2.length; i++) {
                dicesAreas2[i].setText("");
            }
            for (int i = 0; i < myResultString2.length(); i++) {
                dicesAreas2[i].setText(String.valueOf(myResultStringArray2[i]));
            }
            for (int i = 0; i < vectorColorsDices2.size(); i++) {
                if (user.getColor().equals(vectorColorsDices2.get(i)[0]) && vectorColorsDices2.get(i)[1].equals("0")) {
                    for (int j = 0; j < dicesAreas2.length; j++) {
                        dicesAreas2[j].setText("");
                    }
                    StatusArea.setText("Вы проиграли");
                    Bets.setEnabled(false);
                    SetBet.setEnabled(false);
                    DiceValue.setEditable(false);
                    NumberOfDice.setEditable(false);
                    Perudo.setEnabled(false);
                    Bets.revalidate();

                }
            }
            if (isGameOver()) {
                for (int i = 0; i < vectorColorsDices2.size(); i++) {
                    if (user.getColor().equals(vectorColorsDices2.get(i)[0]) && !vectorColorsDices2.get(i)[1].equals("0")) {
                        for (int j = 0; j < dicesAreas2.length; j++) {
                            dicesAreas2[j].setText("");
                        }
                        StatusArea.setText("Вы выиграли");
                        Bets.setEnabled(false);
                        SetBet.setEnabled(false);
                        DiceValue.setEditable(false);
                        NumberOfDice.setEditable(false);
                        Perudo.setEnabled(false);
                        Bets.revalidate();

                    }
                }
            }
        });

        SetBet.addActionListener(e -> {
            if (Integer.parseInt(DiceValue.getText()) >= 1 && Integer.parseInt(DiceValue.getText()) <= 6) {
                if (Integer.parseInt(NumberOfDice.getText()) > 0) {
                    String[] lastDiceAndValue = getLastDiceAndValue();
                    int lastDice = Integer.parseInt(lastDiceAndValue[0]); // значение предыдущей ставки
                    int lastValue = Integer.parseInt(lastDiceAndValue[1]); // кол-во предыдущей ставки
                    //значение предыдущей ставки - s[1]
                    //кол-во предыдущей ставки - s[0]

                    //Проверка на предыдущую ставку блеф или нет
                    if (!lastDiceAndValue[0].equals("-1") && !lastDiceAndValue[1].equals("-1")) {
                        //Проверка на ставку Джокер или обычная кость

                        if (Integer.parseInt(DiceValue.getText()) == lastValue) {
                            if (Integer.parseInt(NumberOfDice.getText()) > lastDice) {
                                SetBet.setEnabled(false);
                                Perudo.setEnabled(false);
                                setBet(NumberOfDice.getText(), DiceValue.getText());
                                changeTurn();
                                DiceValue.setText("");
                                NumberOfDice.setText("");
                            } else {
                                //Окно ошибки, что значение должно быть больше предыдущей ставки
                                JDialog error = new JDialog();
                                error.setTitle("Ошибка");
                                error.setModal(true);
                                error.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                JTextArea textArea = new JTextArea();
                                textArea.setFont(new Font("TimesRoman", Font.ITALIC, 30));
                                textArea.setText("Значение должно быть больше предыдущей ставки");
                                DiceValue.setText("");
                                textArea.setEditable(false);
                                Container errorContentPane = error.getContentPane();
                                errorContentPane.add(textArea);
                                error.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
                                error.setResizable(false);
                                error.pack();
                                error.setVisible(true);
                            }
                        } else {
                            if (lastValue == 1) {
                                if (Integer.parseInt(NumberOfDice.getText()) >= (int) (lastDice * 2 + 1)) {
                                    SetBet.setEnabled(false);
                                    Perudo.setEnabled(false);
                                    setBet(NumberOfDice.getText(), DiceValue.getText());
                                    changeTurn();
                                    DiceValue.setText("");
                                    NumberOfDice.setText("");
                                } else {
                                    //Окно ошибки, что значение должно быть больше предыдущей в два раза + 1
                                    JDialog error = new JDialog();
                                    error.setTitle("Ошибка");
                                    error.setModal(true);
                                    error.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                    JTextArea textArea = new JTextArea();
                                    textArea.setFont(new Font("TimesRoman", Font.ITALIC, 30));
                                    textArea.setText("Значение должно быть больше предыдущей в два раза + 1");
                                    DiceValue.setText("");
                                    textArea.setEditable(false);
                                    Container errorContentPane = error.getContentPane();
                                    errorContentPane.add(textArea);
                                    error.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
                                    error.setResizable(false);
                                    error.pack();
                                    error.setVisible(true);
                                }
                            } else {
                                if (Integer.parseInt(DiceValue.getText()) == 1) {
                                    if (Integer.parseInt(NumberOfDice.getText()) >= (int) (lastDice / 2 + 0.5)) {
                                        SetBet.setEnabled(false);
                                        Perudo.setEnabled(false);
                                        setBet(NumberOfDice.getText(), DiceValue.getText());
                                        changeTurn();
                                        DiceValue.setText("");
                                        NumberOfDice.setText("");
                                    } else {
                                        //Окно ошибки, что значение должно быть больше половины c округлением в большую сторону
                                        JDialog error = new JDialog();
                                        error.setTitle("Ошибка");
                                        error.setModal(true);
                                        error.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                        JTextArea textArea = new JTextArea();
                                        textArea.setFont(new Font("TimesRoman", Font.ITALIC, 30));
                                        textArea.setText("Значение должно быть больше половины c округлением в большую сторону");
                                        DiceValue.setText("");
                                        textArea.setEditable(false);
                                        Container errorContentPane = error.getContentPane();
                                        errorContentPane.add(textArea);
                                        error.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
                                        error.setResizable(false);
                                        error.pack();
                                        error.setVisible(true);
                                    }
                                } else {
                                    if (Integer.parseInt(NumberOfDice.getText()) >= lastDice) {
                                        SetBet.setEnabled(false);
                                        Perudo.setEnabled(false);
                                        setBet(NumberOfDice.getText(), DiceValue.getText());
                                        changeTurn();
                                        DiceValue.setText("");
                                        NumberOfDice.setText("");
                                    } else {
                                        //Окно ошибки, что значение должно быть больше предыдущей ставки
                                        JDialog error = new JDialog();
                                        error.setTitle("Ошибка");
                                        error.setModal(true);
                                        error.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                        JTextArea textArea = new JTextArea();
                                        textArea.setFont(new Font("TimesRoman", Font.ITALIC, 30));
                                        textArea.setText("Значение должно быть больше предыдущей ставки.");
                                        DiceValue.setText("");
                                        textArea.setEditable(false);
                                        Container errorContentPane = error.getContentPane();
                                        errorContentPane.add(textArea);
                                        error.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
                                        error.setResizable(false);
                                        error.pack();
                                        error.setVisible(true);
                                    }
                                }
                            }
                        }
                    } else {
                        SetBet.setEnabled(false);
                        Perudo.setEnabled(false);
                        setBet(NumberOfDice.getText(), DiceValue.getText());
                        changeTurn();
                        DiceValue.setText("");
                        NumberOfDice.setText("");
                    }

                } else {
                    JDialog error = new JDialog();
                    error.setTitle("Ошибка");
                    error.setModal(true);
                    error.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    JTextArea textArea = new JTextArea();
                    textArea.setFont(new Font("TimesRoman", Font.ITALIC, 30));
                    textArea.setText("Количество кубиков должно быть > 0");
                    NumberOfDice.setText("");
                    textArea.setEditable(false);
                    Container errorContentPane = error.getContentPane();
                    errorContentPane.add(textArea);
                    error.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
                    error.setResizable(false);
                    error.pack();
                    error.setVisible(true);

                }
            } else {
                JDialog error = new JDialog();
                error.setTitle("Ошибка");
                error.setModal(true);
                error.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                JTextArea textArea = new JTextArea();
                textArea.setFont(new Font("TimesRoman", Font.ITALIC, 30));
                textArea.setText("Введён неверный номинал кубика");
                DiceValue.setText("");
                textArea.setEditable(false);
                Container errorContentPane = error.getContentPane();
                errorContentPane.add(textArea);
                error.setBounds(width - width / 2 - 100, height - height / 2 + 90, width, height);
                error.setResizable(false);
                error.pack();
                error.setVisible(true);
            }

            //TODO дубликат refresh
            Vector<String[]> history = getHistory();
            String text = "";
            if (!history.get(0)[0].equals("")) {
                for (int i = 0; i < history.size(); i++) {
                    String[] s = history.get(i);
                    if (s[1].equals("-1") && s[2].equals("-1")) {
                        text += "Игрок " + s[3] + " оспорил ставку" + "\n" + "Начался новый раунд\n";

                    } else {
                        text += "Игрок " + s[3] + " делает ставку: " + s[2] + " количеством " + s[1] + "\n";
                    }
                }
            } else {
                text = "";
            }
            HistoryArea.setText(text);

            String[] turn2 = getTurn();
            String color2 = "";
            if (turn2[0].equals(user.getUuid())) {
                color2 = "Ваш ход";
                Bets.setEnabled(true);
                SetBet.setEnabled(true);
                DiceValue.setEditable(true);
                NumberOfDice.setEditable(true);
                Perudo.setEnabled(true);
                Bets.revalidate();


            } else {
                color2 = "Ходит " + turn2[1];
                Bets.setEnabled(false);
                SetBet.setEnabled(false);
                DiceValue.setEditable(false);
                NumberOfDice.setEditable(false);
                Perudo.setEnabled(false);
                Bets.revalidate();
            }
            StatusArea.setText(color2);
            String playersColorsAndDices2 = getPlayersColorsAndDices();
            Vector<String[]> vectorColorsDices2 = new Vector<String[]>();
            String[] s_playersColorsDices2 = playersColorsAndDices2.split("\\%");
            for (int i = 0; i < s_playersColorsDices2.length; i++) {
                vectorColorsDices2.add(s_playersColorsDices2[i].split("\\$"));
            }
            String text_for_CurrentDice2 = "";
            for (int i = 0; i < vectorColorsDices2.size(); i++) {
                text_for_CurrentDice2 += vectorColorsDices2.get(i)[0] + " - " + vectorColorsDices2.get(i)[1] + "\n";
            }
            CurrentDice.setText(text_for_CurrentDice2);
            int myResult2 = getMyResult();
            String myResultString2 = String.valueOf(myResult2);
            char[] myResultStringArray2 = myResultString2.toCharArray();
            JTextArea[] dicesAreas2 = {FirstDice, SecondDice, ThirdDice, FourthDice, FifthDice};
            for (int i = 0; i < dicesAreas2.length; i++) {
                dicesAreas2[i].setText("");
            }
            for (int i = 0; i < myResultString2.length(); i++) {
                dicesAreas2[i].setText(String.valueOf(myResultStringArray2[i]));
            }
            for (int i = 0; i < vectorColorsDices2.size(); i++) {
                if (user.getColor().equals(vectorColorsDices2.get(i)[0]) && vectorColorsDices2.get(i)[1].equals("0")) {
                    for (int j = 0; j < dicesAreas2.length; j++) {
                        dicesAreas2[j].setText("");
                    }
                    StatusArea.setText("Вы проиграли");
                    Bets.setEnabled(false);
                    SetBet.setEnabled(false);
                    DiceValue.setEditable(false);
                    NumberOfDice.setEditable(false);
                    Perudo.setEnabled(false);
                    Bets.revalidate();

                }
            }
            if (isGameOver()) {
                for (int i = 0; i < vectorColorsDices2.size(); i++) {
                    if (user.getColor().equals(vectorColorsDices2.get(i)[0]) && !vectorColorsDices2.get(i)[1].equals("0")) {
                        for (int j = 0; j < dicesAreas2.length; j++) {
                            dicesAreas2[j].setText("");
                        }
                        StatusArea.setText("Вы выиграли");
                        Bets.setEnabled(false);
                        SetBet.setEnabled(false);
                        DiceValue.setEditable(false);
                        NumberOfDice.setEditable(false);
                        Perudo.setEnabled(false);
                        Bets.revalidate();

                    }
                }
            }


        });


        jFrame.revalidate();
        jFrame.add(TheTable);
        jFrame.requestFocus();
        jFrame.pack();

    }

    public void enterLobby() {
        try {
            out.writeObject("enterLobby:");
            UUID uuid = UUID.randomUUID();
            out.writeObject(String.valueOf(uuid));
            String s = null;
            s = String.valueOf(in.readObject());
            System.out.println(s);
            user = new User(String.valueOf(uuid));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void leaveLobby() {
        try {
            out.writeObject("leaveLobby:");
            out.writeObject(user.getUuid());
            String s = String.valueOf(in.readObject());
            System.out.println(s);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void readyToGame() {
        try {
            out.writeObject("readytogame:");
            out.writeObject(String.valueOf(user.getUuid()));
            String s = String.valueOf(in.readObject());
            System.out.println(s);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void notReadyToGame() {
        try {
            out.writeObject("notreadytogame:");
            out.writeObject(user.getUuid());
            String s = String.valueOf(in.readObject());
            System.out.println(s);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getLobbyNumber() {
        try {
            out.writeObject("getlobbynumber:");
            int number = Integer.parseInt(String.valueOf(in.readObject()));
            System.out.println(number);
            return number;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getReadyNumber() {
        try {
            out.writeObject("getreadynumber:");
            int number = Integer.parseInt(String.valueOf(in.readObject()));
            System.out.println(number);
            return number;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean isInLobby() {
        try {
            out.writeObject("isinlobby:");
            out.writeObject(user.getUuid());
            String s = String.valueOf(in.readObject());
            System.out.println(s);
            //System.out.println(isin);
            return Boolean.parseBoolean(s);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getGameId() {
        try {
            out.writeObject("getgameid:");
            out.writeObject(user.getUuid());
            String gameId = String.valueOf(in.readObject());
            System.out.println(gameId);
            return gameId;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void imOutOfGame() {
        try {
            out.writeObject("imoutofgame:");
            out.writeObject(user.getUuid() + "$" + user.getGameId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToDeleteGame() {
        try {
            out.writeObject("trytodeletegame:");
            out.writeObject(user.getUuid() + "$" + user.getGameId());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isInGame() {
        try {
            out.writeObject("isingame:");
            out.writeObject(user.getUuid());
            return Boolean.parseBoolean(String.valueOf(in.readObject()));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void createGame() {
        try {
            out.writeObject("createGame:");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vector<String[]> getHistory() {
        try {
            out.writeObject("gethistory:");
            out.writeObject(user.getGameId());
            String inp = String.valueOf(in.readObject());
            Vector<String[]> ss = new Vector<String[]>();
            if (!inp.equals("")) {
                String[] s = inp.split("\\%");
                for (int i = 0; i < s.length; i++) {
                    ss.add(s[i].split("\\$"));
                }
                return ss;
            } else {
                String[] s = {""};
                ss.add(s);
                return ss;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getTurn() {
        try {
            out.writeObject("getTurn:");
            out.writeObject(user.getGameId());
            return String.valueOf(in.readObject()).split("\\$");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void changeTurn() {
        try {
            out.writeObject("changeTurn:");
            out.writeObject(user.getGameId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMyColor() {
        try {
            out.writeObject("getmycolor:");
            out.writeObject(user.getUuid() + "$" + user.getGameId());
            return String.valueOf(in.readObject());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPlayersColorsAndDices() {
        try {
            out.writeObject("getplayerscolorsanddices:");
            out.writeObject(user.getGameId());
            return String.valueOf(in.readObject());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getMyResult() {
        try {
            out.writeObject("getmyresult:");
            out.writeObject(user.getUuid() + "$" + user.getGameId());
            return Integer.parseInt(String.valueOf(in.readObject()));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setBet(String dice, String value) {
        try {
            out.writeObject("setbet:");
            out.writeObject(user.getUuid() + "$" + user.getGameId() + "$" + dice + "$" + value);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getLastDiceAndValue() {
        String[] a = {};
        try {
            out.writeObject("getlastdiceandvalue:");
            out.writeObject(user.getGameId());
            String[] s = String.valueOf(in.readObject()).split("\\$");
            return s;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return a;
        }

    }

    public void perudo() {
        try {
            out.writeObject("perudo:");
            out.writeObject(user.getUuid() + "$" + user.getGameId());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isGameOver() {
        try {
            out.writeObject("isgameover:");
            out.writeObject(user.getGameId());
            return Boolean.parseBoolean(String.valueOf(in.readObject()));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


}
