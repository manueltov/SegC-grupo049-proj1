import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SeiTchizServer {
    private static int PORT = 45678;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Server tem que correr com o comando: 'SeiTchizServer <port>'");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        if (port != PORT) {
            System.err.println("SeiTchizServer só no porto " + PORT);
            System.exit(0);
        }
        System.out.println("Conexão feita ao porto " + port);
        SeiTchizServer server = new SeiTchizServer();
        server.startServer(port);
    }

    private void startServer(int port) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor criado");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        while (true) {
            try {
                socket = serverSocket.accept();
                System.out.println("Mensagem recebida");
                ServerThread newServerThread = new ServerThread(socket);
                newServerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ServerThread extends Thread {
        private Socket socket = null;

        public ServerThread(Socket soc) {
            socket = soc;
            System.out.println("Conexão feita com o Cliente SeiTchiz");
        }

        public void run() {
            try {
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

                ServerActions acc = new ServerActions(inStream, outStream);

                boolean login = acc.comecaAccoes();
                outStream.writeObject(login);
                outStream.flush();

                String mesReceived = "";
                String mesSent = "";
                // enquanto o servidor não receber uma mensagem a dizer 'stop' ele continua à
                // espera de pedidos
                while (!mesReceived.equals("stop")) {
                    mesReceived = (String) inStream.readObject();
                    mesReceived = mesReceived.toLowerCase();
                    System.out.println(mesReceived);
                    String[] split = mesReceived.split(" ");
                    mesSent = "Mensagem inválida";
                    if (split.length == 1) {
                        String command = split[0];
                        if (command.equals("v") || command.equals("viewfollowers")) {
                            mesSent = "Seguidores:\n" + acc.followers();
                        }
                    } else if (split.length == 2) {
                        String command = split[0];
                        String arg1 = split[1];
                        if (command.equals("f") || command.equals("follow")) {
                            boolean follow = acc.followUser(arg1);
                            if (follow) {
                                mesSent = "Seguiu: " + arg1;
                            } else {
                                mesSent = "Não foi possível seguir: " + arg1;
                            }
                        } else if (command.equals("u") || command.equals("unfollow")) {
                            boolean unfollow = acc.unfollowUser(arg1);
                            if (unfollow) {
                                mesSent = "Deixou de seguir: " + arg1;
                            } else {
                                mesSent = "Não foi possível deixar de seguir: " + arg1;
                            }
                        } else if (command.equals("n") || command.equals("newgroup")) {
                            boolean newgroup = acc.newGroup(arg1);
                            if (newgroup) {
                                mesSent = "Group created: " + arg1;
                            } else {
                                mesSent = "Failled creating the group: " + arg1;
                            }
                        }
                    }
                    outStream.writeObject(mesSent);
                    outStream.flush();
                }
                inStream.close();
                outStream.close();
                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                // e.printStackTrace();
                System.out.println("Cliente desconectado!");
            }
        }
    }
}