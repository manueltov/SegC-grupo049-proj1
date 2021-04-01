*** REALIZADO E TESTADO EM AMBIENTE LINUX  E MACOS***

*** 45678 é o Porto usado para a coneção
*** Todas as keystores têm como password : 456789
*** Existem 3 users criados (João, Rafael, Ana) com as suas respetivas keystores(JoãoStore,RafaelStore,AnaStore) e certificados

**------------------------------------------------------------------------------*

** É possivel fazer a ligação com o RafaelStore e Rafael, com o JoãoStore e João e com AnaStore e Ana, substituindo nos parametros do cliente na keystore e userID respetivamente
** O Servidor usa como parâmetros : <porto> <keystore> <keystore-password>
** O Cliente usa como parâmetros : <ip:porto> <truststore> <keystore> <keystore-password> <clientID>

**------------------------------------------------------------------------------*

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