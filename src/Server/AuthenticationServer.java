package Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
/**
 * 
 * Esta classe é responsável pela autenticação e registo no servidor 
 *
 */
public class AuthenticationServer {

	private static AuthenticationServer autentica = null;
	private static final String txt = ".txt";
	private static final String utilizadores = "users" + txt;
	private static final String followers = "followers" + txt;
	private static final String following = "following" + txt;

	private AuthenticationServer() {

	}

	public static AuthenticationServer getInstance() {
		if (autentica == null) {
			autentica = new AuthenticationServer();
		}
		return autentica;
	}

	/**
	 * 
	 * Regista um novo utilizador
	 * 
	 * @param user - o nome do utilizador 
	 * @return boolean - true - se foi registado com sucesso / - false - se não foi registado com sucesso
	 * @throws IOException
	*/
	public boolean registerUser(String user) throws IOException {
		File file = openFile(utilizadores);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		writer.write(user.toLowerCase() + "\n");
		writer.close();

		// followers file
		file = openFile(followers);
		writer = new BufferedWriter(new FileWriter(file, true));
		writer.write(user.toLowerCase() + ":\n");
		writer.close();

		// following file
		file = openFile(following);
		writer = new BufferedWriter(new FileWriter(file, true));
		writer.write(user.toLowerCase() + ":\n");
		writer.close();

		System.out.println("User adicionado " + user.toLowerCase());
		return true;
	}
	/**
	 * 
	 * Verifica se já existe 
	 * 
	 * @param username - o nome do utilizador 
	 * @return boolean - true - se já existe  / - false - se não existir 
	 * 
	*/
	public boolean existsUser(String username) {
		File usersFile = openFile(utilizadores);
		try (Scanner reader = new Scanner(usersFile)) {
			while (reader.hasNextLine()) {
				//String line = reader.nextLine();
				//String[] split = line.split(":");
				//String username = split[0];
				if (username.toLowerCase().equals(reader.nextLine())) {
					System.out.println("User " + username.toLowerCase() + " existe");
					return true;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(" Erro ao ler o ficheiro de utilizadores.");
		}
		return false;
	}

	//não usado
	/*public boolean checkTrustStore(String username, String trust_store) {
		boolean valid = false;
		File usersFile = openFile(utilizadores);
		try (Scanner reader = new Scanner(usersFile)) {
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] split = line.split(":");
				String user = split[0];
				String pw = split[1];
				if (username.equals(user) && trust_store.equals(pw)) {
					valid = true;
					break;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(" Erro ao ler o ficheiro de utilizadores.");
		}
		if (valid) {
			System.out.println("User " + username + ":" + trust_store + " correto");
		} else {
			System.out.println("User " + username + ":" + trust_store + " incorreto");
		}
		return valid;
	}
	*/
	/**
	* 
	* Procura e devolve um ficheiro com o nome dado, caso o ficheiro não exista, cria um novo ficheiro
	* 
	* @param str - nome do ficheiro 
	* @return file - true - ficheiro com o nome dado
	* 
	*/
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
