
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetworkUDPClient {
	private ExecutorService executorService;
	private DatagramSocket datagramSocket;

	public static void main(String args[]) throws IOException {
		for(int i=1; i<=4;i++) {
			NetworkUDPClient client = new NetworkUDPClient(i);
			client.start();
			client.stop();
		}
	}

	public void start() throws IOException {
		startPingPong();
		//startThroughput();
		stop();
	}
	public void stop() {
		executorService.shutdownNow();
	}

	public NetworkUDPClient(int noOfThreads) throws SocketException {
		executorService = Executors.newFixedThreadPool(noOfThreads);
		datagramSocket = new DatagramSocket();
		System.out.print("ThreadCount:"+noOfThreads);
	}

	public double startPingPong() throws IOException {
		long start = System.nanoTime();
		Future<?> future = executorService.submit(new ClientUDPThread(datagramSocket, true));
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		long end = System.nanoTime();
		return end-start;
	}
	
	public double startThroughput() throws IOException {
		long start = System.nanoTime();
		Future<?> future = executorService.submit(new ClientUDPThread(datagramSocket, false));
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		long end = System.nanoTime();
		return end-start;
	}
}

class ClientUDPThread implements Runnable {
	private DatagramSocket connectionSocket;
	private byte[] buffer = new byte[8000];
	private boolean isPingPong;
	InetAddress server;
	int serverPort = 9595;

	private double totalBytes = 8e+9; //8GB
	private int transferBytes = 8000; //8k ==8192
	private byte[] data;

	public ClientUDPThread(DatagramSocket connectionSocket, boolean isPingPong) throws UnknownHostException {
		this.connectionSocket = connectionSocket;
		this.isPingPong = isPingPong;
		this.data = generateData();
		server = InetAddress.getByName("localhost");
	}

	public byte[] generateData() {
		byte[] data = new byte[transferBytes]; //eg : 64 kB
		for(int i=0;i<transferBytes-1;i++) {
			data[i] = 'a';
		}	
		data[transferBytes-1] ='\n';
		return data;
	}

	@Override
	public void run() {
		try {
			//Test data transfer 
			int loop = (int) (totalBytes/transferBytes/2); //2 --> 1 read and 1 write operation
			DatagramPacket packet = new DatagramPacket(data, data.length, server, serverPort);
			
			byte[] stop  = "STOP".getBytes();
			DatagramPacket stopPacket = new DatagramPacket(stop, stop.length, server, serverPort);
			//-------------START-------------------------
			float start = System.nanoTime();
			for (int i=1; i < loop; i++) {
				connectionSocket.send(packet);
				if(isPingPong) {
					packet = new DatagramPacket(buffer, buffer.length);
					connectionSocket.receive(packet);
					buffer = new byte[8000];
				}
			}
			
			connectionSocket.send(stopPacket);
			connectionSocket.send(stopPacket);
			connectionSocket.send(stopPacket);
			
			packet = new DatagramPacket(buffer, buffer.length);
			connectionSocket.receive(packet);
			String received = new String(packet.getData(), 0, packet.getLength());
			
			while(received!=null && received.equals("STOP")) {
				buffer = new byte[8000];
				connectionSocket.send(stopPacket);
				packet = new DatagramPacket(buffer, buffer.length);
				connectionSocket.receive(packet);
				received = new String(packet.getData(), 0, packet.getLength());
			}
			float end = System.nanoTime();
			//-------------STOP-------------------------
			
			float latencyTime = (float) ((end - start)/totalBytes);
			float time = end - start;
			float throughput = (float)totalBytes/time;
			System.out.println("\tLatency: "+latencyTime/16000*1000000+"ms\tThroughput: "+ throughput*1000 + " MB/s "+"  \tBytes:"+totalBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

