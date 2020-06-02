
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

public class TCPConnection implements Runnable {

    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private ArrayList<String> a = new ArrayList<>();
    private String protocol;
    private int Id=0;
    private boolean transport=false;
    private boolean isWork = true;

    TCPConnection(Server server, Socket socket){
        this.server=server;
        this.socket=socket;
        try {
            out= new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void SendObj(String _protocol,Object obj){
        try {
            out.writeObject(_protocol);
            out.writeObject(obj);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public synchronized void SendHistory(ArrayList<String> history){

        try {
            out.writeObject("str:");
            out.writeObject("History");
            for(String i: history ) {
                out.writeObject("str:");
                out.writeObject(i);
            }
            out.writeObject("str:");
            out.writeObject("------------------------------\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public boolean getTransport(){return transport;}

    public int getId (){return this.Id;}

    public ArrayList<String> getA(){return a;}

    @Override
    public void run() {

        try {
            while (isWork) {
                try {
                    protocol = String.valueOf(in.readObject());
                    System.out.println("Пришло указание "+ protocol);
                    switch (protocol) {
                        case ("str:") :{
                            protocol = String.valueOf(in.readObject());
                            a.add(protocol);
                            server.SendMsg("str:", protocol, this);
                            break;
                        }

                        case ("IDs:") :{
                            this.Id = Integer.parseInt(String.valueOf(in.readObject()));
                            server.updateList();
                            break;
                        }
                        case ("Conn") : {
                            server.CreateConnection(TCPConnection.this, Integer.parseInt(String.valueOf(in.readObject())));
                            break;
                        }
                        case ("disconnect") :{
                            isWork = false;
                            SendObj("gf", null);// простая строка
                            server.SendMsg("disconnect", "Пользователь покинул чат", this);
                            break;
                        }
                        case ("AdmDisconnect") :{// при закрытие диалога админом
                            server.AdmDisconnect(this);
                            break;
                        }
                        case ("AdminClose") :{// при закрытие программы админом
                            isWork = false;
                            SendObj("gf", null);
                            server.AdmDisconnect(this);
                            break;
                        }
                        case ("History") :{
                            //if (protocol.equals("History")) {
                            a.add(String.valueOf(in.readObject()));
                            break;
                        }
                        case ("SendImage"):{

                            protocol=String.valueOf(in.readObject());
                            out.writeObject(server.getImages(protocol));
                            out.flush();
                            break;
                        }

                        case("UpdateTourPicture"):{
                            protocol =String.valueOf(in.readObject());
                            server.addPictureTour(protocol,(ArrayList<byte[]>)in.readObject());
                            break;
                        }

                        case("DeleteTourPicture"):{
                            server.deleteTourPicture(String.valueOf(in.readObject()));
                            break;
                        }

                        case ("CreateDirectory"):{
                            server.createDirectory(String.valueOf(in.readObject()));
                            break;
                        }
                        case("transport"):{
                            transport=true;
                            break;
                        }

                        case("Close"):{
                            SendObj("gf", null);
                            isWork=false;
                            break;}

                        default:break;
                    }
                } catch (IOException e) {
                    break;
                } catch (ClassNotFoundException e) {
                    break;
                }
            }
        }finally {
            try {
                in.close();
                out.close();
                server.Disconnect(this);
                server.updateList();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}
