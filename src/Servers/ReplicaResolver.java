package Servers;

import udp_bridge.Message;
import udp_bridge.Reliable;
import udp_bridge.UDP;
import udp_bridge.Unicast;

public class ReplicaResolver implements Runnable{
	
	private CenterServer caCenter;
	private CenterServer usCenter;
	private CenterServer ukCenter;
	
	private String name;
	private UDP udp;

	
	public ReplicaResolver(String name, Reliable udp, CenterServer caCenter, CenterServer usCenter, CenterServer ukCenter) {
		super();
		this.name =name;
		this.udp = udp;
		
		this.caCenter = caCenter;
		this.usCenter = usCenter;
		this.ukCenter = ukCenter;
	}
	
	public void run() {
		UDPServer();
	}
	
	public void UDPServer() {
		try {
			while(true) {
				Message data = Message.fromBytes(this.udp.receive());
				
				this.udp.changeRemote(data.process);
				
				String[] req = (new String(data.message)).split(";");
				
				String res = "Failed";
				
				if(req[1].startsWith("CA"))
					res = processRequest(caCenter, req);
				else if(req[1].startsWith("UK"))
					res = processRequest(usCenter, req);
				else if(req[1].startsWith("US"))
					res = processRequest(ukCenter, req);

				System.out.println("WE OUTCHEA!");
				this.udp.send(req[1]+";"+ res + ";" + this.name);
			}
		}catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	
	private String processRequest(CenterServer current, String[] req) {
		String res = "Failed";
		try {
			String s = req[0];	
			System.out.println("S is: " + s);
			switch(s) {
			case "2":
				System.out.println("Creating Employee Record");
				res = current.createERecord(req);
				break;
			case "1":
				System.out.println("Creating Manager Record");
				res = current.createMRecord(req);
				break;
			case "4":
				System.out.println("Editing Record");
				res = current.EditRecord(req);
				break;
			case "3":
				res = current.getRecordCounts(req);
				break;
			case "5":
				res = current.transferRecord(req);
				break;
			default:
				res = "Failed";
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return res;
	}
}
