import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class AuthenticationServer {

    private static AuthenticationServer autentica = null;
    private static final String txt = ".txt";
    private static final String utilizadores = "users" + txt;
    private static final String followers = "followers" + txt;

    private AuthenticationServer() {

    }

    public static AuthenticationServer getInstance() {
        if (autentica == null) {
            autentica = new AuthenticationServer();
        }
        return autentica;
    }

    // falta meter pass
    public boolean registerUser(String user, String password) throws IOException {
        File file = openFile(utilizadores);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(user + ":" + password + "\n");
        writer.close();
        file = openFile(followers);
        writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(user + ":\n");
        writer.close();
        System.out.println("User adicionado com respetiva password " + user);
        return true;
    }

    public boolean existsUser(String user) {
        File usersFile = openFile(utilizadores);
        try (Scanner reader = new Scanner(usersFile)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] split = line.split(":");
                String username = split[0];
                if (user.equals(username)) {
                    System.out.println("User " + user + " existe");
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(" Erro ao ler o ficheiro de utilizadores.");
        }
        return false;
    }

    public boolean checkPassword(String username, String password) {
        boolean valid = false;
        File usersFile = openFile(utilizadores);
        try (Scanner reader = new Scanner(usersFile)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] split = line.split(":");
                String user = split[0];
                String pw = split[1];
                if (username.equals(user) && password.equals(pw)) {
                    valid = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(" Erro ao ler o ficheiro de utilizadores.");
        }
        if (valid) {
            System.out.println("User " + username + ":" + password + " correto");
        } else {
            System.out.println("User " + username + ":" + password + " incorreto");
        }
        return valid;
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
