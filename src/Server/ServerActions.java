package Server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ServerActions {
	private String user = "";
	private String password = "";
	private int photoID = 0;
	private ObjectInputStream in = null;
	private static final String txt = ".txt";
	private static final String followers = "followers" + txt;
	private static final String ledger = "ledger" + txt;
	private static final String GROUPS_FOLDER = "./src/Server/Groups";
	private static final String USERS = "users.txt";
	private static final String IMAGES_FOLDER = "./src/Server/Images";

	public ServerActions(ObjectInputStream in, ObjectOutputStream out) {
		this.in = in;
		// this.out = out;
		createImagesFolder();
		this.photoID = 0;
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

	private boolean autenticacao() throws ClassNotFoundException, IOException {
		boolean existsUser;
		user = (String) in.readObject();
		password = (String) in.readObject();
		// ver se utilizador j√° existe
		existsUser = AuthenticationServer.getInstance().existsUser(user);
		// out.flush();
		// out.writeObject(existsUser);
		// se n√£o existir, cria um novo
		if (!existsUser) {
			AuthenticationServer.getInstance().registerUser(user, password);
			return true;
		}
		// se existir, verificar a password
		else {
			boolean login = AuthenticationServer.getInstance().checkPassword(user, password);
			if (login) {
				return true;
			} else {
				return false;
			}
		}
	}

	private static void createImagesFolder() {
		try {
			Path path = Paths.get(IMAGES_FOLDER);
			// java.nio.file.Files;
			Files.createDirectories(path);
		} catch (IOException e) {
			System.err.println("Erro: pasta de fotografias n„o criada" + e.getMessage());
		}
	}

	public boolean followUser(String userToFollow) throws IOException {
		boolean follow = false;
		List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(followers), StandardCharsets.UTF_8));
		for (int i = 0; i < fileContent.size(); i++) {
			String line = fileContent.get(i);
			String[] split = line.split(":");
			String userFollow = split[0];
			if (userFollow.equals(userToFollow) && !userToFollow.equals(user)) {
				if (split.length > 1) {
					String[] followersArray = split[1].split(",");
					if (!Arrays.asList(followersArray).contains(user)) {
						line = line + "," + user;
						follow = true;
					}
				} else {
					line = line + user;
					follow = true;
				}
				fileContent.set(i, line);
				break;
			}
		}
		Files.write(Paths.get(followers), fileContent, StandardCharsets.UTF_8);
		return follow;
	}

	public boolean unfollowUser(String userToUnfollow) throws IOException {
		boolean unfollow = false;
		List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(followers), StandardCharsets.UTF_8));
		for (int i = 0; i < fileContent.size(); i++) {
			String line = fileContent.get(i);
			String[] split = line.split(":");
			String userUnfollow = split[0];
			if (userUnfollow.equals(userToUnfollow) && !userToUnfollow.equals(user)) {
				if (split.length > 1) {
					String[] followersArray = split[1].split(",");
					ArrayList<String> followersList = new ArrayList<>(Arrays.asList(followersArray));
					for (int j = 0; j < followersList.size(); j++) {
						if (followersList.get(j).equals(user)) {
							followersList.remove(j);
							unfollow = true;
							break;
						}
					}
					line = userToUnfollow + ":";
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
		Files.write(Paths.get(followers), fileContent, StandardCharsets.UTF_8);
		return unfollow;
	}

	public String followers() throws IOException {
		String followersString = "Nao tem seguidores\n";
		ArrayList<String> fileContent = new ArrayList<>(
				Files.readAllLines(Paths.get(followers), StandardCharsets.UTF_8));
		for (int i = 0; i < fileContent.size(); i++) {
			String line = fileContent.get(i);
			String[] split = line.split(":");
			String username = split[0];
			if (username.equals(user) && split.length > 1) {
				followersString = split[1];
			}
		}
		return followersString;
	}

	public String post(String photo) {
		String photoID = null;

		String folderName = IMAGES_FOLDER + "/img_" + user;

		// if it doesn't exists, create it!
		try {
			Path path = Paths.get(folderName);
			Files.createDirectories(path);
		} catch (IOException e) {
			System.err.println("Erro: pasta de fotografias n„o criada" + e.getMessage());
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

	private String photoAdd(String photo, String folder) {
		String generatedPhotoID = null;
		String filename = folder + "/" + photo + ".txt";
//		File file = new File(filename);
		File file = openFile(filename);

		if (file.exists()) {
			generatedPhotoID = generatePhotoID();
		}

		if (generatedPhotoID != null) {
			if (addToLedger(user, generatedPhotoID)) {
				return generatedPhotoID;
			}
		}
		return generatedPhotoID;
	}

	private boolean addToLedger(String user, String photoID) {
		boolean added = false;
		LocalDateTime timeStamp = LocalDateTime.now();
		String timeStampEdited = timeStamp.toString().replace(":", "");
		// user:photoID:likes:timeStamp
		// ze:photo12:7:20210225083000
		String text = user + ":" + photoID + ":" + "0" + ":" + timeStampEdited + "\n";

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
		this.photoID++;
		String aux = "photo" + this.photoID;
		return aux;
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
		String likes_str = photoInfo[2];
		int likes = Integer.parseInt(likes_str);
		likes++;
		photoInfo[2] = likes + "";

		// update photo info
		if (updatePhotoInfo(photoID, photoInfo)) {
			liked = true;
		}

		return liked;
	}

	private String[] getPhotoInfo(String searchPhotoID) {
		// user:photoID:likes:timeStamp
		// ze:photo12:7:20210225083000
		String[] photoInfo = null;
		File ledgerFile = openFile(ledger);
		try (Scanner reader = new Scanner(ledgerFile)) {
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] split = line.split(":");
				String username = split[0];
				String photoID = split[1];
				String likes = split[2];
				String timeStamp = split[3];
				if (photoID.equals(searchPhotoID)) {
					photoInfo = new String[4];
					photoInfo[0] = username;
					photoInfo[1] = photoID;
					photoInfo[2] = likes;
					photoInfo[3] = timeStamp;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(" Erro ao ler o ficheiro ledger.");
		}
		return photoInfo;
	}

	private boolean updatePhotoInfo(String searchPhotoID, String[] photoInfo) {
		boolean photoInfoUpdated = false;
		// user:photoID:likes:timeStamp
		// ze:photo12:7:20210225083000

		try {
			List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(ledger), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent.size(); i++) {
				String line = fileContent.get(i);
				String[] split = line.split(":");
				String photoID = split[1];
				if (photoID.equals(searchPhotoID)) {
					String text = photoInfo[0] + ":" + photoInfo[1] + ":" + photoInfo[2] + ":" + photoInfo[3];
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
