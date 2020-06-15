public class Singleton {
    private static Singleton singleton;
    private static DataBase dbConnection= new DataBase();

    private Singleton() {
    }

    public static synchronized Singleton getCollection() {
        if (singleton == null) {
            singleton = new Singleton();
        }
        return singleton;
    }

    public DataBase getDbConnection(){return dbConnection;}

}
