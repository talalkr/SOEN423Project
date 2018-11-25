package udp_bridge;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Unreliable implements UDP {
	private UDPState udp;
	private Process process;
	private double transmission = 50;
	private double reception = 50;
	
	
	public Unreliable(double transmission, double reception, int localport, int ...remoteports) throws SocketException, UnknownHostException {
		this(transmission, reception, localport, new Multicast(localport, remoteports));
	}
	
	
	public Unreliable(double transmission, double reception, int localport, int remoteport) throws SocketException, UnknownHostException {
		this(transmission,reception, localport, new Unicast(localport,remoteport));
	}
	
	
	public Unreliable(double transmission, double reception, int localport, UDPState udp) throws UnknownHostException {
		this.process = new Process("localhost", localport);
		this.udp = udp;
		this.transmission = transmission;
		this.reception = reception;
	}
	
	public Process getLocalProcess() {
		return process;
	}

	@Override
	public void send(String message) throws IOException {
		
		this.send(message.getBytes());
	}

	int count = 0;
	@Override
	public void send(byte[] message) throws IOException {
		if(Math.random()*100 > transmission) {
			this.udp.send(message);
		}
		if(Math.random()*100 > transmission) {
			this.udp.send(message);
		}
		/*System.out.println(count);
		if(count != 0) {
			System.out.println(count);
			this.udp.send(message);
		}
		count ++;*/
		
	}

	@Override
	public String listen() throws IOException {
		
		return new String(receive());
	}

	@Override
	public byte[] receive() throws IOException {
		if(Math.random()*100 > reception) {
			return this.udp.receive();
		}
		return null;
	}


	@Override
	public void changeRemote(Process... processes) {
		// TODO Auto-generated method stub
		
	}
	
	

}
