package Server;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String owner;
    private ArrayList<String> members;
    private ArrayList<Message> inbox;

    public Group(String groupID, String clientID) {
        this.id = groupID;
        this.owner = clientID;
        this.members = new ArrayList<>();
        this.inbox = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public ArrayList<Message> getInbox() {
        return inbox;
    }

    public void setInbox(ArrayList<Message> inbox) {
        this.inbox = inbox;
    }

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

    public void addMessage(String message) {
        Message msg = new Message(message);
        msg.addToCanRead(owner);
        for (String user : this.members) {
            msg.addToCanRead(user);
        }
        this.inbox.add(msg);
    }
}
