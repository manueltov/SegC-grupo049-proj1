package Client;



import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import SeiTchizKeys.KeysClient;


public class SeiTchiz {
    private Socket socketClient = null;
    private static int PORT = 45678;
    private KeysClient keysClient = null;
    
    public static void main(String[] args) {
        System.out.println("cliente SeiTchiz inicio");
        if (args.length != 5) {
            //127.0.0.1:45678
            System.err.println("Passe os dados desta maneira: <serverAddress> <truststore> <keystore> <keystore-password> <username> ");
            System.exit(0);
        }
        // Args serveradress
        String[] serverAddress = args[0].split(":");
        String ip = serverAddress[0];
        int port = Integer.parseInt(serverAddress[1]);
        System.out.println("---------" + ip + ":" + port + "---------");
        
        if(port != PORT) {
            System.err.println("Só se liga ao porto 45678");
            System.exit(-1);
        }
        //user
        String username = args[4];
        String path = "src"+File.separator+"Client"+File.separator+"PubKeys"+File.separator+username+File.separator;
        
        String keyStore=path+username+"Store";
        
        String truststore =path+"trustore.client";
        String keyStorePass=args[3];
       
        SeiTchiz client = new SeiTchiz();
        
        
        client.loadCertificate(username,truststore,keyStore,keyStorePass);
        System.out.println("KeyStore válida do Cliente");
        client.startClient(ip, port, username);
        
       
    }
    
    
    private void loadCertificate(String username, String truststore, String keyStore, String keyStorePass) {
      
    	System.setProperty("javax.net.ssl.trustStore",truststore);    
        System.setProperty("javax.net.ssl.keyStore",keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword",keyStorePass);
	
		keysClient = new KeysClient(username,keyStore,keyStorePass);
	}
	
    
	private void startClient(String ip, int port, String user) {
        try {
        	
        	SocketFactory sf = SSLSocketFactory.getDefault();
            socketClient = sf.createSocket(ip, port);

            ObjectOutputStream outStream = new ObjectOutputStream(socketClient.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(socketClient.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

         
            //envia credenciais e verifica login pelo user 
            outStream.writeObject(user);
            //servidor responde com um long 
            long code = (long) inStream.readObject();
           
            //recebe login true ou false
            String response = inStream.readObject().toString();
            
            System.out.println("Código recebido do Servidor");


            byte[] codeByte = ByteBuffer.allocate(Long.BYTES).putLong(code).array();
            byte[] cipherByte = keysClient.cripherPrivate(codeByte);
            byte[] longCifrado = (byte[])  cipherByte;
            
            outStream.writeObject(longCifrado);
            outStream.flush();

            if(response.equals("false")) {
            	Certificate certificate = keysClient.getCertificate();
            	outStream.flush();
				outStream.writeObject(certificate);
            }

            boolean confirmation =  (boolean) inStream.readObject();

            if (confirmation==true) {
            	
                System.out.println("Conectado!");
                System.out.println("*********************************");
                System.out.println("======= MENU DE COMANDOS ========");
                System.out.println("* follow <userID> | f <userID>");
                System.out.println("* unfollow <userID> | u <userID>");
                System.out.println("* viewfollowers | v");
                System.out.println("* post <photo> | p <photo>");
                System.out.println("* wall <nPhotos> | w <nPhotos>");
                System.out.println("* like <photoID> | l <photoID>");
                System.out.println("* newgroup <groupID> | n <groupID>");
                System.out.println("* addu <userID> <groupID> | a <userID> <groupID>");
                System.out.println("* removeu <userID> <groupID> | r <userID> <groupID>");
                System.out.println("* ginfo [groupID] | g [groupID]");
                System.out.println("* msg <groupID> <msg> | m <groupID> <msg>");
                System.out.println("* collect <groupID> | c <groupID>");
                System.out.println("* history <groupID> | h <groupID>");
                System.out.println("===================================");
                String str="";
                String str2="";
                while(!str.equals("stop")){
                    str=br.readLine();
                    outStream.writeObject(str);
                    outStream.flush();
                    str2= inStream.readObject().toString();
                    System.out.println(str2);
                }
            }
            else{
                System.out.println("Desconectado!");
                return;
            }
            outStream.close();
            inStream.close();
            socketClient.close();
        }

        catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
