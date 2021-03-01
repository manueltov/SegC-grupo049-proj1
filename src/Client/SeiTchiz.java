package Client;



import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SeiTchiz {
    private Socket socketClient = null;
    private static int PORT = 45678;
    public static void main(String[] args) {
    	createFolder();
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
            System.err.println("SÃ³ se liga ao porto 45678");
            System.exit(-1);
        }
        String username = args[1];
        String password = args[2];
        SeiTchiz client = new SeiTchiz();
        client.startClient(ip, port, username, password);
    }

    private static void createFolder() {
    	try {
            Path path = Paths.get("../imagensParaEnvio/");
            //java.nio.file.Files;
            Files.createDirectories(path);
        } 
        catch (IOException e) {
            System.err.println("Erro: pasta de fotografias para envio não criada" + e.getMessage());
        }		
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
