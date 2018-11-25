package udp_bridge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.UnknownHostException;


public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final byte[] message;
	public final int s;
	public final Process process;
	//TODO add address and port and whatnot
	
	public Message(byte[] m, int s, int process) throws UnknownHostException {		
			this(m,s,new Process(process));
	}
	
	public Message(byte[] m, int s, Process process) {
		this.message = m;
		this.s = s;
		this.process = process;
	}
	
	public String toString() {
		return "S: " + s + " P: " + process + " M: " + new String(message);
	}
	
	public byte[] getBytes() {
		try {
			ByteArrayOutputStream ob1 = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(ob1);
			os.writeObject(this);
		
			return ob1.toByteArray();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	
	}
	
	public static Message fromBytes(byte[] m) throws IOException{
		ByteArrayInputStream input = new ByteArrayInputStream(m);
		ObjectInputStream oi = new ObjectInputStream(input);
		try {
			return(Message)oi.readObject();
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
			return null;
		}
	}
}
