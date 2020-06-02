import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public static void main(String[] args) {
        new Server();
    }

    private Vector<TCPConnection> connections= new Vector<>();
    private HashMap<TCPConnection,TCPConnection> helpConnections = new HashMap<>();
    Server(){
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8189);
            System.out.println("BLYA");

            while(true) {
                clientSocket = serverSocket.accept();
                TCPConnection client = new TCPConnection(this, clientSocket);
                connections.add(client);
                new Thread(client).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Сервер остановлен");
                serverSocket.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void CreateConnection(TCPConnection tcpConnection,int Id){// создания соединения с клиентом
        for (int i = 0;i<connections.size();i++) {
            if (connections.get(i).getId() == Id){
                helpConnections.put(tcpConnection, connections.get(i));
                tcpConnection.SendHistory(connections.get(i).getA());
                return;
            }
        }
    }


    public synchronized void Disconnect(TCPConnection tcpConnection){// действия сервера при отключения пользователя
        SendMsg("str:","клиент вышел",tcpConnection);
        connections.remove(tcpConnection);
        helpConnections.remove(tcpConnection);
    }

    public synchronized void AdmDisconnect(TCPConnection tcpConnection){
        helpConnections.remove(tcpConnection);
    }

    public synchronized void updateList(){
        String help = new String();
        help="";
        for(TCPConnection tcpConnection: connections){
            if(tcpConnection.getId()!=0) help+=tcpConnection.getId()+"\n";
        }
        for(TCPConnection tcpConnection: connections){
            if(tcpConnection.getTransport())continue;
            if(tcpConnection.getId()==0){
                System.out.println("Help:    "+help);
                System.out.println("отправлено "+ tcpConnection);
                tcpConnection.SendObj("Update",help);
            }
        }
    }//обновление списка для админа


    public synchronized void createDirectory(String id){
        new File("src/com/company/assets/server/"+id+"").mkdir();
    }


    public synchronized ArrayList<byte[]> getImages(String str){

        int i=0;
        File file = new File("src/com/company/assets/server/"+str+"");
        ArrayList<byte[]> arrayImage = new ArrayList<>();
        if (file.isDirectory()) {
            for (File help:file.listFiles()){
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(ImageIO.read(help), "png", baos);
                    baos.flush();
                    byte[] bytes = baos.toByteArray();
                    arrayImage.add (bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return arrayImage;
    }

    public synchronized void deleteTourPicture(String id){
        File file = new File("src/com/company/assets/server/"+id+"");
        File[] a = file.listFiles();
        for(File help:a)
            help.delete();
        file.delete();
    }

    public synchronized void addPictureTour(String id ,ArrayList<byte[]> arrayList){
        for(byte[] obj:arrayList)
        {
            ByteArrayInputStream in = new ByteArrayInputStream(obj);
            try {
                ImageIO.write(ImageIO.read(in),"png",new File("src/com/company/assets/server/"+id+"/"+ Math.random()*100000000+".png" +""));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public synchronized void SendMsg(String _protocol , Object object, TCPConnection tcpConnection) {

        for (Map.Entry<TCPConnection, TCPConnection> entry : helpConnections.entrySet()){
            if (entry.getKey().equals(tcpConnection)) entry.getValue().SendObj(_protocol, object);

            if(entry.getValue().equals(tcpConnection))  entry.getKey().SendObj(_protocol, object);
        }
    }
}
