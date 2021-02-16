//SC 2020/2021

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SeiTchizServer {
  String file = "//SeiTchizServer/db.txt";
  public static void main(String[] args) {
    connectToServer();
  }

  public static void connectToServer() {
    try(ServerSocket serverSocket = new ServerSocket(45678)) {
        Socket connectionSocket = serverSocket.accept();

        InputStream inputToServer = connectionSocket.getInputStream();
        OutputStream outoutFromServer = connectionSocket.getOutputStream();

        Scanner scanner = new Scanner(inputToServer, "UFT-8");
        PrintWriter serverPrintOut = new PrintWriter(new OutputStreamWriter(outputFromServer, "UTF-8"), true);

       serverPrintOut.println("Bem-vindo ao servidor SeiTchizServer");
       serverPrintOut.println("Introduza o seu nome de utilizador:");
       String user = scanner.nextLine();
       if (user == "exit"){
         return;
       }
       serverPrintOut.println("Introduza a sua palavra-passe:");
       String pass = scanner.nextLine();

       if (autentication(user,pass)== true){
          serverPrintOut.println("Bem-vindo <user>");
        }
      else(){
        serverPrintOut.println("nome e/ou palavra-passe incorrectos, por favor tente novamente");
        connectToServer();
      }

       }

  public bool autentication (String nome, String pass){
    String stringToSearch = "<nome>:<pass>";
    try (Stream<String> stream = Files.lines(Paths.get(file))) {
     // Find first
     Optional<String> lineHavingTarget = stream.filter(l -> l.contains(stringToSearch)).findFirst();}
     if (lineHavingTarget != NULL){
       return true
     }
     else{
       return false
     }
     catch (IOException e) {
         // log exception
    }
  }
