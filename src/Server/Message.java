package Server;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * Esta classe é responsável pela definição e manipulação do tipo de dados "Message"
 *
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private ArrayList<String> alreadyRead;
    private ArrayList<String> canRead;
    /**
    * 
    * Cria o tipo de dados Message
    * 
    * @param text - corpo da mensagem
    *
    */
    public Message(String text) {
        this.text = text;
        this.alreadyRead = new ArrayList<>();
        this.canRead = new ArrayList<>();
    }
    /**
     * 
     * Devolve o corpo de uma mensagem
     * 
     * @return text - corpo da mensagem
     *
     */
    public String getText() {
        return text;
    }
    /**
     * 
     * Devolve uma lista de mensagens que ja foram lidas
     * 
     * @return alreadyRead - lista de mensagens
     *
     */
    public ArrayList<String> getAlreadyRead() {
        return alreadyRead;
    }
    /**
    * 
    * Devolve uma lista de mensagens que se podem ler
    * 
    * @return canRead - lista de mensagens
    *
    */
    public ArrayList<String> getCanRead() {
        return canRead;
    }
    /**
    * 
    * adiciona um utilizador á lista de utilizadores que ja leu a mensagem
    * 
    * @param userID - identificador do utilizador
    *
    */
    public void addToAlreadyRead(String userID) {
        this.alreadyRead.add(userID);
    }
    /**
     * 
     * adiciona um utilizador á lista de utilizadores que pode ler a mensagem
     * 
     * @param userID - identificador do utilizador
     *
     */
    public void addToCanRead(String userID) {
        this.canRead.add(userID);
    }
    /**
     * 
     * remove um utilizador á lista de utilizadores que pode ler a mensagem
     * 
     * @param userID - identificador do utilizador
     *
     */
    public void removeFromCanRead(String userID) {
        for (String user : this.canRead) {
            if (user.equals(userID)) {
                this.canRead.remove(user);
                return;
            }
        }
    }
    /**
     * 
     * verifica se um utilizador á lista de utilizadores que pode ler a mensagem
     * 
     * @param userID - identificador do utilizador
     *
     */
    public boolean checkIfCanRead(String userID) {
        for (String user : this.canRead) {
            if (user.equals(userID)) {
                return true;
            }
        }
        return false;
    }
    /**
     * 
     * verifica se um utilizador á lista de utilizadores que já leu a mensagem
     * 
     * @param userID - identificador do utilizador
     *
     */
    public boolean checkIfAlreadyRead(String userID) {
        for (String user : this.alreadyRead) {
            if (user.equals(userID)) {
                return true;
            }
        }
        return false;
    }
}
