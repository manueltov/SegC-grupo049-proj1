package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import SeiTchizKeys.KeysServer;
/**
 * 
 * Esta classe é responsável pela criação e manipulação do servidor "SeiTchizServer"
 *
 */
public class SeiTchizServer {
	private static int PORT = 45678;
	private static final String txt = ".txt";
	private static final String ledger = "ledger" + txt;

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Server tem que correr com o comando: 'SeiTchizServer <port> <keystore> <keystore-password>'");
			System.exit(0);
		}
		int port = Integer.parseInt(args[0]);
		if (port != PORT) {
			System.err.println("SeiTchizServer no porto " + PORT);
			System.exit(0);
		}
		
		//
		String keyStore = "src"+File.separator+"Server"+File.separator+args[1];
		String keyStorePass = args[2];
		System.out.println("Conexao feita ao porto " + port);
		// se ledgerFile ainda nao existe create one
		createLedgerFile();

		
		
		SeiTchizServer server = new SeiTchizServer();
		server.loadCertificate(keyStore,keyStorePass);
		System.out.println("KeyStore válida");
		server.startServer(port);
	}
	private KeysServer keysServer = null;
	
	private void loadCertificate(String keyStore, String keyStorePass) {
		
		System.setProperty("javax.net.ssl.keyStore", keyStore);
		System.setProperty("javax.net.ssl.keyStorePassword", keyStorePass);
		
		keysServer=new KeysServer(keyStore,keyStorePass);
		
	}
	
	private void startServer(int port) {
		
		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		ServerSocket serverSocket=null;
		try {
			serverSocket = ssf.createServerSocket(port);
			System.out.println("Servidor criado");
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		while (true) {
			try {
				
				System.out.println("Mensagem recebida");
				ServerThread newServerThread = new ServerThread(serverSocket.accept());
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void createLedgerFile() {
		File file = new File(ledger);
		if (!file.exists()) {
			try {
				file = openFile(ledger);
				BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
				writer.write("current_ID:0" + "\n");
				writer.close();
			} catch (IOException e) {
				System.out.println("Error creating or updating ledger file...");
				// e.printStackTrace();
			}
		}
	}

	private static File openFile(String str) {
		File file = new File(str);
		File folders = file.getParentFile();
		try {
			if (folders != null && !folders.exists()) {
				folders.mkdirs();
			}
			if (!file.exists() && !str.contains(txt)) {
				file.mkdir();
			}
			if (!file.exists() && str.contains(txt)) {
				file.createNewFile();
			}
		} catch (IOException e) {
			System.err.println(" Erro ao criar a pasta ou ficheiro:" + str);
		}
		return file;
	}
	/**
	* 
	* Esta classe é responsável pela criação e manipulação dos comandos utilizados pelos utilizadores do "SeiTchizServer"
	* comandos:
	* stop -> parar de receber pedidos
	* v/viewfollowers -> ver o seu numero de seguidores 
	* g/ginfo -> mostra informações sobre qual os grupos a que pertence/ é dono
	*			@param groupID - identificador do grupo
	* c/collect -> receber todas as mensagens do grupo que o utilizador ainda não tenha recebido
	*			@param groupID - identificador do grupo
	* h/history -> histórico de mensagens do grupo que o utilizador ja leu
	*			@param groupID - identificador do grupo
	* f/follow -> seguir outro utilizador
	*			@param userID - identificador do utilizador que quer seguir
	* u/unfollow -> deixar de seguir outro utilizador
	*			@param userID - identificador do utilizador que quer deixar de seguir
	* p/post -> publicar uma foto
	*			@param photo - fotografia que quer publicar
	* w/wall -> mostrar um conjunto de fotografias mais recentes dos utilizadores que seguem
	*			@param nPhotos - numero de fotografias que o utilizador deseja ver
	* l/like -> gostar de uma fotografia
	*			@param photoID - identificador da fotografia que gosta
	* n/newgroup -> criar um grupo de utilizadores
	*			@param grupoID - identificador do grupo que quer criar
	* a/addu -> adicionar um utilizador ao grupo
	*			@param userID - identificador do utilizador que quer juntar ao grupo
	*			@param groupID - identificador do grupo
	* r/removeu -> remover um utilizador do grupo 
	*			@param userID - identificador do utilizador que quer remover do grupo
	*			@param groupID - identificador do grupo				
	* m/msg -> enviar uma mensagem para um grupo a qual pertence
	*			@param groupID - identificador do grupo
	*			@param msg - conteudo da mensagem
	*/
	class ServerThread extends Thread {
		private Socket socket = null;

		public ServerThread(Socket soc) {
			socket = soc;
			System.out.println("Conexao feita com o Cliente SeiTchiz");
		}

		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				ServerActions acc = new ServerActions(inStream, outStream);
				
				acc.loadKeys(keysServer);
				
				acc.comecaAccoes();
			     
				//outStream.writeObject(login);
				//outStream.flush();

				String mesReceived = "";
				String mesSent;
			
				// enquanto o servidor nao receber uma mensagem a dizer 'stop' ele continua à
				// espera de pedidos
				while (!mesReceived.equals("stop")) {
					acc.printGroups();
					
					mesReceived = (String) inStream.readObject();
					mesReceived = mesReceived.toLowerCase();
					System.out.println(mesReceived);
					String[] split = mesReceived.split(" ");
					mesSent = "Mensagem invalida";
					if (split.length == 1) {
						String command = split[0];
						if (command.equals("v") || command.equals("viewfollowers")) {
							String viewFollowers = acc.viewFollowers();
							if (viewFollowers != null) {
								mesSent = "Seguidores:\n" + viewFollowers;
							} else {
								mesSent = "Não tem seguidores.";
							}
						}
						if (command.equals("g") || command.equals("ginfo")) {
							mesSent = "Os meus grupos:\n" + acc.listGroups();
						}
					} else if (split.length == 2) {
						String command = split[0];
						if (command.equals("f") || command.equals("follow")) {
							String userID = split[1];
							boolean follow = acc.followUser(userID);
							if (follow) {
								mesSent = "Seguiu: '" + userID + "'";
							} else {
								mesSent = "Nao foi possivel seguir: '" + userID + "'";
							}
						} else if (command.equals("u") || command.equals("unfollow")) {
							String userID = split[1];
							boolean unfollow = acc.unfollowUser(userID);
							if (unfollow) {
								mesSent = "Deixou de seguir: '" + userID + "'";
							} else {
								mesSent = "Nao foi possivel deixar de seguir: '" + userID + "'";
							}
						} else if (command.equals("p") || command.equals("post")) {
							String photoID = null;
							
							//ask for <photo>
							outStream.writeObject("<photo>");
	                        outStream.flush();
	                        
	                        //receive photo
	                        byte[] secondMesReceived = (byte[]) inStream.readObject();
                        	photoID = acc.post(secondMesReceived);
                        	
							if (photoID != null) {
								mesSent = "Fotografia com ID: " + photoID + "\n publicada com sucesso.";
							} else {
								mesSent = "nao foi possivel publicar a fotografia selecionada";
							}
						} else if (command.equals("w") || command.equals("wall")) {
							String nPhotos = split[1];
							String wall = acc.wall(nPhotos);
							if (wall != null) {
								mesSent = wall;
							} else {
								mesSent = "nao foi possivel fazer wall";
							}
						} else if (command.equals("l") || command.equals("like")) {
							String photoID = split[1];
							boolean liked = acc.like(photoID);
							if (liked) {
								mesSent = "Fotografia com ID: " + photoID + " liked.";
							} else {
								mesSent = "nao foi possivel fazer like na foto com ID: " + photoID;
							}
						} else if (command.equals("n") || command.equals("newgroup")) {
							String groupID = split[1];
							boolean created = acc.newGroup(groupID);
							if (created) {
								mesSent = "Grupo criado: '" + groupID + "'";
							} else {
								mesSent = "Grupo ja existe: '" + groupID + "'";
							}
						} else if (command.equals("g") || command.equals("ginfo")) {
							String groupID = split[1];
							mesSent = acc.listGroups(groupID);
						} else if (command.equals("c") || command.equals("collect")) {
							String groupID = split[1];
							mesSent = acc.collect(groupID);
						} else if (command.equals("h") || command.equals("history")) {
							String groupID = split[1];
							mesSent = acc.history(groupID);
						}
					} else if (split.length == 3) {
						String command = split[0];
						if (command.equals("a") || command.equals("addu")) {
							String userID = split[1];
							String groupID = split[2];
							boolean added = acc.addUser(userID, groupID);
							if (added) {
								mesSent = "User: " + userID + " adicionado ao grupo '" + groupID + "'";
							} else {
								mesSent = "Nao foi possivel adicionar o user '" + userID + "' ao grupo '" + groupID
										+ "'";
							}
						} else if (command.equals("r") || command.equals("removeu")) {
							String userID = split[1];
							String groupID = split[2];
							boolean removed = acc.removeUser(userID, groupID);
							if (removed) {
								mesSent = "User: " + userID + " removido do grupo '" + groupID + "'";
							} else {
								mesSent = "Nao foi possivel remover o user '" + userID + "' do grupo '" + groupID + "'";
							}
						} else if (command.equals("m") || command.equals("msg")) {
							String groupID = split[1];
							boolean sent = acc.sendMessage(groupID, split);
							if (sent) {
								mesSent = "Mensagem enviada para o grupo '" + groupID + "'";
							} else {
								mesSent = "Nao foi possivel enviar a mensagem para o grupo '" + groupID + "'";
							}
						}
					} else if (split.length >= 3) {
						String command = split[0];
						if (command.equals("m") || command.equals("msg")) {
							String groupID = split[1];
							boolean sent = acc.sendMessage(groupID, split);
							if (sent) {
								mesSent = "Mensagem enviada para o grupo '" + groupID + "'";
							} else {
								mesSent = "Nao foi possivel enviar a mensagem para o grupo '" + groupID + "'";
							}
						} else {
							mesSent = "Comando invalido\n";
						}
					} else {
						mesSent = "Comando invalido\n";
					}
					outStream.writeObject(mesSent);
					outStream.flush();
				}
				inStream.close();
				outStream.close();
				socket.close();
			} catch (IOException | ClassNotFoundException e) {
				// e.printStackTrace();
				System.out.println("Cliente desconectado!");
			}
		}
	}
}