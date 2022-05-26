import java.net.*;

public class webServer {
	
	//método que executa o inicio do programa
	public static void main (String arvg[]) throws Exception{
	//Porta 8181, escolhida sem motivo especial
	int porta = 8181;
	//Estabelece o socket de escuta do Servidor
	ServerSocket socketServ = new ServerSocket(porta);
	//Estabelece o socket do cliente
	Socket socketCli;
	//Processar a requisicao do servico HTTP em um laco infinito
	while (true) {
	//Apenas um mensagem para indicar que o servidor
	//encontra-se ativo
	System.out.println( "Servidor Ativo" );
	//Escutar requisicao de conexao TCP.
	socketCli = socketServ.accept();
	//Constroi um objeto para processar a mensagem de
	//requisicao HTTP
	HttpRequest requisicao = new HttpRequest(socketCli);
	//Criado um novo thread para processar as novas
	//requisicões
	Thread thread = new Thread (requisicao);
	//Inicia o thread.
	thread.start();
	}
	}
	}

}
