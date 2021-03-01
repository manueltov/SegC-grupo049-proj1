import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ServerActions {
    private String user = "";
    private String password = "";
    private ObjectInputStream in = null;
    private static final String txt = ".txt";
    private static final String followers = "followers" + txt;
    private static final String groups = "groups" + txt;

    public ServerActions(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        // this.out = out;

        //create users.txt
        File file = openFile(groups);
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
        // ver se utilizador já existe
        existsUser = AuthenticationServer.getInstance().existsUser(user);
        // out.flush();
        // out.writeObject(existsUser);
        // se não existir, cria um novo
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
        String followersString = "";
        ArrayList<String> fileContent = new ArrayList<>(
                Files.readAllLines(Paths.get(followers), StandardCharsets.UTF_8));
        for (int i = 0; i < fileContent.size(); i++) {
            String line = fileContent.get(i);
            String[] split = line.split(":");
            String username = split[0];
            if (username.equals(user)) {
                followersString = split[1];
            }
        }
        return followersString;
    }

    public boolean newGroup(String groupID) {
        boolean created = false;

        // Se o grupo já existir assinala um erro
        if (groupExists(groupID)) {
            System.out.println("ERROR: that groupID already exists");
            created = false;
            return created;
        }

        // cria um grupo privado, cujo dono (owner) será o cliente que o criou
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(groups), StandardCharsets.UTF_8));
            String line = groupID + ":" + user + "\n";
            fileContent.set(fileContent.size()-1, line);
            Files.write(Paths.get(groups), fileContent, StandardCharsets.UTF_8);
            created = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: error reading file...");
        }
        return created;
    }

    private boolean groupExists(String groupID) {
        boolean exists = false;
        List<String> fileContent = null;
        try {
            fileContent = new ArrayList<>(Files.readAllLines(Paths.get(groups), StandardCharsets.UTF_8));
            for (int i = 0; i < fileContent.size(); i++) {
                String line = fileContent.get(i);
                String[] split = line.split(":");
                String groupIdentification = split[0];

                if (groupIdentification.equals(groupID)) {
                    exists = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: error reading file...");
        }
        return exists;
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
