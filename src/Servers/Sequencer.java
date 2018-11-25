package Servers;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;

import udp_bridge.*;
import udp_bridge.Process;


public class Sequencer{
	

	private Reliable udp;
	private LinkedList<Message> holdback = new LinkedList<Message>();
	
	private Receiver receiver = new Receiver();
	private Broadcaster broadcaster = new Broadcaster();
	
	public Sequencer(int localport, Process ...processes) throws SocketException {
		this.udp = new Reliable(new Multicast(localport, processes));
	}
	
	public Sequencer(int localport, int...remoteports) throws SocketException {
		this.udp = new Reliable(localport, remoteports);
	}
	
	public void start() {
		receiver.start();
		broadcaster.start();
		
	}
	
	public void stop() {
		receiver.kill();
		broadcaster.kill();
		
	}
	
	
	
	
	private class Receiver extends Thread{
		private boolean running = true;
		@Override
		public void run() {
			while(running) {
				try {
					byte[] m = udp.receive();
					Message message = new Message(m, 0, udp.getLastSender());
					synchronized(holdback) {						
						holdback.add(message);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		public void kill() {
			this.running = false;
		}
		
	}
	
	
	private class Broadcaster extends Thread{
		private boolean running = true;
		@Override
		public void run() {
			while(running) {
				int size = 0;
				synchronized(holdback) {
					size = holdback.size();
				}
				if(size > 0) {
					try {
						synchronized(holdback) {							
							udp.send(holdback.removeFirst().getBytes());
						}
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
			}
		}
		
		public void kill() {
			this.running = false;
		}
	}
	
	
}
