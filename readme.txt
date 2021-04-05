
*** REALIZADO E TESTADO EM AMBIENTE LINUX  E MACOS***

*** 45678 é o Porto usado para a coneção
*** Todas as keystores têm como password : 456789
*** Existem 3 users criados (João, Rafael, Ana) com as suas respetivas keystores(JoãoStore,RafaelStore,AnaStore) e certificados. 
***Os testes foram feitos com estes 3 utilizadores.

**------------------------------------------------------------------------------*

** É possivel fazer a ligação com o RafaelStore e Rafael, com o JoãoStore e João e com AnaStore e Ana, substituindo nos parametros do cliente na keystore e userID respetivamente
** O Servidor usa como parâmetros : <porto> <keystore> <keystore-password>
** O Cliente usa como parâmetros : <ip:porto> <truststore> <keystore> <keystore-password> <clientID>

**------------------------------------------------------------------------------
COM POLITICAS e com .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo49-proj1-2 e correr o seguinte comando:

java -cp bin -Djava.security.manager -Djava.security.policy=server.policy -jar server.jar 45678 servidorStore 456789

Para correr o cliente deve colocar-se na pasta SegC-grupo49-proj1-2 e correr o seguinte comando:

(Entrando com o Rafael como exemplo)
java -cp bin -Djava.security.manager -Djava.security.policy=client.policy -jar client.jar 127.0.0.1:45678  trustore.client RafaelStore 456789 Rafael

***------------------------------------------------------------------------------*
COM POLITICAS e sem .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo49-proj1-2 e correr o seguinte comando:

java -cp bin -Djava.security.manager -Djava.security.policy=server.policy Server.SeiTchizServer 45678 servidorStore 456789

Para correr o cliente deve colocar-se na pasta SegC-grupo49-proj1-2 e correr o seguinte comando:

(Entrando com o Rafael como exemplo)
java -cp bin -Djava.security.manager -Djava.security.policy=client.policy Client.SeiTchiz 127.0.0.1:45678  trustore.client RafaelStore 456789 Rafael


**------------------------------------------------------------------------------
SEM POLITICAS e sem .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo49-proj1-2 e correr o seguinte comando:
java -cp bin Server.SeiTchizServer 45678 servidorStore 456789

Para correr o cliente deve colocar-se na pasta SegC-grupo49-proj1 e correr o seguinte comando:

(Entrando com o Rafael)
java -cp bin Client.SeiTchiz 127.0.0.1:45678  trustore.client RafaelStore 456789 Rafael
***------------------------------------------------------------------------------*
NO ECLIPSE E COM POLITICAS

1º Importar a pasta do projeto
2º Ir a classe .java do servidor a SeiTchizServer e fazer : run as -> Run configuration -> Arguments -> 45678 servidorStore 456789 nos Argumentos e
	na VM -> -Djava.security.manager
			 -Djava.security.policy=server.policy

3º Fazer o mesmo na classe .java do cliente a SeiTchiz e fazer : run as -> Run configuration -> Arguments -> 127.0.0.1:45678 truststore.client RafaelStore 456789 Rafael
	 nos Argumentos e na VM -> -Djava.security.manager
						       -Djava.security.policy=client.policy

 *-----------------------FEITA A CONEÇÃO---------------------------------*

Feita a coneção, é visualizada uma lista de comandos que o Client pode efectuar ao seu gosto.

Para desconectar o Client, basta escrever "stop"

*-----------------------IMPLEMENTAÇÃO---------------------------------*

*** 45678 é o Porto usado para a coneção
*** Todas as keystores têm como password : 456789
*** Existem 3 users criados (João, Rafael, Ana) com as suas respetivas keystores(JoãoStore,RafaelStore,AnaStore) e certificados. 
***Os testes foram feitos com estes 3 utilizadores.
***Ao adicionar um membro ao grupo (comando addu), parti do suposto que este membro já existia(ja autenticado, estando no ficheiro users.txt,alterei a linha onde ve se o user existe na autenticação para true, na primeira do primeiro trabalho essa linha funcionava corretamente, nesta não consegui entender o porquê então coloquei essa variável a true para conseguir implementar) para conseguir implementar este método, o mesmo acontece com o removeu.

***O metodo msg não foi implementado nesta fase, contendo as funcionalidades(implementadas) da fase anterior.

***No metodo post, é enviado um ficheiro/foto para o servidor, a foto a enviar deve ser colocada/criada, na pasta postDirectory (criada depois de se inicializar a conecção).Ao chamar o metodo post, tem que se chamar a foto/ inserir a foto com a sua extenção (por exemplo, se criar a foto "teste.png", tem que chamar post com teste.png ( p teste.png)).

***No trabalho enviado, vai a pasta postDirectory já criada, contendo assim uma foto como teste(teste.png), caso queira testar desta forma. Caso queria apagar o postDirectory e esta foto, terá na mesma de depois de criada a coneção entre o cliente e o servidor, ele cria então a pasta postDirectory mas terá então que criar uma foto como teste, como mencionado no ponto anterior.
