

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetworkBenchmarkServer {
	private ServerSocket serverSocket;
	private ExecutorService executorService;

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		(new NetworkBenchmarkServer(8)).startServer(9090);
	}

	NetworkBenchmarkServer(int noOfThreads) {
		executorService = Executors.newFixedThreadPool(noOfThreads);
	}

	public void stopServer() {
		executorService.shutdownNow();
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startServer(int port) throws IOException, InterruptedException, ExecutionException {
		startPPServer(port);
		//startTPServer(port);
		stopServer();
	}

	public long startPPServer(int port) throws IOException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		serverSocket = new ServerSocket(port);
		while (true) {
			System.out.println("Listening...");
			// Wait for connection
			Socket connectionSocket = serverSocket.accept();
			Future<?> future = executorService.submit(new TcpPingPongThread(connectionSocket));
			future.get();
			connectionSocket.close();
		}
	}

	public long startTPServer(int port) throws IOException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		serverSocket = new ServerSocket(port);
		while (true) {
			System.out.println("Listening...");
			// Wait for connection
			Socket connectionSocket = serverSocket.accept();
			Future<?> future = executorService.submit(new TcpThroughputThread(connectionSocket));
			future.get();
			connectionSocket.close();
		}
	}
}

class TcpPingPongThread implements Runnable {
	private Socket connectionSocket;

	public TcpPingPongThread(Socket connectionSocket) {
//	System.out.println("Connection accepted");	
	this.connectionSocket = connectionSocket;
	System.out.println("Connection accepted");
	}

	@Override
	public void run() {
		try {
			PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			float start = System.nanoTime();
			int bytecount=0;
			String in;
//System.out.println("Starting data transfer..");
			while ((in = reader.readLine()) != null) {
				//Writing data to client
				if(in.equals("STOP")) { //Keep reading till u find "STOP"
					bytecount++;
					break;
				} else {
					writer.write(in+'\n');
writer.flush();				
	bytecount++;
				}
			}
			float end = System.nanoTime();
			writer.write("STOP:"+bytecount+":"+(end-start)+'\n');
writer.flush();	
		System.out.println("No of 8KBytes: "+bytecount+"\tTime:"+(float)(end-start)/1000000000+" sec");
			writer.flush();
			writer.close();
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1); 
		}
	}
}
class TcpThroughputThread implements Runnable {
	private Socket connectionSocket;

	public TcpThroughputThread(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
	}

	@Override
	public void run() {
		try {
			PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			int bytecount=0;
			String in;

			float start = System.nanoTime();
			while ((in = reader.readLine()) != null) {
				if(in.equals("STOP")) { //Keep reading till u find "STOP"
					bytecount++;
					break;
				}
			}
			float end = System.nanoTime();
			writer.write("STOP:"+bytecount+":"+(end-start)+'\n');
			writer.flush();
			System.out.println("No of 8KBytes: "+bytecount+"\tTime:"+(float)(end-start)/1000000000+" sec");

			writer.close();
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1); 
		}
	}
}
