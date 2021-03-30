


*** REALIZADO E TESTADO EM AMBIENTE LINUX  E MACOS***

** 45678 é o Porto usado para a coneção
** "user" é o utilizador a utilizar
** "password" é a do respetivo "user"


COM POLITICAS e com .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

java -cp bin -Djava.security.manager -Djava.security.policy=server.policy -jar server.jar 45678

Para correr o cliente deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

java -cp bin -Djava.security.manager -Djava.security.policy=client.policy -jar client.jar 127.0.0.1:45678 user password


*------------------------------------------------------------------------------*

SEM POLITICAS e sem .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

java -cp bin Server.SeiTchizServer 45678

Para correr o cliente deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

java -cp bin Client.SeiTchiz 127.0.0.1:45678 user password

*------------------------------------------------------------------------------*

COM POLITICAS mas sem os .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

java -cp bin -Djava.security.manager -Djava.security.policy=server.policy Server.SeiTchizServer 45678


Para correr o cliente deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

java -cp bin -Djava.security.manager -Djava.security.policy=client.policy Client.SeiTchiz 127.0.0.1:45678 user password

*------------------------------------------------------------------------------*
NO ECLIPSE E COM POLITICAS

1º Importar a pasta do projeto
2º Ir a classe .java do servidor a SeiTchizServer e fazer : run as -> Run configuration -> Arguments -> 45678  nos Argumentos e
	na VM -> -Djava.security.manager
			 -Djava.security.policy=server.policy

3º Fazer o mesmo na classe .java do cliente a SeiTchiz e fazer : run as -> Run configuration -> Arguments -> 127.0.0.1:45678 user password 
	nos Argumentos e na VM -> -Djava.security.manager
						      -Djava.security.policy=client.policy

*-----------------------FEITA A CONEÇÃO---------------------------------*

Feita a coneção, é visualizada uma lista de comandos que o Client pode efectuar ao seu gosto.

Para desconectar o Client, basta escrever "stop"

