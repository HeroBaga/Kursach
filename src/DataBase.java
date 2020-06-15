import java.sql.*;
import java.util.*;

//фКласс для работы с SQLite
public class DataBase {
    Connection dbConnection;

    DataBase(){
        String connectionString = "jdbc:sqlite:./src/DB.db";

        try {
            dbConnection = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    // Запись игрока в лобби
    public void enterLobby(String playerId){
        String insert = "Insert into Lobby values (?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(insert);
           prST.setString(1, playerId);

            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Удаление игрока из лобби
    public void leaveLobby(String playerId){
        String insert = "Delete from Lobby where player_id = (?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(insert);
            prST.setString(1, playerId);

            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получение кол-ва игроков в лобби
    public int numberLobby() throws SQLException {
        ResultSet resSet = null;

        String select = "SELECT count(*) FROM Lobby";

        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert resSet != null;
        return Integer.parseInt(resSet.getString(1));
    }

    // Проверка на существование игрока в лобби
    public boolean isinLobby(String playerId) throws SQLException {
        ResultSet resSet = null;

        String select = "SELECT count(*) FROM Lobby where player_id = (?) ";

        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, playerId);

            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert resSet != null;
        int size = Integer.parseInt(resSet.getString(1));
        return size > 0;
    }

    // Добавление игрока в статус готовых
    public void addReady(String playerId){
        String insert = "Insert into Ready values (?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(insert);
            prST.setString(1, playerId);

            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Удаление игрока из готовых
    public void notReady(String playerId){
        String insert = "Delete from Ready where player_id = (?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(insert);
            prST.setString(1, playerId);

            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Запрос кол-ва готовых игроков
    public int numberReady() throws SQLException {
        ResultSet resSet = null;

        String select = "SELECT count(*) FROM Ready";

        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert resSet != null;
        return Integer.parseInt(resSet.getString(1));
    }

    // Проверка на существование такого готового игрока
    public boolean isinReady(String playerId) throws SQLException {
        ResultSet resSet = null;

        String select = "SELECT count(*) FROM Ready where player_id = (?) ";

        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, playerId);

            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert resSet != null;
        int size = Integer.parseInt(resSet.getString(1));
        return size > 0;
    }

    // Очистка лобби
    public void clearLobby(){
        String insert = "Delete from Lobby";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(insert);
            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Очистка готовых
    public void clearReady(){
        String insert = "Delete from Ready";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(insert);

            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Функция бросания N кубиков
    public int roll(int Ndice){
        int dice = 0;
        for (int i = 0; i <Ndice; i++){
            dice = (int) (dice + ( (int)(Math.random()*6) +1)*Math.pow(10, i));
        }
        return dice;
    }

    // Создание игры
    public void createGame(){
        ResultSet resSet;
        String select = "SELECT Count(player_id) FROM Ready";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            resSet = prST.executeQuery();
            int c = resSet.getInt(1);
            if (c>5) {c=5;}

            select = "Select player_id from Ready";
            prST = getDbConnection().prepareStatement(select);
            resSet = prST.executeQuery();

            String insert = "Insert into Games values (?, ?, ?, ?, ?, ?, ?)";
            String gameId = String.valueOf(UUID.randomUUID());
            int i=0;
            String[] colors = {"Красный", "Зеленый", "Синий", "Желтый", "Фиолетовый"};
            while (resSet.next()){
                String pid = resSet.getString(1);
                prST = getDbConnection().prepareStatement(insert);
                prST.setString(1, pid);
                prST.setString(2, gameId);
                //Изменить колличество кубиков
                prST.setInt(3,5); //здесь

                prST.setInt(4, roll(5   )); // и здесь
                prST.setString(5, colors[i]);
                if (colors[i].equals("Красный")){
                    prST.setInt(6, 1);
                }else{
                    prST.setInt(6, 0);
                }
                prST.setInt(7, 1);

                prST.executeUpdate();

                notReady(pid);
                leaveLobby(pid);
                i++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Проверка на существование игрока в игре
    public boolean playerInGame(String playerId) throws SQLException {
        ResultSet resSet = null;

        String select = "SELECT Count(*) FROM Games where player_id=(?)";

        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, playerId);

            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        assert resSet != null;
        int size = Integer.parseInt(resSet.getString(1));
        return size > 0;
    }

    // Запрос на ID игры в которой находится игрок
    public String getGameId(String playerId) throws SQLException {
        ResultSet resSet = null;

        String select = "SELECT game_id FROM Games where player_id=(?)";

        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, playerId);

            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Vector<String> s = new Vector<>();

        assert resSet != null;
        while (resSet.next()) {
            s.add(resSet.getString(1));
        }
        return s.get(0);
    }

    // Запрос кол-ва игроков в игре
    public int getGameNumber(String gameId) throws SQLException {
        ResultSet resSet = null;

        String select = "SELECT count(*) FROM Games where game_id=(?)";

        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, gameId);
            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert resSet != null;
        return Integer.parseInt(resSet.getString(1));
    }

    // Запрос истории ставок
    public String getHistory(String gameId) throws SQLException {
        ResultSet resSet = null;

        String select = "SELECT player_id, dices, value, color  FROM Game_History where game_id=(?)";

        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, gameId);

            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String s = "";

        assert resSet != null;
        while (resSet.next()) {
            s += resSet.getString(1) + "$"
                    + resSet.getString(2)+ "$"
                    + resSet.getString(3)+ "$"
                    + resSet.getString(4)+ "%";
        }
        try{
            return s.substring(0, s.length()-1);
        } catch (StringIndexOutOfBoundsException e){
            e.printStackTrace();
            return "";
        }

    }

    // Запрос на то, какой игрок ходит
    public String getTurn(String gameId) throws SQLException {
        ResultSet resSet = null;
        String select = "Select player_id, color from Games where game_id=(?) and ishisturn=1";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, gameId);

            resSet = prST.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String s = "";

        assert resSet != null;
        while (resSet.next()) {
            s += resSet.getString(1) + "$"
                    + resSet.getString(2);
        }
        return s;
    }

    // Смена хода
    public void changeTurn(String gameId) throws SQLException {
        ResultSet resSet = null;
        ResultSet resSet2 = null;
        String select = "Select player_id, color from Games where game_id=(?) and ishisturn=1";
        String select2 = "Select color, dices from Games where game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, gameId);
            resSet = prST.executeQuery();

            prST = getDbConnection().prepareStatement(select2);
            prST.setString(1, gameId);
            resSet2 = prST.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        int number = getGameNumber(gameId);
        String[] s = {resSet.getString(1), resSet.getString(2)};
        int next_color_index=0;

        String[] colors = {"Красный", "Зеленый", "Синий", "Желтый", "Фиолетовый"};
        String[] colors2 = new String[number];
        List<String> newcolors = new ArrayList<String>(Arrays.asList(colors));
        while (resSet2.next()){
            if (resSet2.getString(2).equals("0")){
                String colortoremove = resSet2.getString(1);
                newcolors.remove(colortoremove);
                number--;
            }
        }
        colors = newcolors.toArray(new String[0]);
        for (int i = 0; i < number; i++){
            colors2[i] = colors[i];
            if (s[1].equals(colors[i])){ next_color_index=i;}
        }

        if (next_color_index+1>=number){
            next_color_index = 0;
        } else{
            next_color_index++;
        }


        String update = "Update Games set ishisturn=1 where game_id=(?) and color = (?)";
        String update2 = "Update Games set ishisturn=0 where game_id=(?) and color = (?)";
        prST = getDbConnection().prepareStatement(update);
        prST.setString(1, gameId);
        prST.setString(2, colors2[next_color_index]);
        prST.executeUpdate();

        prST = getDbConnection().prepareStatement(update2);
        prST.setString(1, gameId);
        prST.setString(2, s[1]);
        prST.executeUpdate();
    }

    // Запрос цвета игрока
    public String getMyColor(String playerId, String gameId) throws SQLException {
        ResultSet resSet = null;
        String select = "Select color from Games where player_id =(?) and game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, playerId);
            prST.setString(2, gameId);

            resSet = prST.executeQuery();
            return resSet.getString(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Запрос цветов и кол-ва оставшихся кубиков игроков
    public String getPlayersColorsAndDices(String gameId){
        ResultSet resSet = null;
        String select = "Select color, dices from Games where game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, gameId);

            resSet = prST.executeQuery();
            String s = "";

            while (resSet.next()){
                s += resSet.getString(1) + "$" + resSet.getString(2)+"%";
            }
            return s.substring(0, s.length()-1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получение выпавшей комбинации игрока
    public int getMyResult(String playerId, String gameId){
        ResultSet resSet = null;
        String select = "Select result from Games where player_id = (?) and game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, playerId);

            prST.setString(2, gameId);

            resSet = prST.executeQuery();
            return resSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Создание ставки
    public void setBet(String playerId, String gameId, String dice, String value){
        String insert = "Insert into Game_History values (?,?,?,?,?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(insert);
            prST.setString(1, gameId);
            prST.setString(2, playerId);
            prST.setInt(3, Integer.parseInt(dice));
            prST.setInt(4, Integer.parseInt(value));
            prST.setString(5, getMyColor(playerId, gameId));
            prST.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    // Запрос последней ставки из истории
    public Object[] getLastDiceAndValue(String gameId){
        ResultSet resSet = null;
        String select = "Select player_id, dices, value from Game_History where game_id=(?)";
        PreparedStatement prST;
        Object[] res = {"",0,0};
        try {
            prST = getDbConnection().prepareStatement(select);

            prST.setString(1, gameId);

            resSet = prST.executeQuery();
            while (resSet.next()){
                res[0] = resSet.getString(1);
                res[1] = resSet.getString(2);
                res[2] = resSet.getString(3);
            }
            return res;

        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }

    // Получение всех комбинаций на столе
    public HashMap<Integer, Integer> getAllTable(String gameId){
        ResultSet resSet = null;
        String select = "Select result from Games where game_id=(?)";
        PreparedStatement prST;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 1; i <7; i++){
            map.put(i, 0);
        }
        try {
            prST = getDbConnection().prepareStatement(select);

            prST.setString(1, gameId);

            resSet = prST.executeQuery();
            while (resSet.next()){
                int number = resSet.getInt(1);
                char[] numberArr = String.valueOf(number).toCharArray();
                for (char c : numberArr) {
                    number = Integer.parseInt(String.valueOf(c));
                    if (number >= 1 && number <= 6) {
                        int n = map.get(number);
                        map.remove(number);
                        map.put(number, n + 1);
                    }
                    System.out.println(map);
                }
            }
            for (int i = 2; i <7; i++){
                int number = map.get(i);
                map.remove(i);
                map.put(i, number+map.get(1));
            }
            return map;

        } catch (SQLException e) {
            e.printStackTrace();
            return map;
        }
    }

    // Смена кол-ва кубиков
    public void updateDices(String playerId, String gameId){
        ResultSet resSet = null;

        String update = "Update Games set dices=(?) where player_id = (?) and game_id=(?)";
        String select = "Select dices from Games where player_id = (?) and game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, playerId);
            prST.setString(2, gameId);
            resSet = prST.executeQuery();
            prST = getDbConnection().prepareStatement(update);
            prST.setInt(1, resSet.getInt(1)-1);
            prST.setString(2, playerId);
            prST.setString(3,gameId);
            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Смена выпавших комбинаций
    public void updateResults(String gameId){
        ResultSet resSet = null;

        String update = "Update Games set result=(?) where player_id = (?) and game_id=(?)";
        String select = "Select player_id, dices from Games where game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, gameId);
            resSet = prST.executeQuery();
            while (resSet.next()) {
                prST = getDbConnection().prepareStatement(update);
                int dices = resSet.getInt(2);
                if (dices>0){
                    int dice_rolled = roll(dices);
                    prST.setInt(1, dice_rolled);
                    prST.setString(2, resSet.getString(1));
                    prST.setString(3, gameId);
                    prST.executeUpdate();
                }else{
                    prST.setInt(1, 0);
                    prST.setString(2, resSet.getString(1));
                    prST.setString(3, gameId);
                    prST.executeUpdate();
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Смена хода после нажатой кнопки Блеф
    public void changeTurnWithPerudo(String playerId, String gameId){
        ResultSet resSet = null;
        String update = "Update Games set ishisturn=0 where game_id = (?)";
        String update2 = "Update Games set ishisturn=1 where player_id=(?) and game_id=(?)";
        String select = "Select dices from Games where player_id=(?) and game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, playerId);
            prST.setString(2, gameId);
            resSet = prST.executeQuery();

            if (resSet.getInt(1)==0){
                changeTurn(gameId);
            } else {

            prST = getDbConnection().prepareStatement(update);
            prST.setString(1, gameId);
            prST.executeUpdate();

            prST = getDbConnection().prepareStatement(update2);
            prST.setString(1, playerId);
            prST.setString(2, gameId);
            prST.executeUpdate();}

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Проверка на то, кончилась ли игра
    public boolean isGameOver(String gameId) throws SQLException {
        ResultSet resSet = null;
        String select = "Select count(*) from Games where game_id=(?) and dices>0";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, gameId);
            resSet = prST.executeQuery();


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resSet.getInt(1)==1;

    }

    // Игрок выходит из игры
    public void imOutOfGame(String playerId, String gameId){
        String update = "Update Games set dices=0, isingame=0 where player_id=(?) and game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(update);
            prST.setString(1, playerId);
            prST.setString(2, gameId);
            prST.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Попытка удаления игры
    public void tryToDeleteGame(String playerId, String gameId){
        ResultSet resSet = null;
        String select = "Select count(*) from Games where game_id=(?) and isingame=1";
        String update = "Delete from Games where game_id=(?)";
        PreparedStatement prST;
        try {
            prST = getDbConnection().prepareStatement(select);
            prST.setString(1, gameId);
            resSet = prST.executeQuery();
            if (resSet.getInt(1)==0){
                prST = getDbConnection().prepareStatement(update);
                prST.setString(1, gameId);
                prST.executeUpdate();
                clearHistory();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Очистка игр
    public void clearGame(){
        String insert = "Delete from Games";
        PreparedStatement prST = null;
        try {
            prST = getDbConnection().prepareStatement(insert);
            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Очистка истории
    public void clearHistory(){
        String insert = "Delete from Game_History";
        PreparedStatement prST = null;
        try {
            prST = getDbConnection().prepareStatement(insert);
            prST.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
