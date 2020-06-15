import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class TCPConnection implements Runnable {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ArrayList<String> a = new ArrayList<>();
    private int Id=0;
    private boolean isWork = true;
    private DataBase db = Singleton.getCollection().getDbConnection();

    TCPConnection(Socket socket){
        try {
            out= new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            while (isWork) {
                try {
                    String protocol = String.valueOf(in.readObject());
                    System.out.println("Пришло указание "+ protocol);
                    //Switch-case на ловлю запросов с клиента
                    switch (protocol) {
                        // Запись игрока в лобби
                        case ("enterLobby:"):{
                            String uuid = String.valueOf(in.readObject());
                            db.enterLobby(uuid);
                            out.writeObject("Ты был добавлен в лобби.");
                            break;
                        }

                        // Удаление игрока из готовых
                        case ("leaveLobby:"):{
                            String uuid = String.valueOf(in.readObject());
                            db.leaveLobby(uuid);
                            out.writeObject("Ты был удален из лобби.");
                            break;
                        }

                        // Запись игрока в готовые
                        case ("readytogame:"):{
                            String uuid = String.valueOf(in.readObject());
                            db.addReady(uuid);
                            out.writeObject("Ты сказал, что готов к игре.");
                            break;
                        }

                        // Удаление игрока из готовых
                        case ("notreadytogame:"):{
                            String uuid = String.valueOf(in.readObject());
                            db.notReady(uuid);
                            out.writeObject("Ты сказал, что не готов к игре.");
                            break;
                        }


                        // Получение кол-ва игроков в лобби
                        case ("getlobbynumber:"):{
                            int number = db.numberLobby();
                            out.writeObject(String.valueOf(number));
                            break;
                        }

                        // Получение кол-ва готовых игроков
                        case ("getreadynumber:"):{
                            int number = db.numberReady();
                            out.writeObject(String.valueOf(number));
                            break;
                        }


                        // Проверка на существование игрока в лобби
                        case ("isinlobby:"):{
                            String uuid = String.valueOf(in.readObject());
                            boolean isin = db.isinLobby(uuid);
                            out.writeObject(String.valueOf(isin));
                            break;
                        }

                        // Проверка на существование игрока в готовых
                        case ("isinready:"):{
                            String uuid = String.valueOf(in.readObject());
                            boolean isin = db.isinReady(uuid);
                            out.writeObject(String.valueOf(isin));
                            break;
                        }

                        // Проверка на то, находится ли игрок в игре
                        case ("isingame:"):{
                            String uuid = String.valueOf(in.readObject());
                            boolean isin = db.playerInGame(uuid);
                            out.writeObject(String.valueOf(isin));
                            break;
                        }


                        // Получение ID игры в которой находится игрок
                        case ("getgameid:"):{
                            String uuid = String.valueOf(in.readObject());
                            String gameid = db.getGameId(uuid);
                            out.writeObject(gameid);
                            break;
                        }

                        // Создание новой игры
                        case ("createGame:"):{
                            db.createGame();
                            break;
                        }

                        // Запрос истории ставок
                        case ("gethistory:"):{
                            String uuid = String.valueOf(in.readObject());
                            out.writeObject(db.getHistory(uuid));
                            break;
                        }

                        // Запрос на то, какой игрок сейчас ходит
                        case ("getTurn:"):{
                            String uuid = String.valueOf(in.readObject());
                            out.writeObject(db.getTurn(uuid));
                            break;
                        }


                        // Смена хода
                        case ("changeTurn:"):{
                            String uuid = String.valueOf(in.readObject());
                            db.changeTurn(uuid);
                            break;
                        }


                        // Запрос цвета игрока
                        case ("getmycolor:"):{
                            String uuids = String.valueOf(in.readObject());
                            String[] sub = uuids.split("\\$");
                            out.writeObject(db.getMyColor(sub[0], sub[1]));
                            break;
                        }

                        // Запрос цветов игроков и их оставшихся кубиков
                        case ("getplayerscolorsanddices:"):{
                            String uuid = String.valueOf(in.readObject());
                            out.writeObject(db.getPlayersColorsAndDices(uuid));
                            break;
                        }


                        // Получение выпавшей комбинации
                        case ("getmyresult:"):{
                            String uuids = String.valueOf(in.readObject());
                            String[] sub = uuids.split("\\$");
                            out.writeObject(String.valueOf(db.getMyResult(sub[0], sub[1])));
                            break;
                        }


                        // Создание ставки
                        case ("setbet:"):{
                            String uuids = String.valueOf(in.readObject());
                            String[] sub = uuids.split("\\$");
                            db.setBet(sub[0], sub[1], sub[2], sub[3]);
                            break;
                        }


                        // Запрос последней ставки из истории
                        case ("getlastdiceandvalue:"):{
                            String uuid = String.valueOf(in.readObject());
                            Object[] s = db.getLastDiceAndValue(uuid);
                            out.writeObject(String.valueOf(s[1])+"$"+String.valueOf(s[2]));
                            break;
                        }


                        // Была нажата кнопка Блеф
                        case ("perudo:"):{
                            String uuids = String.valueOf(in.readObject());
                            String[] sub = uuids.split("\\$");
                            Object[] lastDiceAndValue = db.getLastDiceAndValue(sub[1]);
                            boolean perudoTrue = isPerudoTrue(sub[1], Integer.parseInt(String.valueOf(lastDiceAndValue[1])), Integer.parseInt(String.valueOf(lastDiceAndValue[2])));
                            if (perudoTrue){
                                db.updateDices(String.valueOf(lastDiceAndValue[0]), sub[1]);
                            }else{
                                db.updateDices(sub[0], sub[1]);
                            }
                            db.updateResults(sub[1]);
                            db.changeTurnWithPerudo(sub[0], sub[1]);
                            break;
                        }


                        // Проверка на то, кончилась ли игра
                        case ("isgameover:"):{
                            String uuid = String.valueOf(in.readObject());
                            out.writeObject(String.valueOf(db.isGameOver(uuid)));
                            break;
                        }


                        // Игрок говорит, что он вышел из игры
                        case ("imoutofgame:"):{
                            String uuids = String.valueOf(in.readObject());
                            String[] sub = uuids.split("\\$");
                            db.imOutOfGame(sub[0], sub[1]);
                            break;
                        }


                        // Попытка удалить игру, при выходе игрока
                        // Не сработает, пока в игре есть хотя бы один игрок
                        case ("trytodeletegame:"):{
                            String uuids = String.valueOf(in.readObject());
                            String[] sub = uuids.split("\\$");
                            db.tryToDeleteGame(sub[0], sub[1]);
                            break;
                        }
                        default:break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    break;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    // Проверка на то, правда ли, что игрок блефует
    public boolean isPerudoTrue(String gameId, int dice, int value){
        HashMap<Integer, Integer> map = db.getAllTable(gameId);
        return (int) map.get(value) < dice;

    }

}
