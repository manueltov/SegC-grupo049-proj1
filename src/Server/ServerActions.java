package Server;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import SeiTchizKeys.Keys;
import SeiTchizKeys.KeysClient;
import SeiTchizKeys.KeysServer;
/**
 * 
 * Esta classe é responsável pela execução das funcões para cumprir os comandos enviados pelo utilizador do "SeiTchizServer"
 *
 */
public class ServerActions {
	private String user = "";
	private ObjectInputStream in = null;
	private ObjectOutputStream out;
	private static final String txt = ".txt";
	private static final String followers = "followers" + txt;
	private static final String following = "following" + txt;
	private static final String ledger = "ledger" + txt;
	private static final String GROUPS_FOLDER = "./src/Server/Groups";
	private static final String GROUPS_FOLDERKEYS = "./src/Server/GroupsKeys";
	// private static final String USERS = "users.txt"; //not used
	private static final String IMAGES_FOLDER = "./src/Server/Images";
	
	/**
	* 
	* Cria a stream de dados do server
	* 
	* @param in - stream de dados que entra
	* @param out -stream de dados que saem
	*/
	public ServerActions(ObjectInputStream in, ObjectOutputStream out) {
		this.in = in;
		this.out = out;
		createImagesFolder();
	}
	/**
	* 
	* verifica a autenticacao do utilizador
	* 
	* @return boolean - true - se a auntenticação tenha sucesso / - false - caso contrario
	*/
	public boolean comecaAccoes() {
		try {
			if (!autenticacao()) {
				return false;
			} else {
				return true;
			}
		} catch (IOException | ClassNotFoundException e) {
			System.err.println(" Erro ao receber e enviar mensagens ao Cliente");
		}
		return false;
	}
	private KeysServer keyServer = null;
	/**
	* 
	* carrega as chaves usadas no servidor vindas do servidor de chaves
	* 
	* @param keyServer - servidor de chaves
	*/
	public void loadKeys(KeysServer keyServer) {
		this.keyServer=keyServer;
	}
	private boolean autenticacao() throws ClassNotFoundException, IOException {
		
		boolean existsUser;
		//user recebido
		user = (String) in.readObject();
		
		// ver se utilizador já existe
		existsUser = AuthenticationServer.getInstance().existsUser(user);
		
	
		long code = (new Random()).nextLong();
		
		System.out.println("Código enviado ao servidor");
		
		//envia o long
		out.flush();
		out.writeObject(code);
		
		//ve se ja existe
		out.flush();
		out.writeObject(existsUser);
		
		//nonce cifrado pelo user
		byte[] codeByte = (byte[]) in.readObject();
		
		//certificado do user
		Certificate certificate = null;
		
		// se nao existir, cria um novo guardando o seu .cer
		if (!existsUser) {
		
			//guarda o certificado do client no servidor
			certificate = (Certificate) in.readObject();
			KeysServer.saveUserCertificate(user,certificate);
	
		}
	
		certificate = KeysServer.getUserCertificate(user);
		
		//chve publica cert
		byte[] decodeByte = Keys.decipher(codeByte,certificate.getPublicKey());
		
		long codeUser = ByteBuffer.wrap(decodeByte).getLong();
		
		boolean validation = code == codeUser;
		
		System.out.println("Validação do Cliente: "+ validation);
		out.flush();
		out.writeObject(validation);
		
		if ( !existsUser && validation) {
			AuthenticationServer.getInstance().registerUser(user);
		}
		
		return existsUser;
	}

	private static void createImagesFolder() {
		try {
			Path path = Paths.get(IMAGES_FOLDER);
			// java.nio.file.Files;
			Files.createDirectories(path);
			Path path_groups = Paths.get(GROUPS_FOLDER);
			Files.createDirectories(path_groups);
		} catch (IOException e) {
			System.err.println("Erro: pasta de fotografias nao criada" + e.getMessage());
		}
	}
	/**
	* 
	* função para seguir um utilizador
	* 
	* @param userToFollow - identificador do utilizador que se quer seguir
	* @return boolean - true - se a operação foi executada com sucesso / - false - caso contrário
	* @throws IOException
	*/
	public boolean followUser(String userToFollow) throws IOException {
		if (userToFollow.equals(this.user)) {
			System.err.println("Não é possível seguir-se a si mesmo.");
			return false;
		}

		boolean followersFile = false;
		boolean followingFile = false;

		try {

			// followers file
			followersFile = addToFollowers(userToFollow);
			if (!followersFile) {
				System.err.println("Problem adding to followers file...");
			}

			// following file
			followingFile = addToFollowing(userToFollow);
			if (!followingFile) {
				System.err.println("Problem adding to following file...");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return followersFile && followingFile;
	}

	private boolean addToFollowing(String userToFollow) throws Exception {
		boolean follow = false;
		boolean found = false;

		List<String> fileContent_following;
		try {
			fileContent_following = new ArrayList<>(Files.readAllLines(Paths.get(following), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent_following.size(); i++) {
				String line = fileContent_following.get(i);
				String[] split = line.split(":");
				String myuser = split[0];
				if (myuser.toLowerCase().equals(this.user.toLowerCase())) {
					found = true;
					if (split.length > 1) {
						String[] followingArray = split[1].split(",");
						if (!Arrays.asList(followingArray).contains(userToFollow.toLowerCase())) {
							line = line + "," + userToFollow.toLowerCase();
							follow = true;
						} else {
							throw new Exception("Já se encontra a seguir este utilizador.");
						}
					} else {
						line = line + userToFollow.toLowerCase();
						follow = true;
					}
					fileContent_following.set(i, line);
					break;
				}
			}
			if (!found) {
				throw new Exception("Utilizador não foi encontrado");
			}
			Files.write(Paths.get(following), fileContent_following, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("Not added to following.txt");
			// e.printStackTrace();
		}
		return follow;
	}

	private boolean addToFollowers(String userToFollow) throws Exception {
		boolean follow = false;
		boolean found = false;

		try {
			List<String> fileContent_followers;
			fileContent_followers = new ArrayList<>(Files.readAllLines(Paths.get(followers), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent_followers.size(); i++) {
				String line = fileContent_followers.get(i);
				String[] split = line.split(":");
				String userFollow = split[0];
				if (userFollow.toLowerCase().equals(userToFollow.toLowerCase())) {
					found = true;
					if (split.length > 1) {
						String[] followersArray = split[1].split(",");
						if (!Arrays.asList(followersArray).contains(user.toLowerCase())) {
							line = line + "," + user.toLowerCase();
							follow = true;
						} else {
							throw new Exception("Já se encontra a seguir este utilizador.");
						}
					} else {
						line = line + user.toLowerCase();
						follow = true;
					}
					fileContent_followers.set(i, line);
					break;
				}
			}
			if (!found) {
				throw new Exception("Utilizador não foi encontrado");
			}
			Files.write(Paths.get(followers), fileContent_followers, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("Not added to followers.txt");
			// e.printStackTrace();
		}
		return follow;
	}
	/**
	* 
	* função para deixar de seguir um utilizador
	* 
	* @param userToUnfollow - identificador do utilizador que se quer seguir
	* @return boolean - true - se a operação foi executada com sucesso / - false - caso contrário
	* @throws IOException
	*/
	public boolean unfollowUser(String userToUnfollow) throws IOException {
		if (userToUnfollow.equals(this.user)) {
			System.err.println("Não é permitido deixar de seguir o próprio.");
			return false;
		}

		boolean followersFile = false;
		boolean followingFile = false;

		try {

			// followers file
			followersFile = removeFromFollowers(userToUnfollow);
			if (!followersFile) {
				throw new Exception("Problem removing from followers file...");
			}

			// following file
			followingFile = removeFromFollowing(userToUnfollow);
			if (!followingFile) {
				throw new Exception("Problem removing from following file...");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return followersFile && followingFile;
	}

	private boolean removeFromFollowers(String userToUnfollow) throws Exception {
		boolean unfollow = false;
		boolean wasFollowing = false;
		boolean userUnfollowFound = false;

		List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(followers), StandardCharsets.UTF_8));
		for (int i = 0; i < fileContent.size(); i++) {
			String line = fileContent.get(i);
			String[] split = line.split(":");
			String userUnfollow = split[0];
			if (userUnfollow.toLowerCase().equals(userToUnfollow.toLowerCase())) {
				userUnfollowFound = true;
				if (split.length > 1) {
					String[] followersArray = split[1].split(",");
					ArrayList<String> followersList = new ArrayList<>(Arrays.asList(followersArray));
					for (int j = 0; j < followersList.size(); j++) {
						if (followersList.get(j).equals(user.toLowerCase())) {
							wasFollowing = true;
							followersList.remove(j);
							unfollow = true;
							break;
						}
					}
					if (!wasFollowing) {
						throw new Exception("Não se encontrava a seguir esse utilizador.");
					}
					line = userToUnfollow.toLowerCase() + ":";
					for (int j = 0; j < followersList.size(); j++) {
						if (j == 0) {
							line = line + followersList.get(j);
						} else {
							line = line + "," + followersList.get(j);
						}
					}
				}
				fileContent.set(i, line);
				break;
			}
		}
		if (!userUnfollowFound) {
			throw new Exception("Não foi encontrado o user que pretende deixar de seguir.");
		}
		Files.write(Paths.get(followers), fileContent, StandardCharsets.UTF_8);
		return unfollow;
	}
	
	private boolean removeFromFollowing(String userToUnfollow) throws Exception{
		boolean unfollow = false;
		boolean userUnfollowFound = false;

		List<String> fileContent_following;
		try {
			fileContent_following = new ArrayList<>(Files.readAllLines(Paths.get(following), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent_following.size(); i++) {
				String line = fileContent_following.get(i);
				String[] split = line.split(":");
				String myuser = split[0];
				if (myuser.toLowerCase().equals(this.user.toLowerCase())) {
					if (split.length > 1) {
						String[] followingArray = split[1].split(",");
						ArrayList<String> followingList = new ArrayList<>(Arrays.asList(followingArray));
						for (int j = 0; j < followingList.size(); j++) {
							if (followingList.get(j).equals(userToUnfollow.toLowerCase())) {
								userUnfollowFound = true;
								followingList.remove(j);
								unfollow = true;
								break;
							}
						}
						if (!userUnfollowFound) {
							throw new Exception(
									"Não foi possível deixar de seguir esse user pq não existe ou não o estava a seguir.");
						}
						line = myuser.toLowerCase() + ":";
						for (int j = 0; j < followingList.size(); j++) {
							if (j == 0) {
								line = line + followingList.get(j);
							} else {
								line = line + "," + followingList.get(j);
							}
						}
					}
					fileContent_following.set(i, line);
					break;
				}
			}
			Files.write(Paths.get(following), fileContent_following, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("Problem removing from following.txt");
			// e.printStackTrace();
		}
		return unfollow;
	}
	/**
	 * 
	 * função para mostrar os seguidores de um utilizador 
	 * 
	 * @return followersString - String com todos os nomes de todos os seguidores
	 * @throws IOException
	*/
	public String viewFollowers() throws IOException {
		String followersString = null;
		try {
			ArrayList<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(followers), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent.size(); i++) {
				String line = fileContent.get(i);
				String[] split = line.split(":");
				String username = split[0];
				if (username.toLowerCase().equals(user.toLowerCase())) {
					if (split.length > 1) {
						followersString = split[1];
					} else {
						System.out.println("Não tem seguidores.");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: reading followers file.");
		}
		return followersString;
	}
	/**
	 * 
	 * função para mostrar todos as pessoas seguidas pelo utilizador 
	 * 
	 * @return followingString - String com todos os nomes de todos os seguidos
	 * @throws IOException
	*/
	public String viewFollowing() throws IOException {
		String followingString = null;
		try {
			ArrayList<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(following), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent.size(); i++) {
				String line = fileContent.get(i);
				String[] split = line.split(":");
				String username = split[0];
				if (username.toLowerCase().equals(user.toLowerCase())) {
					if (split.length > 1) {
						followingString = split[1];
					} else {
						System.out.println("Não segue ninguém.");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: reading following file.");
		}
		return followingString;
	}

	public String post(byte[] secondMesReceived) {
		String photoID = null;

		String folderName = IMAGES_FOLDER + "/img_" + user;

		// if it doesn't exists, create it!
		try {
			Path path = Paths.get(folderName);
			Files.createDirectories(path);
		} catch (IOException e) {
			System.err.println("Erro: pasta de fotografias n�o criada" + e.getMessage());
			return photoID;
		}

		// add photo to that folder
		photoID = photoAdd(secondMesReceived, folderName);

		// verify if it worked
		if (photoID != null) {
			System.out.println(secondMesReceived + " successfully added!");
			return photoID;
		} else {
			System.out.println("Something went wrong... Photo wasn't added.");
			return photoID;
		}
	}

	private String photoAdd(byte[] photoFile, String folder) {
		String generatedPhotoID = generatePhotoID();
		String filename = folder + "/" + generatedPhotoID + ".png";
		File file = new File(filename);
//		File file = openFile(filename);

		byte[] content = (byte[]) photoFile;
		try {
			Files.write(file.toPath(), content);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (generatedPhotoID != null) {
			if (addToLedger(user, generatedPhotoID, generatedPhotoID)) {
				return generatedPhotoID;
			}
		}
		return generatedPhotoID;
	}

	private boolean addToLedger(String user, String photoID, String photoFileName) {
		boolean added = false;
		LocalDateTime timeStamp = LocalDateTime.now();
		String timeStampEdited = timeStamp.toString().replace(":", "");
		// current_ID:2
		// coolUser:photo1:bliblibli.txt:2:2021-03-03T105618.753201
		// user:photoID:filename:likes:timeStamp
		String text = user + ":" + photoID + ":" + photoFileName + ":" + "0" + ":" + timeStampEdited + "\n";
		try {
			BufferedWriter myWriter = new BufferedWriter(new FileWriter(openFile(ledger), true));
			myWriter.write(text);
			myWriter.close();
			System.out.println("ledger atualizado");
			added = true;
		} catch (IOException e) {
			System.out.println("Erro a escrever no ficheiro");
			e.printStackTrace();
			added = false;
		}
		return added;
	}

	private String generatePhotoID() {
		int photoID = getPhotoID();
		photoID++;
		if (updatePhotoID(photoID) == false) {
			System.out.println("Error, updating the photoID in the ledger.");
		}
		String aux = "photo" + photoID;
		return aux;
	}

	private boolean updatePhotoID(int photoID) {
		boolean updated = false;
		try {
			List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(ledger), StandardCharsets.UTF_8));
			String text = "current_ID:" + photoID;
			fileContent.set(0, text);
			Files.write(Paths.get(ledger), fileContent, StandardCharsets.UTF_8);
			updated = true;
		} catch (IOException e) {
			System.out.println("Error, updating the photoID in the ledger.");
			e.printStackTrace();
		}
		return updated;
	}

	private int getPhotoID() {
		// current_ID:2
		// coolUser:photo1:bliblibli.txt:2:2021-03-03T105618.753201
		// user:photoID:filename:likes:timeStamp
		String current_ID = null;
		File ledgerFile = openFile(ledger);
		try (Scanner reader = new Scanner(ledgerFile)) {
			String line = reader.nextLine();
			if (line.length() == 0) {
				List<String> fileContent = new ArrayList<>(
						Files.readAllLines(Paths.get(ledger), StandardCharsets.UTF_8));
				String text = "current_ID:" + 0;
				current_ID = 0 + "";
				fileContent.set(0, text);
				Files.write(Paths.get(ledger), fileContent, StandardCharsets.UTF_8);
			} else {
				String[] split = line.split(":");
				current_ID = split[1];
			}
		} catch (FileNotFoundException e) {
			System.err.println(" Erro ao ler o ficheiro ledger.");
		} catch (IOException e) {
			System.out.println("Error, updating the photoID in the ledger.");
			e.printStackTrace();
		}
		return Integer.parseInt(current_ID);
	}

	public String wall(String nPhotos) throws IOException {
		int numeroPhotos = Integer.parseInt(nPhotos);
		if (numeroPhotos < 1) {
			System.err.println("Erro: user tem de pedir uma ou mais fotos.");
		}

		String photosToPrint = null;

		// ir buscar as pessoas q se segue
		String following = null;
		following = viewFollowing();

		if (following == null) {
			photosToPrint = "Nao segue ninguém por isso nao há fotos para apresentar.";
			System.out.println(photosToPrint);
			return photosToPrint;
		}

		// a cada pessoa q se segue pegar nas fotos
		String[] followingList = following.split(",");
		List<String> followingStringList = new ArrayList<String>(Arrays.asList(followingList));
		StringBuilder sb = new StringBuilder();

		try {
			int photoCounter = Integer.parseInt(nPhotos);
			List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(ledger), StandardCharsets.UTF_8));

			if (fileContent.size() <= 1) {
				photosToPrint = "Não há fotos para apresentar.";
				System.out.println(photosToPrint);
				return photosToPrint;
			}

			int ultimaLinha = fileContent.size() - 1;
			while (photoCounter != 0) {
				String line = fileContent.get(ultimaLinha);
				String[] split = line.split(":");
				String userFromList = split[0];
				String photoID = split[1];
				String photoName = split[2];
				String photoLikes = split[3];
				if (followingStringList.contains(userFromList.toLowerCase())) {
					String text = "A foto com ID " + photoID + ", nome: " + photoName + ", tem " + photoLikes
							+ " likes.\n";
					sb.append(text);
					photoCounter--;
				}
				if (userFromList.equals("current_ID")) {
					break;
				}
				ultimaLinha--;
			}
		} catch (IOException e) {
			System.out.println("Error, updating the ledger.");
			e.printStackTrace();
		}
		photosToPrint = sb.toString();
		return photosToPrint;
	}

	public boolean like(String photoID) {
		boolean liked = false;

		// Get the photo info
		String[] photoInfo = getPhotoInfo(photoID);
		if (photoInfo == null) {
			System.out.println("Error, getting that photoID...");
			return liked;
		}

		// increment likes of that photo
		String likes_str = photoInfo[3];
		int likes = Integer.parseInt(likes_str);
		likes++;
		photoInfo[3] = likes + "";

		// update photo info
		if (updatePhotoInfo(photoID, photoInfo)) {
			liked = true;
		}

		return liked;
	}

	private String[] getPhotoInfo(String searchPhotoID) {
		// current_ID:2
		// coolUser:photo1:bliblibli.txt:2:2021-03-03T105618.753201
		// user:photoID:filename:likes:timeStamp
		String[] photoInfo = null;
		File ledgerFile = openFile(ledger);
		try (Scanner reader = new Scanner(ledgerFile)) {
			// queimar uma linha pois a primeira tem o currentID
			reader.nextLine();
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] split = line.split(":");
				String username = split[0];
				String photoID = split[1];
				String photoFileName = split[2];
				String likes = split[3];
				String timeStamp = split[4];
				if (photoID.equals(searchPhotoID)) {
					photoInfo = new String[5];
					photoInfo[0] = username;
					photoInfo[1] = photoID;
					photoInfo[2] = photoFileName;
					photoInfo[3] = likes;
					photoInfo[4] = timeStamp;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(" Erro ao ler o ficheiro ledger.");
		}
		return photoInfo;
	}

	private boolean updatePhotoInfo(String searchPhotoID, String[] photoInfo) {
		boolean photoInfoUpdated = false;
		// current_ID:2
		// coolUser:photo1:bliblibli.txt:2:2021-03-03T105618.753201
		// user:photoID:filename:likes:timeStamp

		try {
			List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(ledger), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent.size(); i++) {
				String line = fileContent.get(i);
				String[] split = line.split(":");
				String photoID = split[1];
				if (photoID.equals(searchPhotoID)) {
					String text = photoInfo[0] + ":" + photoInfo[1] + ":" + photoInfo[2] + ":" + photoInfo[3] + ":"
							+ photoInfo[4];
					fileContent.set(i, text);
					photoInfoUpdated = true;
					break;
				}
			}
			Files.write(Paths.get(ledger), fileContent, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("Error, updating the ledger.");
			e.printStackTrace();
		}

		return photoInfoUpdated;
	}
	/**
	 * 
	 * função para alterar  e criar um grupo 
	 *
	 * @param filename - File - ficheiro onde estão presentes os grupos
	 * @param group - Group - grupo de utilizadores
	 */
	public void writeGroup(File filename, Group group) {
		try {
			FileOutputStream f = new FileOutputStream(filename);
			ObjectOutputStream o = new ObjectOutputStream(f);
			o.writeObject(group);
			o.close();
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	* 
	* função para criar chave do grupo
	*
	* @param filename - File - ficheiro onde estão presentes os grupos
	* @param key - String - chave do grupo
	* @param groupID - String - identificador do grupo
	*/
	public void writeGroupKey(File filename, String key, String groupID) {
		File theDir=new File("./src/Server/GroupsKeys/"+groupID);
		if (!theDir.exists()){
		    theDir.mkdirs();
		}
		try {
			FileWriter fw = new FileWriter(filename,true);
			fw.write("0__"+key+"\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	* 
	* função que devolve todos os grupos
	*
	* @return groups - ArrayList - lista de grupos
	*/
	public ArrayList<Group> readGroups() {
		ArrayList<Group> groups = new ArrayList<>();
		File folder = new File(GROUPS_FOLDER);
		File[] files = folder.listFiles();
		try {
			for (int i = 0; i < files.length; i++) {
				String filename = files[i].getPath();
				FileInputStream fi = new FileInputStream(filename);
				ObjectInputStream oi = new ObjectInputStream(fi);

				Group group = (Group) oi.readObject();
				groups.add(group);

				fi.close();
				oi.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return groups;
	}
	/**
	* 
	* função que imprime as informações sobre todos os grupos
	*
	*/
	public void printGroups() {
		ArrayList<Group> groups = readGroups();
		for (Group g : groups) {
			System.out.println("=============== GROUP ===============");
			System.out.println("Id: " + g.getId());
			System.out.println("Dono: " + g.getOwner());
			System.out.println("Membros:");
			for (String s : g.getMembers()) {
				System.out.println("   -" + s);
			}
			System.out.println("Mensagens:");
			for (Message m : g.getInbox()) {
				System.out.println("   -" + m.getText());
				System.out.println("      Quem pode ler:");
				for (String us : m.getCanRead()) {
					System.out.println("         -" + us);
				}
				System.out.println("      Quem ja leu:");
				for (String us : m.getAlreadyRead()) {
					System.out.println("         -" + us);
				}
				System.out.println("------------------------------");
			}
			System.out.println("=====================================");
		}
	}
	/**
	* 
	* função que mostra as  mensagens enviadas para o grupo groupID e que o cliente ainda não tenha recebido
	*
	* @param groupID - String - identificador do grupo
	* @return msgStr - String - conjunto de mensagens que o utilizador ainda não leu / Nao pertence ao grupo ou o grupo nao existe / o existem mensagens novas
	*/
	public String collect(String groupID) {
		String msgStr = "Nao pertence ao grupo ou o grupo nao existe\n";
		ArrayList<Group> groups = readGroups();
		for (Group group : groups) {
			if (group.getId().equals(groupID) && group.userInGroup(user)) {
				msgStr = "";
				for (Message msg : group.getInbox()) {
					boolean canRead = msg.checkIfCanRead(user);
					if (canRead) {
						msg.addToAlreadyRead(user);
						msg.removeFromCanRead(user);
						msgStr = msgStr + msg.getText() + "\n";
					}
				}
				if (msgStr.equals("")) {
					msgStr = "Nao existem mensagens novas\n";
				}
				String filename = GROUPS_FOLDER + "/" + group.getId() + ".dat";
				writeGroup(new File(filename), group);
				break;
			}
		}
		return msgStr;
	}
	/**
	 * 
	 * função que imprime histórico das mensagens do grupo indicado que o cliente já leu anteriormente
	 *
	 * @param groupID - String - identificador do grupo
	 * @return msgStr - String - conjunto de mensagens que o utilizador ainda não leu / Nao pertence ao grupo ou o grupo nao existe / o existem mensagens novas
	*/
	public String history(String groupID) {
		String msgStr = "Nao pertence ao grupo ou o grupo nao existe\n";
		ArrayList<Group> groups = readGroups();
		for (Group group : groups) {
			if (group.getId().equals(groupID) && group.userInGroup(user)) {
				msgStr = "";
				for (Message msg : group.getInbox()) {
					boolean alreadyRead = msg.checkIfAlreadyRead(user);
					if (alreadyRead) {
						msgStr = msgStr + msg.getText() + "\n";
					}
				}
				if (msgStr.equals("")) {
					msgStr = "Nao existem mensagens no historico\n";
				}
				break;
			}
		}
		return msgStr;
	}
	/**
	  * 
	  * função que imprime a lista de grupos, mostando o dono e os membros
	  *
	  * @return listStr - String - lista de grupos
	*/	
	public String listGroups() {
		String listStr = "";
		ArrayList<Group> groups = readGroups();
		for (Group group : groups) {
			if (group.getOwner().equals(user)) {
				listStr = listStr + "Dono: " + group.getId() + ";\n";
			} else if (group.isMember(user) > -1) {
				listStr = listStr + "Membro: " + group.getId() + ";\n";
			}
		}
		return listStr;
	}
	/**
	  * 
	  * função que imprime as informações de um grupo, mostando o dono e os membros
	  *
	  * @return listStr - String - informações e detalhes do grupo 
	*/
	public String listGroups(String groupID) {
		String listStr = "";
		ArrayList<Group> groups = readGroups();
		for (Group g : groups) {
			if (g.getId().equals(groupID)) {
				if (g.userInGroup(user)) {
					listStr = listStr + "Informacoes do grupo " + groupID + ":\n";
					listStr = listStr + "Dono: " + g.getOwner() + "\n";
					for (String s : g.getMembers()) {
						listStr = listStr + "Membro: " + s + "\n";
					}
				}
				break;
			}
		}
		if (listStr.equals("")) {
			listStr = "Nao tem permissoes ou grupo nao existe\n";
		}
		return listStr;
	}
	private String keyE;
	public boolean newGroup(String groupID) throws IOException  {
	
		byte[] key = KeysClient.getSimetricKey();
		try {
			String keyEncrypted = new String(Keys.cipher(key,KeysClient.getPublicKey(user)));
			keyE= keyEncrypted;
		} catch (CertificateException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		boolean created = false;
		Path path = Paths.get("Groups");
		Path pathk = Paths.get("GroupsKeys");
		try {
			Files.createDirectories(path);
			Files.createDirectories(pathk);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String filename = GROUPS_FOLDER + "/" + groupID + ".dat";
		String keysAll = GROUPS_FOLDERKEYS+ File.separator+ groupID+File.separator+groupID+"keys.txt";
		
		File file = new File(filename);
		File fk = new File(keysAll);
		if (!file.exists()) {
			//ficheiro
			Group group = new Group(groupID, user,keyE);
			writeGroup(file, group);
			
			created = true;
		}
		if(!fk.exists()) {
			writeGroupKey(fk, keyE,groupID);
		}
		
		
		return created;
	}
	/**
	* 
	* função que adiciona um utilizador a um grupo
	* 
	* @param userID - String - identificador do utilizador a adicionar
	* @param groupID - String - identificador do grupo
	* @return boolean - true - caso o utilizador tenha sido adicionado ao grupo / - false - caso contrário 
	*/
	public boolean addUser(String userID, String groupID) {
		
		ArrayList<Group> groups = readGroups();
		boolean existsGroup = false;
		boolean added = false;
		
		//boolean existsUser = AuthenticationServer.getInstance().existsUser(userID);
		boolean existsUser=true;
	
		int i = 0;
		for (Group g : groups) {
			if (g.getId().equals(groupID)) {
				existsGroup = true;
				break;
			}
			i++;
		}
		if (!existsUser) {
			
			return false;
			
		}
		if (!existsGroup) {
			return false;
		}
		Group group = groups.get(i);
		if (!group.getOwner().equals(user)) {
			return false;
		}
		
		if (!group.userInGroup(userID)) {
			group.getMembers().add(userID);
			
			String filename = GROUPS_FOLDER + "/" + group.getId() + ".dat";
			writeGroup(new File(filename), group);
			added = true;
			updateGroupKey(groupID,group.getMembers(),group.getOwner());
			
		}
		readGroups();
		return added;
	}

	private void updateGroupKey(String groupID, ArrayList<String> arrayList, String owner) {
		byte[] keyS = KeysClient.getSimetricKey();
		
		System.out.println(arrayList+"***************");
		int keyCode = getNumberKeys(groupID,owner);
		System.out.println(keyCode);
		
		try {
			for(String user :arrayList) {
				String keyEncrypted = new String(KeysClient.cipher(keyS,KeysClient.getPublicKey(user)));
				addUserKey(groupID,user,keyCode+"",keyEncrypted);
			}
		}catch(IOException | CertificateException e) {
			System.out.println("Erro ao atualizar a key do groups");
		}
	}

	private void addUserKey(String groupID, String userID,String id , String key) throws IOException {
		File theDir=new File("./src/Server/GroupsKeys/"+groupID+File.separator+"Users");
		if (!theDir.exists()){
		    theDir.mkdirs();
		}
		//novo com path pa chave do user
		String p =GROUPS_FOLDERKEYS+File.separator+groupID+File.separator+"Users"+File.separator+userID+"keys.txt";
		String members=GROUPS_FOLDERKEYS+File.separator+groupID+File.separator+"Users"+File.separator+"Members.txt";
		
		File m = new File(members);
		File n = new File(p);
		
		String all = id+"__"+key;	
		writeGroupUsersKeys(p,all);
		String maux="<"+userID+","+p+">";
		writeToMembers(members,maux);
		DuplicatesFromFile(members);
		
	}
	private void DuplicatesFromFile(String members) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(members));
	    Set<String> lines = new HashSet<String>(10000); 
	    String line;
	    while ((line = reader.readLine()) != null) {
	        lines.add(line);
	    }
	    reader.close();
	    BufferedWriter writer = new BufferedWriter(new FileWriter(members));
	    for (String unique : lines) {
	        writer.write(unique);
	        writer.newLine();
	    }
	    writer.close();
	}

	private void writeToMembers(String members, String maux) {
		File f = openFile(members);
		try {
			FileWriter fw = new FileWriter(members,true);
			fw.write(maux+"\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void writeGroupUsersKeys(String file, String all) {
		File f = openFile(file);
		try {
			FileWriter fw = new FileWriter(file,true);
			fw.write(all+"\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private int getNumberKeys(String groupID, String owner) {
		int count=0;
		try {
		      File file = new File(GROUPS_FOLDERKEYS+File.separator+groupID+File.separator+groupID+"keys.txt");
		      Scanner sc = new Scanner(file);

		      while(sc.hasNextLine()) {
		        sc.nextLine();
		        count++;
		      }
		      sc.close();
		    } catch (Exception e) {
		      e.getStackTrace();
		    }
		//se nao da o dobro
		return count/2;
	}
	/**
	 * 
	 * função que remove um utilizador de um grupo
	 * 
	 * @param userID - String - identificador do utilizador a remover
	 * @param groupID - String - identificador do grupo
	 * @return boolean - true - caso o utilizador tenha sido removido do grupo / - false - caso contrário 
	 * @throws IOException
	*/
	public boolean removeUser(String userID, String groupID) throws IOException  {
		ArrayList<Group> groups = readGroups();
		boolean existsGroup = false;
		boolean deleted = false;
		//boolean existsUser = AuthenticationServer.getInstance().existsUser(userID);
		boolean existsUser=true;
		int i = 0;
		for (Group g : groups) {
			if (g.getId().equals(groupID)) {
				existsGroup = true;
				break;
			}
			i++;
		}
		if (!existsUser) {
			return false;
		}
		if (!existsGroup) {
			return false;
		}
		Group group = groups.get(i);
		if (!group.getOwner().equals(user)) {
			return false;
		}
		int pos = group.isMember(userID);
		if (pos > -1) {
			group.getMembers().remove(pos);
			String filename = GROUPS_FOLDER + "/" + group.getId() + ".dat";
			writeGroup(new File(filename), group);
			deleted = true;
			deleteUserGroup(groupID,userID);
		}
		readGroups();
		return deleted;
	}

	private void deleteUserGroup(String groupID, String userID) throws IOException  {
		File file = new File(GROUPS_FOLDERKEYS+File.separator+groupID+File.separator+"Users"+File.separator+userID+"keys.txt");
		removeAUX(file,userID);
		File m= new File(GROUPS_FOLDERKEYS+File.separator+groupID+File.separator+"Users"+File.separator+"Members.txt");
		removeAUXMEMBERS(m,userID,groupID);
		
		
	}

	private void removeAUXMEMBERS(File file, String userID,String groupID) throws IOException  {
		String r ="<"+userID+","+"./src/Server/GroupsKeys/"+groupID+"/Users/"+userID+"keys.txt>";
		File tempFile = new File(GROUPS_FOLDERKEYS+File.separator+groupID+File.separator+"Users"+File.separator+"MembersT.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
	    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

	    String currentLine;
	   
	    while ((currentLine = reader.readLine()) != null) {
	    	String trimmedLine = currentLine.trim();
	        if(trimmedLine.equals(r)) continue;
	        writer.write(currentLine + System.getProperty("line.separator"));
	    }
	    writer.close();
	    reader.close();
	    tempFile.renameTo(file);
	}

	private void removeAUX(File file, String userID) {
		try(Scanner scanner = new Scanner(file)) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                String line;
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (!line.trim().equals(userID)) {

                        pw.println(line);
                        pw.flush();
                    }
                }
               
                if (!file.delete()) {
                    System.out.println("Could not delete file");
                    return;
                }

                if (!file.renameTo(file))
                    System.out.println("Could not rename file");
            }
        }
      catch (IOException e)
      {
          System.out.println("IO Exception Occurred");
      }

		
	}
	/**
	* 
	* função que envia uma mensagem para um grupo 
	* 
	* @param groupID - String - identificador do grupo
	* @param messageArray -String[] - conjunto
	* @return boolean - true - caso o utilizador tenha sido removido do grupo / - false - caso contrário 
	* @throws IOException
	*/
	public boolean sendMessage(String groupID, String[] messageArray) {
		boolean sent = false;
		ArrayList<Group> groups = readGroups();
		for (Group group : groups) {
			if (group.getId().equals(groupID) && group.userInGroup(user)) {
				String message = "";
				for (int i = 0; i < messageArray.length; i++) {
					if (i >= 2) {
						message = message + " " + messageArray[i];
					}
				}
				group.addMessage(message);
				String filename = GROUPS_FOLDER + "/" + group.getId() + ".dat";
				writeGroup(new File(filename), group);
				sent = true;
				break;
			}
		}
		return sent;
	}

	private File openFile(String str) {
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
	
		 
}
