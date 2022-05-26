import java.io.*;
import java.net.*;
import java.util.*;

	final class HttpRequest implements Runnable {
	//Carriage Return + Line Feed
	final static String CRLF = "\r\n";
	//referência do socket da conexão
	Socket socket;
	//senha para autenticacao de pasta protegida
	private String senhaParaAutenticacao = "redes:computadores";
	//Construtor
		public HttpRequest (Socket socket) throws Exception{
		this.socket = socket;
	}
	// roda processrequest e trata as exceções
	public void run(){
		try{
			processRequest();
		}
		catch (Exception e) {
			System.out.println (e);
		}
			}
		
		private void processRequest () throws Exception{
			//Objeto isr referência para os trechos de entrada
			InputStreamReader isr = new
			InputStreamReader(socket.getInputStream());
			//Objeto dos referência para os trechos de saida
			DataOutputStream dos = new
			DataOutputStream(socket.getOutputStream());
			//Ajustar os filtros do trecho de entrada
			BufferedReader br = new BufferedReader(isr);
			//Obter a linha de requisição da mensagem de requisição HTTP
			String requestLine = br.readLine();
			//Exibir a linha de requisição no Console
			System.out.println(); // pula uma linha
			System.out.println(requestLine);
			String headerLine = null;
			StringTokenizer senhaAutorizada = null;
			boolean ehAutenticada = false;
			boolean ehRestrito = false;
			//Dados que irao compor o Log
			String log = requestLine +
			System.getProperty("line.separator");
			//Percorre todas linhas da mensagem
			while ((headerLine = br.readLine()).length() != 0) {
				//Obtendo linhas do cabecalho para log
				log = log + (headerLine +
				System.getProperty("line.separator"));
				//pega a linha que possui a senha
				if(headerLine.contains("Authorization: Basic")){
					senhaAutorizada = new
					StringTokenizer(headerLine);
					//Pula "Authorization: Basic"
					senhaAutorizada.nextToken();
					senhaAutorizada.nextToken();
					//Pega senha na base64
					String senha = senhaAutorizada.nextToken();
				//Decodifica senha na base64
				if(Base64Coder.decodeString(senha).equals
				(this.senhaParaAutenticacao)){
					ehAutenticada = true;
				}
				}
				System.out.println (headerLine);
			}
			//Extrair o nome do arquivo a linha de requisição
			StringTokenizer requisicao = new
			StringTokenizer(requestLine);
			//pula método mostrado na requisicao (GET, POST)
			String metodo = requisicao.nextToken();
			String arquivo = requisicao.nextToken();
			//Acrescente um "." de modo que a requisição do arquivo
			//esteja dentro do dirtório atual
			arquivo = "." + arquivo;
			//Abre o arquivo requisitado
			FileInputStream fis = null;
			//Verifica existencia do arquivo
			
			boolean existeArq = true;
			//Construir a mensagem de resposta
			String linhaStatus = null;
			String linhaContentType = null;
			String msgHtml = null;
			//tratamento quando a requisicao for GET
			if (metodo.equals("GET")){
				try{
					fis = new FileInputStream(arquivo);
				}
				catch(FileNotFoundException e){
					existeArq = false;
				}
			//Verifica se o arquivo é RESTRITO, forma colocadas
			//apenas
			//duas verificações de permissão (RESTRITO, restrito)
			if (arquivo.contains("RESTRITO")||
				arquivo.contains("restrito")){
				ehRestrito = true;
			}
			//Verifica se é restrito o local que está sendo requisitado
			//informações e se não foi autenticado
			if((ehRestrito) && (ehAutenticada) == false){
				linhaStatus = "HTTP/1.0 401 Unauthorized" + CRLF;
				linhaContentType = "WWW-Authenticate: Basic realm=\"RESTRITO\""+
				CRLF;
				msgHtml = "<HTML><HEAD><TITLE> Acesso Nao Autorizado " +
				"</TITLE></HEAD>" +
				"<BODY> Acesso Nao Autorizado </BODY></HTML>";
				existeArq = false;
			}
			else{
			if(existeArq){
				linhaStatus = "HTTP/1.0 200 OK" + CRLF;
				linhaContentType = "Content-type: " +
				contentType(arquivo)+
				CRLF;
			}
			else{
				linhaStatus = "HTTP/1.0 404 Not found" +
				CRLF;
				linhaContentType = "Content-type: " +
				contentType(arquivo)+
				CRLF;
				msgHtml = "<HTML><HEAD><TITLE> Arquivo Nao Encontrado" +
				"</TITLE></HEAD>" + "<BODY> Arquivo Nao Encontrado </BODY></HTML>";
			}
			}
				//Enviar a linha de status
				dos.writeBytes(linhaStatus);
				//Enviar linha de tipo de conteúdo
				dos.writeBytes(linhaContentType);
				//Enviar uma linha em branco para indicar o fim das linhas de
				//cabeçalho
				dos.writeBytes(CRLF);
				//Enviar corpo do Html
			}
			//tratamento quando a requisicao for POST
			else if (metodo.equals("POST")){
				//Obtem o corpo do pacote POST
				char[] buffer = new char[2048];
				String corpo = "";
				String corpoPost = "";
			while(br.ready()){
				int i;
				if((i = br.read(buffer)) > 0)
				corpo = corpo + (new String(buffer,0,i) +
				"\n");
				corpoPost = corpoPost + (new String(buffer,0,i) +
				"<BR>");
				System.out.println(corpo);
				}
				corpo = "CORPO ENVIADO PELO POST: " + corpo;
				log = log + corpo;
				msgHtml = "<HTML><HEAD><TITLE> MENSAGEM POST</TITLE></HEAD>" +
				"<BODY> MENSAGEM ENVIADA PELO POST: </BR>" + corpoPost
				+
				"</BODY></HTML>";
				existeArq = false;
			}
			if(existeArq){
				sendBytes(fis, dos);
				fis.close();
			}
			else{
				dos.writeBytes(msgHtml);
			}
			Log(dos, log, socket);
			dos.close();
			br.close();
			socket.close();
		}
		private void sendBytes(FileInputStream fis, DataOutputStream os)
			throws Exception {
				//Construir um buffer de 1k para comportar os bytes no caminho para
				//o socket
				byte[] buffer = new byte[1024];
				int bytes = 0;
				//Copiar o arquivo requisitado dentro da cadeia de saída do
				//socket
				//enquanto o arquivo não estiver no fim, ou seja, -1..copie
				while((bytes = fis.read(buffer)) != -1){
				os.write(buffer, 0, bytes);
				}
			}
		
		private static String contentType(String arquivo){
			if(arquivo.endsWith(".htm")||arquivo.endsWith(".html")|| arquivo.endsWith(".txt")) return "text/html";
				if(arquivo.endsWith(".gif")) return "image/gif";
			if(arquivo.endsWith(".jpeg")) return "image/jpeg";
			//caso a extensão do arquivo seja desconhecida
			return "application/octet-stream";
			}
		private void Log(DataOutputStream dos, String log, Socket socket) {
			try{
				//Data de requisicao
				Date date = new Date(System.currentTimeMillis());
				String dataRequisicao = date.toString();
				String pulaLinha =
				System.getProperty("line.separator");
				FileWriter fw = new FileWriter("arquivo_de_log.txt",
				true);
				fw.write("------------------------------------------------------" +
				pulaLinha);
				fw.write("Data de Requisicao: " + dataRequisicao + "GMT " +
				pulaLinha);
				fw.write("ENDEREÇO DE ORIGEM:PORTA: " +
				socket.getLocalSocketAddress().toString() +
				pulaLinha);
				fw.write("Conteúdo Requisitado: "+ log + pulaLinha);
				fw.write("Quantidade de bytes transmitidos: " +
				dos.size() +
				pulaLinha);
				fw.write("------------------------------------------------------" +
				pulaLinha);
				fw.write(pulaLinha);
				fw.close();
			}
			catch(IOException io){
				System.out.println(io.getMessage());
			}
	}
}
