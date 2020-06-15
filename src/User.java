public class User {
    String uuid; // уникальный идентификатор игрока
    String gameId; // уникальный идентификатор игры в которой участвует игрок
    String color; // цвет игрока

    User(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setgameId(String gameId) {this.gameId = gameId;}

    public String getGameId() {return gameId;}

    public void setColor(String color) {this.color = color;}

    public String getColor() {return color;}


}
