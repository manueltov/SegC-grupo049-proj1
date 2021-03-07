# SC PROJECT #1   -->>  SeiTchiz

SeiTchiz is a portuguese version of Instagram :-) .

## Example
![Alt text](example.gif?raw=true "GIF of an example")

## Developed and tested in linux environment (MAQUINAS DA FCUL) and MacOS

- 45678 o porto usado para a conecao
- "user" o utilizador a utilizar
- "password" a do respetivo "user"

## Installation

COM POLITICAS e com .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

```bash
java -cp bin -Djava.security.manager -Djava.security.policy=server.policy -jar server.jar 45678
```

Para correr o cliente deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

```bash
java -cp bin -Djava.security.manager -Djava.security.policy=client.policy -jar client.jar 127.0.0.1:45678 user password
```


*------------------------------------------------------------------------------*

SEM POLITICAS e sem .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

```bash
java -cp bin Server.SeiTchizServer 45678
```

Para correr o cliente deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

```bash
java -cp bin Client.SeiTchiz 127.0.0.1:45678 user password
```

*------------------------------------------------------------------------------*

COM POLITICAS mas sem os .jar

Para correr o servidor deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

```bash
java -cp bin -Djava.security.manager -Djava.security.policy=server.policy Server.SeiTchizServer 45678
```

Para correr o cliente deve colocar-se na pasta SegC-grupo049-proj1 e correr o seguinte comando:

```bash
java -cp bin -Djava.security.manager -Djava.security.policy=client.policy Client.SeiTchiz 127.0.0.1:45678 user password
```

*------------------------------------------------------------------------------*

NO ECLIPSE E COM POLITICAS

1. Importar a pasta do projeto

2. Ir a classe .java do servidor a SeiTchizServer e fazer : run as -> Run configuration -> Arguments -> 45678  nos Argumentos e	na VM -> -Djava.security.manager -Djava.security.policy=server.policy

3. Fazer o mesmo na classe .java do cliente a SeiTchiz e fazer : run as -> Run configuration -> Arguments -> 127.0.0.1:45678 user password nos Argumentos e na VM -> -Djava.security.manager -Djava.security.policy=client.policy

## Usage

Feita a conecao, sera visualizada uma lista de comandos que o Client pode efectuar ao seu gosto.

Para desconectar o Client, basta escrever "stop"

## Coded by
Grupo049
- Joao Miranda n. 47143
- Manuel Tovar n. 49522
- Manuel Lopes n. 49023
