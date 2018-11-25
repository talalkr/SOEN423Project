package Servers;
import HRSystem.*;
import Records.*;
import udp_bridge.Message;
import udp_bridge.Multicast;
import udp_bridge.Reliable;
import udp_bridge.UDP;
import udp_bridge.Unicast;
import udp_bridge.Process;

import java.lang.*;
import java.lang.Object;

import org.omg.CosNaming.*; // will use for Naming Service 
import org.omg.CosNaming.NamingContextPackage.*; //contains special exceptions thrown by the naming service 
import org.omg.CORBA.*;
import org.omg.PortableServer.*; // Classes required for the Portable server inheritance model
import org.omg.PortableServer.POA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;  // properties to initiate the ORB

class CenterServer implements Runnable {
	private ORB orb;
	HashMap<String, List<Record>> map = new HashMap<String, List<Record>>(); 
	List<Integer> recIDs = new ArrayList<Integer>(); //Will hold the random values (ensure no duplicates)
	boolean found = false; //Used for EditRecord
	String retVal = ""; //Used for EditRecord
	boolean recExists = false; //Used for transferRecord --> CheckRecordID
	int indexL = 0; //Record index in list ---> checkRecordExist()
	String indexM = ""; //Record index in map ---> checkRecordExist()
	Record rec = null; //TO BE TRANSFERED
	String location;
	String id; //ManagerID of current user

	FileWriter serverLog;
	int count = 0;
	int udpPort;
	boolean transf = false;
	
	private UDP udp;
	
	public CenterServer() {}
	
	public CenterServer(String location, int udpPort) {
		this.location = location;
		this.udpPort = udpPort;
	}
	
	public String createMRecord(String args[]) {
		Project p = new Project(args[6],args[7],args[8]);
		return createMRecord(args[1],args[2], args[3], Integer.parseInt(args[4]), new StringHolder(args[5]), new ProjectHolder(p), args[9]);
	}

	public String createMRecord(String managerID, String firstName, String lastName, int employeeID, StringHolder mailID, ProjectHolder project, String location) {
		this.id = managerID;
		Projects p = new Projects(project.value.getProjectID(),project.value.getClientName(),project.value.getProjectName());

		ManagerRecord mr = new ManagerRecord(id, firstName, lastName, employeeID, mailID.value, p, location);

		this.count++;
		
		//Record ID	
		String id = "MR" + genID();
		mr.recordID = id;
		
		this.rec = mr;

		//Save Record ID in Server Log and in console
		LogToServer(mr);
		String recordInfo = DisplayRecord(mr, mr.recordID);
		//Add To Map
		String key = lastName.toUpperCase().charAt(0) + "";
		synchronized(map) {
			if(map.get(key) == null) //If first letter of last name not found
			{
				map.put(key, new ArrayList<Record>(Arrays.asList(mr))); //Add a new arraylist with that key
			}
			else
			{
				map.get(key).add(mr);
			}
		}
		return "Manager Record Created Successfully: " + recordInfo;
	}

	public String createERecord(String args[]) {
		return createERecord(args[1],args[2], args[3], Integer.parseInt(args[4]), new StringHolder(args[5]), new StringHolder(args[6]));
	}
	
	public String createERecord(String managerID, String firstName, String lastName, int employeeID, StringHolder mailID, StringHolder projectID) {
		this.id = managerID;
		// TODO Auto-generated method stub
		EmployeeRecord er = new EmployeeRecord(id, firstName, lastName, employeeID, mailID.value, projectID.value);
		this.count++;
		
		//RecordID
		String id = "ER" + genID();
		er.recordID = id;
		
		this.rec = er;
		
		//Save Record ID in Server Log and in console		
		LogToServer(er);
		
		String recordInfo = DisplayRecord(er, er.recordID);
		
		//Add To Map
		String key = lastName.toUpperCase().charAt(0) + "";
		
		synchronized(map) {
			if(map.get(key) == null)
			{
				map.put(key, new ArrayList<Record>(Arrays.asList(er)));
			}
			else
			{
				map.get(key).add(er);
			}
		}
		
		return "Employee Record Created Successfully: " + recordInfo;
	}
	
	private int genID() {
		// TODO Auto-generated method stub
		double randomId = Math.random()*10000 + 10000;
		double ceilId = Math.ceil(randomId);
		int id = (int)ceilId;
		if(recIDs.size() == 0) {
			recIDs.add(id);
		}
		else {
			//Check if ID exists already
			for (int i = 0; i < recIDs.size(); i++) {
				if(id == recIDs.get(i)) {
					//Recompute ID
					randomId = Math.random()*10000;
					ceilId = Math.ceil(randomId);
					id = (int)ceilId;
					System.out.println("RECORD DUPLICATE FOUND. BREAKING LOOP");
					continue;
				}
			}
			recIDs.add(id);
		}
		return id;
	}
	
	//ONLY log CreateMR and CreateER
	public void LogToServer(Record r) {
		if(r instanceof ManagerRecord) {
			ManagerRecord mr = (ManagerRecord) r;
				try {
					Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
					serverLog.write("Manager Created Successfully at: "+timeStamp + " " + 
						 mr.firstName +"'s record ID: " + mr.recordID + "\n");
					serverLog.flush();
				}catch(Exception e) {
					System.out.println(e.getMessage());
				}
		}
		if(r instanceof EmployeeRecord) {
			EmployeeRecord er = (EmployeeRecord) r;
				try {
					Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
					serverLog.write("Employee Created Successfully: at "+timeStamp+" "+
							er.firstName +"'s record ID: " + er.recordID + "\n");
					serverLog.flush();
				}catch(Exception e) {
					System.out.println(e.getMessage());
				}
		}
	}
	
	public String DisplayRecord(Record mrOrEr, String recordID) {
		System.out.print("Display Record: ");
		String recordInfo = "";
			if(mrOrEr instanceof ManagerRecord) {
				ManagerRecord mr = (ManagerRecord) mrOrEr;
				if(mr.recordID.equals(recordID))
					recordInfo = mr.firstName + mr.lastName + mr.employeeID + mr.mailID + mr.getLocation() + mr.getPj().getClientName() + mr.getPj().getProjectName() + mr.getPj().getProjectID();
				System.out.println(recordInfo + mr.recordID);
			}
			if(mrOrEr instanceof EmployeeRecord){
				EmployeeRecord er = (EmployeeRecord) mrOrEr;
				if(er.recordID.equals(recordID))
					recordInfo = er.firstName + er.lastName + er.employeeID + er.mailID + er.getProjectID();
				System.out.println(recordInfo + er.recordID);
			}
		return recordInfo;
	}
	
	public String EditRecord(String args[]) {
		return EditRecord(args[1], args[2], args[3], new StringHolder(args[4]));
	}
	//TESTED
	public String EditRecord(String managerID, String recordID, String fieldName, StringHolder newValue){
		this.id = managerID;
		found = false;
		//Iterate, Access record through last item (ID)
		synchronized(map) {
			map.forEach((key,value) -> {
				value.forEach((record) -> { //Iterating through List<Record>
					if(record instanceof ManagerRecord) { //Check if record is MR or ER
						ManagerRecord mr = (ManagerRecord) record;
						if(mr.recordID.equals(recordID)) { //Check if RecordID exists
							switch(fieldName) {
								case "mailID":
									mr.mailID = newValue.value;
									break;
								case "projectID":
									mr.getPj().setProjectID(newValue.value);
									break;
								case "clientName":
									mr.getPj().setClientName(newValue.value);
									break;
								case "projectName":
									mr.getPj().setProjectName(newValue.value);
									break;
								case "location":
									mr.setLocation(newValue.value);
									
									break;
							}
							DisplayRecord(mr, mr.recordID);
							LogEditToServer(mr);
							retVal = "Manager Edited Successfully: "+ mr.firstName + mr.lastName + mr.employeeID + mr.mailID + mr.getLocation() + mr.getPj().getClientName() + mr.getPj().getProjectName() + mr.getPj().getProjectID()+"\n";
							found = true;
						}
					}
					if(record instanceof EmployeeRecord){
						EmployeeRecord er = (EmployeeRecord) record;
						if(er.recordID.equals(recordID)) { //Check if RecordID exists
							switch(fieldName) {
								case "mailID":
									er.mailID = newValue.value;
									break;
								case "projectID":
									er.setProjectID(newValue.value);
									break;
							}
							DisplayRecord(er, er.recordID);
							try {
								if( (!fieldName.equals("mailID") && !fieldName.equals("projectID")) ) {
									Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
									serverLog.write("Employee Fail to Edit: Unable To change specified field for Employee Record, Please Choose mailID or Project ID to change"+" at "+timeStamp+"\n");
									serverLog.flush();
									retVal = "Employee Fail to Edit: Unable To change specified field for Employee Record, Please Choose mailID or Project ID to change";
									found = true;//To avoid (record not found failure) because we know that the recordID exists, however, wrong field to edit
								}else {
									LogEditToServer(er);
									retVal = "Manager Edited Successfully: "+ er.firstName + er.lastName + er.employeeID + er.mailID + er.getProjectID();
									found = true;
								}
							}catch(Exception e) {
								System.out.println(e.getMessage());
							}
						}
					}
				});
		
			});
		}
		if(!found) {//Record ID not found
			retVal = "Record not Found"+"\n";
		}
		return retVal;
	}
	
	//ONLY successful edits
	public void LogEditToServer(Record r) {
		if(r instanceof ManagerRecord) {
			ManagerRecord mr = (ManagerRecord) r;
			try {
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				serverLog.write(this.id + ": Manager Edited Successfully at "+timeStamp+ " "+
						mr.firstName + mr.lastName + mr.employeeID + mr.mailID + mr.getLocation() + mr.getPj().getClientName() + mr.getPj().getProjectName() + mr.getPj().getProjectID()+"\n");
				serverLog.flush();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
		if(r instanceof EmployeeRecord) {
			EmployeeRecord er = (EmployeeRecord) r;
			try {
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				serverLog.write(this.id + ": Employee Edited Successfully: at "+timeStamp+ "\n" +
						er.firstName + er.lastName + er.employeeID + er.mailID +er.getProjectID()+"\n");
				serverLog.flush();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public String getRecordCounts(String[] args) {
		return getRecordCounts(args[1]);
	}
	public String getRecordCounts(String managerID) {
		this.id = managerID;
		String s = "";
		if(udpPort == 4001) {
			if(managerID.startsWith("CA")) {
				s = "CA " + this.count + " UK " + UDPClient(4002, "1") + " US " + UDPClient(4003, "1");
			}
			if(managerID.startsWith("UK")) {
				s = "UK " + this.count + " CA " +  UDPClient(4001, "1") + " US " + UDPClient(4003, "1");
			}
			if(managerID.startsWith("US")) {
				s = "US " + this.count + " CA " + UDPClient(4001, "1") +  " UK " + UDPClient(4002, "1");
			}
		}else if(udpPort == 5001) {
			if(managerID.startsWith("CA")) {
				s = "CA " + this.count + " UK " + UDPClient(5002, "1") + " US " + UDPClient(5003, "1");
			}
			if(managerID.startsWith("UK")) {
				s = "UK " + this.count + " CA " +  UDPClient(5001, "1") + " US " + UDPClient(5003, "1");
			}
			if(managerID.startsWith("US")) {
				s = "US " + this.count + " CA " + UDPClient(5001, "1") +  " UK " + UDPClient(5002, "1");
			}
		}else if(udpPort == 6001) {
			if(managerID.startsWith("CA")) {
				s = "CA " + this.count + " UK " + UDPClient(6002, "1") + " US " + UDPClient(6003, "1");
			}
			if(managerID.startsWith("UK")) {
				s = "UK " + this.count + " CA " +  UDPClient(6001, "1") + " US " + UDPClient(6003, "1");
			}
			if(managerID.startsWith("US")) {
				s = "US " + this.count + " CA " + UDPClient(6001, "1") +  " UK " + UDPClient(6002, "1");
			}
		}
		
		try {
			serverLog.write(this.id + ": Get Record Counts Successfull: \n" + s +"\n");
			serverLog.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	
	public synchronized String UDPClient(int serverPort, String command) {
		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket();
			if(command == "1") {
				//Get Record Count
				byte[] m = ("1").getBytes();
				InetAddress host = InetAddress.getByName("localhost");
				//Send Request to the port number provided
				DatagramPacket request = new DatagramPacket(m, m.length , host, serverPort);
				sock.send(request);
				//Receive Reply
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				sock.receive(reply);
				return new String(reply.getData());
			}else if (command == "2") {
				//TransferRecord() - Record ID Check
				byte[] m = ("2"+this.rec.recordID).getBytes();
				InetAddress host = InetAddress.getByName("localhost");
				//Send Request to the port number provided
				DatagramPacket request = new DatagramPacket(m, m.length , host, serverPort);
				sock.send(request);
				//Receive Reply
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				sock.receive(reply);
				return new String(reply.getData());
			}else if (command == "3") {
				//TransferRecord() - Object Transfer
				//Serialize Obj
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        ObjectOutputStream oos = new ObjectOutputStream(baos);
		        oos.writeObject(this.rec);
		        byte[] m = baos.toByteArray();
		        InetAddress host = InetAddress.getByName("localhost");
		        baos.flush();
		        oos.flush();
		        baos.close();
		        oos.close();
				DatagramPacket request = new DatagramPacket(m, m.length , host, serverPort);
				sock.send(request);
				//Receive record transfer reply
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				sock.receive(reply);
				serverLog.flush();
				return new String(reply.getData());
			}
			
		}catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(sock != null) sock.close();
		}
		
		return "Error: Something Went Wrong calling UDPClient()";
		
	}
	
	private void UDPServer(int serverPort) throws Exception{
		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket(serverPort);
			byte[] buffer = new byte[2000];
			while(true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				sock.receive(request);
				
				String data = new String(buffer,0,request.getLength());
				if(data.startsWith("1")) {
					String result = this.count+"";
					byte[] m = result.getBytes();
					DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), 3000);
					sock.send(reply);
				}
				//ADD SAME IMPL FROM A2
			}
		}catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}finally {
			if(sock != null) sock.close();
		}
	}
	
	
//	public String Deserialize(String message) {
//		String[] m = message.split(";");
////		System.out.println(m[0]+m[1]+m[2]+m[3]+m[4]+m[5]+m[6]+m[7]+m[8]+m[9]);
//		String methodNumber = m[0];
//		String id = m[1];
//		String firstName = m[2];
//		String lastName = m[3]; 
//		int employeeID = Integer.parseInt(m[4]);
//		String mailID = m[5];
//		Project proj = new Project();
//		proj.setProjectID(m[6]);
//		proj.setClientName(m[7]);
//		proj.setProjectName(m[8]);
//		String loc = m[9];
//		createMRecord(id, firstName, lastName, employeeID,  new StringHolder(mailID), new ProjectHolder(proj), loc);
//		return id;
//	}
	
	private String AddRecord(byte[] buffer, DatagramSocket sock) {
		 try {
	        	ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
	            ObjectInputStream ois = new ObjectInputStream(bais);
	        	Object readObject = ois.readObject();
	        	if(readObject instanceof Record) {
	            	readObject = (Record) readObject;
	                if (readObject instanceof ManagerRecord) {
	                	ManagerRecord manR = (ManagerRecord) readObject;
                		Project p = new Project(manR.getPj().getProjectID(), manR.getPj().getClientName(), manR.getPj().getProjectName());
                		createMRecord(manR.id, manR.firstName, manR.lastName, manR.employeeID, new StringHolder(manR.mailID), new ProjectHolder(p), manR.getLocation());
                		return "Manager Record Created";
	                }
	                if (readObject instanceof EmployeeRecord) {
	                	EmployeeRecord empR = (EmployeeRecord) readObject;
                		createERecord(empR.id, empR.firstName, empR.lastName, empR.employeeID, new StringHolder(empR.mailID), new StringHolder(empR.getProjectID()));
                		return "Employee Record Created";
	                }
	        	}
			}catch (Exception e) {
		       System.out.println("No object could be read from the received UDP datagram.");
		    }
	        return "ERROR: Something Went Wrong in adding record";
	}
	
	private String AddRecordRemotely(byte[] buffer, DatagramSocket sock) {
		//Deserialize object
        try {
        	ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = new ObjectInputStream(bais);
        	Object readObject = ois.readObject();
        	if(readObject instanceof Record) {
            	readObject = (Record) readObject;
                if (readObject instanceof ManagerRecord) {
                	ManagerRecord manR = (ManagerRecord) readObject;
                	this.rec = manR;
                	boolean exists = false;
    				exists = checkRecordExists(manR.recordID, exists);
    				if(!exists) {
    					//Record DNE on Remote Center Server! So continue execution
	                	Project p = new Project(manR.getPj().getProjectID(), manR.getPj().getClientName(), manR.getPj().getProjectName());
	                	manR.setLocation(this.location);
	                	serverLog.write("Transfer Completed: ");
	                	serverLog.flush();
	                	//Save Record ID in Server Log and in console
	            		LogToServer(manR);
	            		DisplayRecord(manR, manR.recordID);
	                	//Add To Map
	            		String key = manR.lastName.toUpperCase().charAt(0) + "";
	            		synchronized(map) {
		                	if(map.get(key) == null) //If first letter of last name not found
		            		{
		                		System.out.println("Added To Empty Map");
		            			map.put(key, new ArrayList<Record>(Arrays.asList(manR))); //Add a new arraylist with that key
		            		}
		            		else
		            		{
		            			System.out.println("Added To a Non empty Map");
		            			map.get(key).add(manR);
		            		}
	                	}
	                	this.count++;
	                	return "Manager Record Created Remotely";
    				}
                }
                if (readObject instanceof EmployeeRecord) {
                	EmployeeRecord empR = (EmployeeRecord) readObject;
                	this.rec = empR;
                	boolean exists = false;
    				exists = checkRecordExists(empR.recordID, exists); //Checking if the record (RecordID) you sent from US back to CA exists in CA's MAP
    				if(!exists) {
    					//Record DNE on Remote Center Server! So continue execution
		                serverLog.write("Transfer Completed: ");
	                	serverLog.flush();
	                	//Save Record ID in Server Log and in console
	            		LogToServer(empR);
	            		DisplayRecord(empR, empR.recordID);
	                	//Add To Map
	            		String key = empR.lastName.toUpperCase().charAt(0) + "";
	            		synchronized(map) {
		                	if(map.get(key) == null) //If first letter of last name not found
		            		{
		                		System.out.println("Added To Empty Map");
		            			map.put(key, new ArrayList<Record>(Arrays.asList(empR))); //Add a new arraylist with that key
		            		}
		            		else
		            		{
		            			System.out.println("Added To a Non empty Map");
		            			map.get(key).add(empR);
		            		}
	                	}
	                	this.count++;
		                return "Employee Record Created Remotely";
    				}
                }
        	}
            else {
            	//Not meant to be transfered (reply with garbage)
            	//Object is trash
				return "ERROR: Record NOT Created on Remote Server. The record transferred is garbage value.";
            }
	        bais.close();
	        ois.close();
		}catch (Exception e) {
	       System.out.println("No object could be read from the received UDP datagram.");
	    }
        return "ERROR: AddRecordRemotely() - Something Went Wrong in adding record remotely";
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {

			if(udpPort == 4001 || udpPort == 5001 || udpPort == 6001) {
				UDPServer(udpPort);
			}
			else if(udpPort == 4002 || udpPort == 5002 || udpPort == 6002) {
				UDPServer(udpPort);
			}
			else if(udpPort == 4003 || udpPort == 5003 || udpPort == 6003) {
				UDPServer(udpPort);
			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String transferRecord(String[] args) {
		return transferRecord(args[1], args[2], args[3]);
	}
	
	public String transferRecord(String managerID, String recordID, String remoteCenterServerName) {
		this.id = managerID;
		boolean exists = false;
		exists = checkRecordExists(recordID, exists);
		//Record Exists Locally...
		String s = DoTransfer(managerID, exists, remoteCenterServerName);
		return s;
	}
	
	private String DoTransfer(String managerID, boolean recExists, String remoteCenterServerName) {
		String s = "";
		try {
			if(recExists) {
				//Contact UDP Servers
				if(managerID.startsWith("CA") && remoteCenterServerName.equalsIgnoreCase("UK")) {
					//Logging is only here for testing purposes
					s = UDPClient(6001, "2");
//					Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
//					serverLog.write(this.id+": "+ s + " at "+timeStamp+"\n");
//			    	serverLog.flush();
					s = UDPClient(6001, "3");
//					timeStamp = new Timestamp(System.currentTimeMillis());
//					serverLog.write(this.id+": "+ s + " at "+timeStamp+"\n");
//			    	serverLog.flush();
				}
				else if(managerID.startsWith("CA") && remoteCenterServerName.equalsIgnoreCase("US")) {
					s = UDPClient(6002, "2"); //Check If Record Exists
					s = UDPClient(6002, "3"); //Transfer Record
				}
				//----
				else if(managerID.startsWith("UK") && remoteCenterServerName.equalsIgnoreCase("CA")) {
					s = UDPClient(6000, "2");
					s = UDPClient(6000, "3");
				}
				else if(managerID.startsWith("UK") && remoteCenterServerName.equalsIgnoreCase("US")) {
					s = UDPClient(6002, "2");
					s = UDPClient(6002, "3");
				}
				//----
				else if(managerID.startsWith("US") && remoteCenterServerName.equalsIgnoreCase("CA")) {
					s = UDPClient(6000, "2");
					s = UDPClient(6000, "3");
				}
				else if(managerID.startsWith("US") && remoteCenterServerName.equalsIgnoreCase("UK")) {
					s = UDPClient(6001, "2");
					s = UDPClient(6001, "3");
				}
				//----
				else {
					Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
					try {
						serverLog.write(this.id+": Cannot transfer to the same location: at "+timeStamp+"\n");
						serverLog.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return "Cannot transfer to the same location";
				}
				
			}else {
				return "Record Doesn't exist locally";
			}
			//ServerLog
			Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
			serverLog.write(this.id+": Successfully Completed A Transfer: at "+timeStamp+"\n");
	    	serverLog.flush();
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		synchronized(map) {
			//Remove it from local map
			map.get(indexM).remove(indexL);
			//If there are no elements in that ArrayList, delete that key
			if(map.get(indexM).isEmpty()) {
				map.remove(indexM);
				this.count--;
			}
		}

		return "Transfer Successful";
	}
	
	private boolean checkRecordExists(String recordID, boolean recExists) {
		this.recExists = recExists; //Re-Initialize! (False in this case)
		synchronized(map) {
			map.forEach((key,value) -> {
				value.forEach((record) -> { //Iterating through List<Record>
					if(record instanceof ManagerRecord) { //Check if record is MR or ER
						ManagerRecord mr = (ManagerRecord) record;
						if(mr.recordID.equals(recordID)) { //Check if RecordID exists
							this.recExists = true;
							//Get Index of record in List:
							indexL = value.indexOf(mr);
							//Get Index of record in map:
							indexM = key;
							this.rec = (ManagerRecord) mr;
						}
					}
					if(record instanceof EmployeeRecord){
						EmployeeRecord er = (EmployeeRecord) record;
						if(er.recordID.equals(recordID)) { //Check if RecordID exists
							this.recExists = true;
							//Get Index of record in List:
							indexL = value.indexOf(er);
							//Get Index of record in map:
							indexM = key;
							this.rec = (EmployeeRecord) er;
						}
					}
				});
			});
		}
		
		return this.recExists; //Returning the changed value
	}


}