

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetworkBenchmarkClient {
	private ExecutorService executorService;

	NetworkBenchmarkClient(int noOfThreads) {
		executorService = Executors.newFixedThreadPool(noOfThreads);
		System.out.print("ThreadCount:"+noOfThreads);
	}
	public static void main(String args[]) throws IOException {
		for(int i=0; i<4;i++) {
			int num = (int) Math.pow(2, i);
			NetworkBenchmarkClient client = new NetworkBenchmarkClient(num);
			client.startPingPong();
			//client.startThroughput();
			client.stop();
		}
	}

	public void stop() {
		executorService.shutdownNow();
	}

	public double startPingPong() throws IOException {
		String server = new String ("127.0.0.1");
		Socket socket = new Socket(server, 9090);
		long start = System.nanoTime();
		Future<?> future = executorService.submit(new ClientTcpPPThread(socket));
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		long end = System.nanoTime();
		socket.close();
		return end-start;
	}
	
	public double startThroughput() throws IOException {
		String server = new String ("127.0.0.1");
		Socket socket = new Socket(server, 9090);
		long start = System.nanoTime();
		Future<?> future = executorService.submit(new ClientTcpTPThread(socket));
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		long end = System.nanoTime();
		socket.close();
		return end-start;
	}
}

class ClientTcpPPThread implements Runnable {
	private Socket connectionSocket;
	private double totalBytes = 8e+9; //8GB
	private int transferBytes = 8000; //8k ==8192
	private char[] data;

	public ClientTcpPPThread(Socket connectionSocket/*, double bytes*/) {
		this.connectionSocket = connectionSocket;
		//this.totalBytes = bytes;
		this.data = generateData();
	}

	public char[] generateData() {
		char[] data = new char[transferBytes]; //eg : 64 kB
		for(int i=0;i<transferBytes-1;i++) {
			data[i] = 'a';
		}	
		data[transferBytes-1] ='\n';
		return data;
	}

	@Override
	public void run() {
		try {
			PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			//Test data transfer 
			int loop = (int) (totalBytes/transferBytes/2); //2 --> 1 read and 1 write operation
			long start = System.nanoTime();
//System.out.println("Wrote first byte");			
writer.write(data);
			writer.flush();
//System.out.println("Readiing");
			reader.readLine();
//System.out.println("Read comlete");			
long endLatency  = System.nanoTime();
			for (int i=1; i < loop; i++) {
				writer.write(data);
				writer.flush();
				reader.readLine();
			}
			long end = System.nanoTime();
			float latencyTime = endLatency - start;
			float time = end - start;
			float throughput = (float)totalBytes/time;
			System.out.println("\tLatency: "+latencyTime/(transferBytes*2)+"ns\tThroughput: "+ throughput*1000/8 + " GigaBits/s "+"  \tBits:"+totalBytes/8);
			reader.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ClientTcpTPThread implements Runnable {
	private Socket connectionSocket;
	private double totalBytes = 8e+9; //8GB
	private int transferBytes = 8000; //8k ==8192
	private String data;

	public ClientTcpTPThread(Socket connectionSocket/*, double bytes*/) {
		this.connectionSocket = connectionSocket;
		this.data = generateData();
	}

	public String generateData() {
		StringBuffer data = new StringBuffer(transferBytes); //eg : 64 kB
		for(int i=0;i<transferBytes-1;i++) {
			data.append('a');
		}	
		data.append('\n');
		return data.toString();
	}

	@Override
	public void run() {
		try {
			PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			String stopData = "STOP\n";
			//Test data transfer 
			int loop = (int) (totalBytes/transferBytes); //1 --> 1 read operation
			long start = System.nanoTime();
			long endLatency  = System.nanoTime();
			for (int i=0; i < loop; i++) {
				writer.write(data);
				writer.flush();
			}
			writer.write(stopData);
			writer.flush();
			String in;
			while((in = reader.readLine()) != null && in.startsWith("STOP")) {
				
			}
			long end = System.nanoTime();
			float latencyTime = endLatency - start;
			float time = end - start;
			float throughput = (float)totalBytes/time;
			System.out.println("\tLatency: "+latencyTime/(transferBytes*2)+"ns\tThroughput: "+ throughput*1000/8 + " GigaBits/s "+"  \tBits:"+totalBytes/8);
			reader.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
