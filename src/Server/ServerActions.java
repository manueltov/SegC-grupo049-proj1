package Server;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import SeiTchizKeys.Keys;
import SeiTchizKeys.KeysServer;

public class ServerActions {
	private String user = "";
	private ObjectInputStream in = null;
	private ObjectOutputStream out;
	private static final String txt = ".txt";
	private static final String followers = "followers" + txt;
	private static final String following = "following" + txt;
	private static final String ledger = "ledger" + txt;
	private static final String GROUPS_FOLDER = "./src/Server/Groups";
	// private static final String USERS = "users.txt"; //not used
	private static final String IMAGES_FOLDER = "./src/Server/Images";

	public ServerActions(ObjectInputStream in, ObjectOutputStream out) {
		this.in = in;
		this.out = out;
		createImagesFolder();
	}

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

	public void loadKeys(KeysServer keyServer) {
		this.keyServer = keyServer;
	}

	private boolean autenticacao() throws ClassNotFoundException, IOException {

		boolean existsUser;
		// user recebido
		user = (String) in.readObject();

		// ver se utilizador já existe
		existsUser = AuthenticationServer.getInstance().existsUser(user);

		long code = (new Random()).nextLong();

		System.out.println("Código enviado ao servidor");

		// envia o long
		out.flush();
		out.writeObject(code);

		// ve se ja existe
		out.flush();
		out.writeObject(existsUser);

		// nonce cifrado pelo user
		byte[] codeByte = (byte[]) in.readObject();

		// certificado do user
		Certificate certificate = null;

		// se nao existir, cria um novo guardando o seu .cer
		if (!existsUser) {

			// guarda o certificado do client no servidor
			certificate = (Certificate) in.readObject();
			KeysServer.saveUserCertificate(user, certificate);

		}

		certificate = KeysServer.getUserCertificate(user);

		// chve publica cert
		byte[] decodeByte = Keys.decipher(codeByte, certificate.getPublicKey());

		long codeUser = ByteBuffer.wrap(decodeByte).getLong();

		boolean validation = code == codeUser;

		System.out.println("Validação do Cliente: " + validation);
		out.flush();
		out.writeObject(validation);

		if (!existsUser && validation) {
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
				System.out.println("Problem adding to followers file...");
			}

			// following file
			followingFile = addToFollowing(userToFollow);
			if (!followingFile) {
				System.out.println("Problem adding to following file...");
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

	private boolean removeFromFollowing(String userToUnfollow) throws Exception {
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

	public String viewfollowers() throws IOException {
		String followersString = "Nao tem seguidores\n";
		ArrayList<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(followers), StandardCharsets.UTF_8));
		for (int i = 0; i < fileContent.size(); i++) {
			String line = fileContent.get(i);
			String[] split = line.split(":");
			String username = split[0];
			if (username.toLowerCase().equals(user.toLowerCase())) {
				if (split.length > 1) {
					followersString = split[1];
				} else {
					System.err.println("Não tem seguidores");
				}
			}
		}
		return followersString;
	}

	public String following() throws IOException {
		String followingString = "Nao segue ninguem\n";
		ArrayList<String> fileContent = new ArrayList<>(
				Files.readAllLines(Paths.get(following), StandardCharsets.UTF_8));
		for (int i = 0; i < fileContent.size(); i++) {
			String line = fileContent.get(i);
			String[] split = line.split(":");
			String username = split[0];
			if (username.equals(user) && split.length > 1) {
				followingString = split[1];
			}
		}
		return followingString;
	}

	public String post(String photo) {
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
		photoID = photoAdd(photo, folderName);

		// verify if it worked
		if (photoID != null) {
			System.out.println(photo + " successfully added!");
			return photoID;
		} else {
			System.out.println("Something went wrong... Photo wasn't added.");
			return photoID;
		}
	}

	private String photoAdd(String photoFileName, String folder) {
		String generatedPhotoID = null;
		String filename = folder + "/" + photoFileName + ".txt";
//		File file = new File(filename);
		File file = openFile(filename);

		if (file.exists()) {
			generatedPhotoID = generatePhotoID();
		}

		if (generatedPhotoID != null) {
			if (addToLedger(user, generatedPhotoID, photoFileName)) {
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

	public String wall(String nPhotos) {
		int numeroPhotos = Integer.parseInt(nPhotos);
		if (numeroPhotos < 1) {
			System.out.println("Erro: tem de pedir uma ou mais fotos.");
		}

		String photosToPrint = null;

		// ir buscar as pessoas q se segue
		String following = null;
		try {
			following = following();
		} catch (IOException e) {
			System.out.println("Error getting who am i following...");
			// e.printStackTrace();
			return photosToPrint;
		}

		if (following.equals("Nao segue ninguem\n")) {
			System.out.println("Nao segues ninguem por isso nao ha fotos para apresentar.");
			return null;
		}

		// a cada pessoa q se segue pegar nas fotos
		String[] followingList = following.split(",");
		List<String> followingStringList = new ArrayList<String>(Arrays.asList(followingList));
		StringBuilder sb = new StringBuilder();

		try {
			int photoCounter = Integer.parseInt(nPhotos);
			List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(ledger), StandardCharsets.UTF_8));

			if (fileContent.size() <= 1) {
				System.out.println("Nao ha fotos para apresentar.");
				return null;
			}

			int ultimaLinha = fileContent.size() - 1;
			System.out.println(ultimaLinha);
			while (photoCounter != 0) {
				String line = fileContent.get(ultimaLinha);
				String[] split = line.split(":");
				String userFromList = split[0];
				String photoID = split[1];
				String photoName = split[2];
				String photoLikes = split[3];
				if (followingStringList.contains(userFromList)) {
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

	public boolean newGroup(String groupID) {
		boolean created = false;
		Path path = Paths.get("Groups");
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String filename = GROUPS_FOLDER + "/" + groupID + ".dat";
		File file = new File(filename);
		if (!file.exists()) {
			Group group = new Group(groupID, user);
			writeGroup(file, group);
			created = true;
		}
		return created;
	}

	public boolean addUser(String userID, String groupID) {
		ArrayList<Group> groups = readGroups();
		boolean existsGroup = false;
		boolean added = false;
		boolean existsUser = AuthenticationServer.getInstance().existsUser(userID);
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
		}
		readGroups();
		return added;
	}

	public boolean removeUser(String userID, String groupID) {
		ArrayList<Group> groups = readGroups();
		boolean existsGroup = false;
		boolean deleted = false;
		boolean existsUser = AuthenticationServer.getInstance().existsUser(userID);
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
		}
		readGroups();
		return deleted;
	}

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
