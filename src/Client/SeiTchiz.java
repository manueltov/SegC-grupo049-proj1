package Client;



import java.io.*;
import java.net.Socket;

public class SeiTchiz {
    private Socket socketClient = null;
    private static int PORT = 45678;
    public static void main(String[] args) {
        System.out.println("cliente SeiTchiz inicio");
        if (args.length < 3) {
            //127.0.0.1:45678
            System.err.println("Passe os dados desta maneira: <serverAddress> <username> <password>");
            System.exit(0);
        }
        // Args
        String[] serverAddress = args[0].split(":");
        String ip = serverAddress[0];
        int port = Integer.parseInt(serverAddress[1]);
        System.out.println("---------" + ip + ":" + port + "---------");
        if(port != PORT) {
            System.err.println("Só se liga ao porto 45678");
            System.exit(-1);
        }
        String username = args[1];
        String password = args[2];
        SeiTchiz client = new SeiTchiz();
        client.startClient(ip, port, username, password);
    }

    private void startClient(String ip, int port, String user, String password) {
        try {
            socketClient = new Socket(ip, port);

            ObjectOutputStream outStream = new ObjectOutputStream(socketClient.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(socketClient.getInputStream());
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

            //envia credenciais e verifica login
            outStream.writeObject(user);
            outStream.writeObject(password);
            String response = inStream.readObject().toString();
            if (response.equals("false")) {
                System.out.println("Desconectado!");
                return;
            }
            else {
                System.out.println("Conectado!");
                System.out.println("*********************************");
                System.out.println("======= MENU DE COMANDOS ========");
                System.out.println("* follow <userID> | f <userID>");
                System.out.println("* unfollow <userID> | u <userID>");
                System.out.println("* viewfollowers | v");
                System.out.println("* post <photo> | p <photo>");
                System.out.println("* wall <nPhotos> | w <nPhotos>");
                System.out.println("* like <photoID> | l <photoID>");
                System.out.println("* newgroup <groupID> | n <groupID>");
                System.out.println("* addu <userID> <groupID> | a <userID> <groupID>");
                System.out.println("* removeu <userID> <groupID> | r <userID> <groupID>");
                System.out.println("* ginfo [groupID] | g [groupID]");
                System.out.println("* msg <groupID> <msg> | m <groupID> <msg>");
                System.out.println("* collect <groupID> | c <groupID>");
                System.out.println("* history <groupID> | h <groupID>");
                System.out.println("===================================");
                String str="";
                String str2="";
                while(!str.equals("stop")){
                    str=br.readLine();
                    outStream.writeObject(str);
                    outStream.flush();
                    str2= inStream.readObject().toString();
                    System.out.println(str2);
                }
            }
            outStream.close();
            inStream.close();
            socketClient.close();
        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
