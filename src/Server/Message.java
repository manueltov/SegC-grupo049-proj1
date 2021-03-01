package Server;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private ArrayList<String> alreadyRead;
    private ArrayList<String> canRead;

    public Message(String text) {
        this.text = text;
        this.alreadyRead = new ArrayList<>();
        this.canRead = new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public ArrayList<String> getAlreadyRead() {
        return alreadyRead;
    }

    public ArrayList<String> getCanRead() {
        return canRead;
    }

    public void addToAlreadyRead(String userID) {
        this.alreadyRead.add(userID);
    }

    public void addToCanRead(String userID) {
        this.canRead.add(userID);
    }

    public void removeFromCanRead(String userID) {
        for (String user : this.canRead) {
            if (user.equals(userID)) {
                this.canRead.remove(user);
                return;
            }
        }
    }

    public boolean checkIfCanRead(String userID) {
        for (String user : this.canRead) {
            if (user.equals(userID)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfAlreadyRead(String userID) {
        for (String user : this.alreadyRead) {
            if (user.equals(userID)) {
                return true;
            }
        }
        return false;
    }
}
