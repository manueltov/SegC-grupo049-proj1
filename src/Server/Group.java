package Server;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * 
 * Esta classe é responsável pela definição e manipulação do tipo de dados "Group"
 *
 */
public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String owner;
    private String keyG;
    private ArrayList<String> members;
    private ArrayList<Message> inbox;
	
    /**
     * Cria o tipo de dados Group
     * @param groupID - identificador do grupo 
     * @param clientID - identificador do criador/dono do grupo
     * @param keyE - chave do grupo
     *
     */
    public Group(String groupID, String clientID, String keyE) {
        this.id = groupID;
        this.owner = clientID;
        this.setKeyG(keyE);
        this.members = new ArrayList<>();
        this.inbox = new ArrayList<>();
    }
    /**
     * devolve o identificador do grupo
     * 
 	 * @return id - identificador do grupo
     */
    public String getId() {
        return id;
    }
    /**
     * define o identificador do grupo
     * 
     * @param  id - identificador do grupo
 	 *
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * devolve o dono/criador do grupo
     * 
 	 * @return owner - identificador do grupo
     */
     public String getOwner() {
         return owner;
     }
   
    /**
    * 
    * define o dono/criador do grupo
    * 
    * @param  owner - nome do dono/criador do grupo
  	*
    */
    public void setOwner(String owner) {
        this.owner = owner;
    }
    /**
     * devolve a lista de membros do grupo
     * 
 	* @return members - lista de membros
     */
    public ArrayList<String> getMembers() {
        return members;
    }
    /**
     * define lista de membros do grupo
     * 
 	 * @param members - lista de membros
     */
    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }
    /**
    * 
    * devolve a lista de mensagens do grupo
    * 
 	* @return inbox - lista de mensagens
    */
    public ArrayList<Message> getInbox() {
        return inbox;
    }
    /**
     * 
     * devolve a lista de mensagens do grupo
     * 
 	* @return members - lista de membros
     */
    public void setInbox(ArrayList<Message> inbox) {
        this.inbox = inbox;
    }
    /**
     * verifica se o utilizador pesquisado pertence ao grupo
     *
     * @param userID - identificador do utilizador
 	* @return boolean - true - caso o utilizador exista no grupo - false - caso contrário
     */
    public boolean userInGroup(String userID) {
        boolean isOwner = false;
        boolean exists = false;
        if (userID.equals(this.owner)) {
            isOwner = true;
        }
        for (String s : this.members) {
            if (s.equals(userID)) {
                exists = true;
                break;
            }
        }
        return isOwner || exists;
    }
    /**
    * 
    * verifica se o utilizador pesquisado é membro do grupo
    *
    * @param userID - identificador do utilizador
 	* @return int - 0 - caso o utilizador seja membro - -1 - caso contrário
    */
    public int isMember(String userID) {
        int index = -1;
        int i = 0;
        for (String s : this.members) {
            if (s.equals(userID)) {
                index = i;
                break;
            }
            i++;
        }
        return index;
    }
    /**
     * adiciona uma mensagem ao grupo
     *
     * @param message - mensagem  
     */
    public void addMessage(String message) {
        Message msg = new Message(message);
        msg.addToCanRead(owner);
        for (String user : this.members) {
            msg.addToCanRead(user);
        }
        this.inbox.add(msg);
    }
    /**
    * devolve a chave do grupo
    *
    * @return keyG - chave do grupo
 	* 
    */
	public String getKeyG() {
		return keyG;
	}
	/**
	* define chave do grupo
	*
	* @param keyG - chave do grupo
	* 
	*/
	public void setKeyG(String keyG) {
		this.keyG = keyG;
	}
}
