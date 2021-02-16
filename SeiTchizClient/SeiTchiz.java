import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class SeiTchiz {

	public static void main(String[] args) {
		 System.out.println("cliente SeiTchiz inicio");
		if (args.length < 2) {
			System.out.println("Passe os dados desta maneira: <serverAddress> <clientID> [password]");
			return;
		}
		
		// Args
		//String[] serverAddress = args[0].split(".");
		//String ip = serverAddress[0];
		//int port = Integer.parseInt(serverAddress[1]);
		String user = args[1];
		String ip = args[0];
		String password = "";
		
		if (args.length == 2) {
			System.out.println("Introduza a sua password:");
			password = (new Scanner(System.in)).nextLine();
		} else
			password = args[2];
		
		int port = 45678;
		
		SeiTchiz cliente = new SeiTchiz();
		cliente.startClient(ip,port);
		
		
		
		
	}

	private void startClient(String ip, int port) {
		Socket socketCliente = null;
		try {
			socketCliente = new Socket(ip, port);
			ObjectInputStream in = new ObjectInputStream(socketCliente.getInputStream());
	        ObjectOutputStream out = new ObjectOutputStream(socketCliente.getOutputStream());
	        System.out.println("Introduz o user e a passwd");

            //buscar do user as credenciais
            System.out.println("User: ");
            Scanner scanner = new Scanner(System.in);
        	String password = scanner.nextLine();
        	System.out.println("Introduza a sua pass:");
        	String localUserID = scanner.nextLine();

            out.writeObject(password);
            out.writeObject(localUserID);
            
	        
		}catch(IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
			
		}
		
        
	}

}
