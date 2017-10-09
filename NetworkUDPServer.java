
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetworkUDPServer {
	private DatagramSocket serverSocket;
	private ExecutorService executorService;
	private static int port = 9595;

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		(new NetworkUDPServer(3)).startServer(port);
		//(new NetworkUDPServer(3)).startServer(port);
	}

	NetworkUDPServer(int noOfThreads) {
		executorService = Executors.newFixedThreadPool(noOfThreads);
	}

	public void stopServer() {
		executorService.shutdownNow();
	}

	public void startServer(int port) throws IOException, InterruptedException, ExecutionException {
		startPingPongServer(port);
		//startThroughputServer(port);
		stopServer();
	}

	public void startThroughputServer(int port) throws InterruptedException, ExecutionException, SocketException {
		// TODO Auto-generated method stub
		serverSocket = new DatagramSocket(port);
		System.out.println("Listening...");
		UDPThread thread = new UDPThread(serverSocket, false);
		Future<?> future = executorService.submit(thread);
		future.get();
		serverSocket.close();
	}
	
	public void startPingPongServer(int port) throws InterruptedException, ExecutionException, SocketException {
		// TODO Auto-generated method stub
		serverSocket = new DatagramSocket(port);
		System.out.println("Listening...");
		UDPThread thread = new UDPThread(serverSocket, true);
		Future<?> future = executorService.submit(thread);
		future.get();
		serverSocket.close();
	}
}

class UDPThread implements Runnable {
	private DatagramSocket serverSocket;
	private boolean isPingPong = false;
	private boolean run;
	private byte[] buffer = new byte[8000];

	public UDPThread(DatagramSocket serverSocket, boolean isPingPong) {
		this.serverSocket = serverSocket;
		this.isPingPong = isPingPong;
	}

	@Override
	public void run() {
		run = true;
		int datacount = 0;
		float start = System.nanoTime();
		while(run) {
			buffer = new byte[8000];
			DatagramPacket bufferIn = new DatagramPacket(buffer, buffer.length);
			try {
				serverSocket.receive(bufferIn);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			byte[] data = bufferIn.getData();
			InetAddress address = bufferIn.getAddress();
			int port = bufferIn.getPort();
			
			String dataIn = new String(data, 0, bufferIn.getLength());
			if (dataIn.equals("end")) {
				run = false;
				float end = System.nanoTime();
				String replyMsg = "end"+ (end-start);
				byte[] reply = replyMsg.getBytes();
				
				DatagramPacket sendBack = new DatagramPacket(reply, reply.length,address, port);
				try {
					serverSocket.send(sendBack);
					serverSocket.send(sendBack);
					serverSocket.send(sendBack);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("1. No of 8KBytes Received: "+datacount+"\tTime:"+(float)(end-start)/1000000000+" sec");
				continue;
			} else if (isPingPong){
				DatagramPacket sendBack = new DatagramPacket(data, data.length,address, port);
				try {
					serverSocket.send(sendBack);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		float end = System.nanoTime();
		System.out.println("2. No of 8KBytes Received: "+datacount+"\tTime:"+(float)(end-start)/1000000000+" sec");
	}
}
