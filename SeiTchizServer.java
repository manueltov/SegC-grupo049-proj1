import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SeiTchizServer {

	public static void main(String[] args) {
		
		int porto = 45678;
		
		if(args.length>0) {
			porto=Integer.parseInt(args[0]);			
		}
		System.out.println("Coneção feita ao porto "+porto);
		SeiTchizServer servidor = new SeiTchizServer();
		
		servidor.startServer(porto);
	}

	private void startServer(int porto) {
		// TODO Auto-generated method stub
		ServerSocket serverSocket = null;
		try{
			serverSocket = new ServerSocket(porto);
			System.out.println("Servidor criado");
		}catch (IOException e) {
			System.out.println("Não foi possivel criar a conecçao com o socket");
			System.exit(-1);
		}
		while(true) {
			Socket soc = null;
			try {
				soc = serverSocket.accept();
				ServerThread newServerThread = new ServerThread(soc);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	class ServerThread extends Thread {
		private Socket socket = null;
		
		public ServerThread(Socket soc) {
			socket = soc;
			System.out.println("Coneção feita com o Cliente SeiTchiz");
			
		}
		public void run(){
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				
				String address = null;
				String user = null;
				String password = null;
				
				try {
					address = (String) inStream.readObject();
					user = (String) inStream.readObject();
					password = (String) inStream.readObject();
					System.out.println("thread: Informações recebida do SeiTchiz");
				}catch(ClassNotFoundException e1) {
					e1.printStackTrace();
				}
				if(user.length()!=0) {
					outStream.writeObject(new Boolean(true));
					System.out.println("Utilizador " + user + " entrou.");
					
				}else {
					outStream.writeObject(new Boolean(false));
					System.out.println("Coneção falhada com o SeiTchiz");
					outStream.close();
					inStream.close();
					socket.close();
					return;
				}
				outStream.close();
				inStream.close();
				socket.close();
				return;
				
			}catch (IOException e) {
				e.printStackTrace();
			}	
			
			
		}
		
	}

}
