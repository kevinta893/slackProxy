

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple redirecting proxy server. Does not retrive replies.
 * @author Kevin
 *
 */
public class ProxyServer {

	public static final int INCREMENT = 1;

	private static ServerSocket listenSock;


	private static boolean running = true;						//server running or not


	//threads
	private static Thread acceptThread;

	//private static Date startDate = new Date(System.currentTimeMillis());

	private static ProxyServer instance;

	public static ProxyServer getInstance(){
		if (instance == null){
			instance = new ProxyServer();
		}
		return instance;
	}


	private ProxyServer(){}


	/**
	 * Starts the server, binds all resources. If an instance has already is or has been
	 * running, then nothing happens.
	 */
	public void startServer(){

		//run only if the current thread is not created. Single instance
		if (acceptThread == null){

			println("Starting proxy server on port " + Config.getPort() + "...");
			System.out.println("\n=====================================================");



			//create the listen socket
			try {
				listenSock = new ServerSocket(Config.getPort());

			} catch (BindException e){
				System.err.println(e.getMessage());
				System.err.println("Cannot setup server! Bind Exception. Quitting...");
				System.exit(-1);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.err.println("Cannot setup server! Unknown Host. Quitting...");
				System.exit(-1);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Cannot setup server! IO Exception. Quitting...");
				System.exit(-1);
			}


		}



		acceptThread = new Thread(new SocketAccepter(listenSock));
		acceptThread.start();
		println("Proxy server now running on port " + Config.getPort() + ". Redirecting commands to: " + Config.getRedirectURL());
	}


	//====================================================================================================
	//The request handler


	/**
	 * The request handler created when a new request is being made.
	 * @author Kevin
	 *
	 */
	private final class RequestHandler implements Runnable{

		Socket client;

		public RequestHandler(Socket client){
			this.client = client;
		}

		@Override
		public void run() {

			try {
				
				InetAddress addr = InetAddress.getByName(Config.getRedirectURL());
				Socket redir = new Socket(addr, Config.getPort());
				
				
				//copy the message for output
				InputStream inMessage = client.getInputStream();
				OutputStream outMessage = redir.getOutputStream();
				
				byte[] buffer = new byte[1024]; // Adjust if you want
			   	int bytesRead;
			    while ((bytesRead = inMessage.read(buffer)) != -1)
			    {
			        outMessage.write(buffer, 0, bytesRead);
			    }
				
			    
			    //send the message
			    outMessage.flush();
			    
				//always close the client
			    redir.close();
			    
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


			





		}
	}


	/**
	 * The socket accepting thread that accepts all connections and attempts
	 * to service them.
	 * @author Kevin
	 *
	 */
	private final class SocketAccepter implements Runnable{

		private ServerSocket serverSocket;

		public SocketAccepter(ServerSocket serv){
			this.serverSocket = serv;
		}

		@Override
		public void run() {

			while( running == true ){
				try {
					Socket client = serverSocket.accept();

					//got a connection
					println("Recieved connection from: " + client.getInetAddress().toString() + ":" + client.getPort());

					//handle request in new thread
					Thread clientHandler = new Thread(new RequestHandler(client));
					clientHandler.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}


			try {
				listenSock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}


	//================================================================
	//Reporting methods and console print

	private static SimpleDateFormat consoleDate = new SimpleDateFormat("HH:mm:ss");

	private static String timeStamp(){
		return "[" + (consoleDate.format(new Date(System.currentTimeMillis()))) + "]: ";
	}

	/**
	 * Prints a line in the server. Time stamped
	 */
	public static void println(String message){
		System.out.println(timeStamp() + message);
	}

}
