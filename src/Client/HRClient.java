package Client;
//HelloClient.java
import HRSystem.*;
import FrontEnd.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.omg.CORBA.*;
 
 public class HRClient extends Thread{
	public static HRInterface myInterface;
  	static List<Integer> employeeIDs = new ArrayList<Integer>();
  
  public static void main(String args[])
  {
    try{
    	Scanner kb = new Scanner(System.in);
        String id;
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
  	  	System.out.println("Enter Client ID: ");
  	  	id = br.readLine();
  	  	id = CheckManID(id, br);

		//Create a Log for the HR Manager
  	  	FileWriter managerLog = new FileWriter("G:\\workspace\\Project\\src\\ManagerLog\\Manager"+id+".txt", true);
//		FileWriter managerLog = new FileWriter("/Users/Dabu/eclipse-workspace/Project/src/ManagerLog/Manager"+ id +"Log.txt", true);
		
		
		System.out.println("Welcome to the Canada Server " + id);
		String loc = "FE";
		//Connect
		// create and initialize the ORB
					ORB orb = ORB.init(args, null);
					// get the root naming context
					org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
					// Use NamingContextExt instead of NamingContext. This is 
					// part of the Interoperable naming Service.  
					NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
					// resolve the Object Reference in Naming
					myInterface = HRInterfaceHelper.narrow(ncRef.resolve_str(loc));
		managerLog.write("Welcome to the Canada Server " + id);
		managerLog.flush();
		ChooseOption(br,  managerLog, myInterface, id, loc);
	
    }catch (Exception e) {
    	System.out.println("ERROR : " + e) ;
		e.printStackTrace(System.out);
    }
  }
  
  public static void ChooseOption(BufferedReader br, FileWriter managerLog, HRInterface myInterface2, String id, String loc) throws Exception {
		boolean exit = false;
		String firstName, lastName, mailID, projectID;
		int employeeID;
		while(!exit) {
			System.out.println("Choose an option: \n1- Add Manager Record\n2- Add Employee Record\n3- Get Record Counts\n4- Edit Record\n5- Transfer Record\nType 'exit' leave");
			String option = br.readLine();
			
			if(option.equalsIgnoreCase("1")) {
				Project project = new Project();
				
				System.out.println("Create Manager Record: ");
				System.out.println("First Name: ");
				firstName = br.readLine();
				firstName = CheckStringInputs(firstName, br);

				System.out.println("Last Name: ");
				lastName = br.readLine();
				lastName = CheckStringInputs(lastName, br);
				System.out.println("Employee ID (Integer): ");
				String userInput = br.readLine();
				employeeID = Integer.parseInt(CheckEmplID(userInput, br));
				employeeID = DupEmplIDs(employeeID, employeeIDs, br);
				
				System.out.println("Mail ID: ");
				mailID = br.readLine();
				while(!mailID.matches("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$")) {
					System.out.println("Try Mail ID again: ");
					mailID = br.readLine();
				}
				System.out.println("Project ID: ");
				project.setProjectID(br.readLine());
				String newVal = project.getProjectID();
				newVal = ProjCheckID(newVal, br);
				project.setProjectID(newVal);
				System.out.println("Project Client Name: ");
				project.setClientName(br.readLine());
				String pjc = CheckStringInputs(project.getClientName(), br);
				project.setClientName(pjc);
				System.out.println("Project Project Name: ");
				project.setProjectName(br.readLine());
				String pjn = CheckStringInputs(project.getProjectName(), br);
				project.setClientName(pjn);
				

				String result =  myInterface.createMRecord(id, firstName, lastName, employeeID, new StringHolder(mailID), new ProjectHolder(project), loc);		
				
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				System.out.println("Client "+id+": "+result+" at "+timeStamp+"\n");
				managerLog.write("Client "+id+": "+result+" at "+timeStamp+"\n");
				managerLog.flush();
			}else if(option.equalsIgnoreCase("2")) {
				System.out.println("First Name: ");
				firstName = br.readLine();
				firstName = CheckStringInputs(firstName, br);
				System.out.println("Last Name: ");
				lastName = br.readLine();
				lastName = CheckStringInputs(lastName, br);
				System.out.println("Employee ID (Integer): ");
				String userInput = br.readLine();
				employeeID = Integer.parseInt(CheckEmplID(userInput, br));
				employeeID = DupEmplIDs(employeeID, employeeIDs, br);
				
				System.out.println("Mail ID: ");
				mailID = br.readLine();
				while(!mailID.matches("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$")) {
					System.out.println("Try Mail ID again: ");
					mailID = br.readLine();
				}
				System.out.println("Project ID: ");
				projectID = br.readLine();
				String newVal = projectID;
				newVal = ProjCheckID(newVal, br);
				projectID = newVal;
				
				String result =  myInterface.createERecord(id, firstName, lastName, employeeID, new StringHolder(mailID), new StringHolder(projectID));
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				System.out.println("Client "+id+": "+result+" at "+timeStamp+"\n");
				managerLog.write("Client "+id+": "+result+" at "+timeStamp+"\n");
				managerLog.flush();
			}else if(option.equalsIgnoreCase("3")) {
				System.out.println("Number of employees on this server");
				String s = myInterface.getRecordCounts(id);
				System.out.println("Get Record Counts Successfull: \n"+s+"\n");
				managerLog.write("Get Record Counts Successfull: \n"+s+"\n");
				managerLog.flush();
			}else if(option.equalsIgnoreCase("4")) {
				System.out.println("Enter Record ID you want to change: ");
				String recID = br.readLine();
				recID = CheckRecID(recID, br);
				System.out.println("Enter fieldName you want to change: ");
				System.out.println("1- MailID");
				System.out.println("2- Project");
				System.out.println("3- Location");
				int op = Integer.parseInt(br.readLine());
				String fn = "";
				Project project = new Project();
				String newVal = "";
					if(op == 1) {
						fn = "mailID";
						System.out.println("Enter the new value of the field: ");
						newVal = br.readLine();
						while(!newVal.matches("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$")) {
							System.out.println("Try Mail ID again: ");
							newVal = br.readLine();
						}
					}
					else if(op == 2) {
						System.out.println("Select Field: ");
						System.out.println("1- Project ID: ");
						System.out.println("2- Project Client Name: ");
						System.out.println("3- Project Project Name: ");
						int n = Integer.parseInt(br.readLine());
						while(n>3) {
							System.out.println("Choose options between 1 and 3");
							n = Integer.parseInt(br.readLine());
						}
						if(n==1) {
							fn = "projectID";
							System.out.println("Enter the new value of projectID: ");
							newVal = br.readLine();
						}
						else if(n==2) {
							fn = "clientName";
							System.out.println("Enter the new value of Client Name: ");
							newVal = br.readLine();
							newVal = CheckStringInputs(newVal, br);
						}
						else if(n==3) {
							fn = "projectName";
							System.out.println("Enter the new value of Project Name: ");
							newVal = br.readLine();
							newVal = CheckStringInputs(newVal, br);
						}
					}
					else if(op == 3) {
						fn = "location";
						System.out.println("Enter the new Location: ");
						newVal = br.readLine();
						while( !(newVal.startsWith("CA")||newVal.startsWith("UK")||newVal.startsWith("US")) ) {
							System.out.println("Enter the new Location: ");
							newVal = br.readLine();
						}
					}
				
				String result = myInterface.EditRecord(id, recID, fn, new StringHolder(newVal));
				System.out.println(result);
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				managerLog.write("Client "+id+": "+result+" at "+timeStamp+"\n");
				managerLog.flush();
			}else if(option.equalsIgnoreCase("5")) {
				System.out.println("Enter Record ID you want to transfer: ");
				String rID = br.readLine();
				rID = CheckRecID(rID, br);
				System.out.println("Enter Remote Center-Server Name: ");
				String rCSN = br.readLine();
				while( !(rCSN.startsWith("CA")||rCSN.startsWith("UK")||rCSN.startsWith("US")) ) {
					System.out.println("Enter Remote Center-Server Name AGAIN PLEASE: ");
					rCSN = br.readLine();
				}
				String result = myInterface.transferRecord(id, rID, rCSN);
				
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				System.out.println("Client "+id+": "+result+" at "+timeStamp+"\n");
				managerLog.write("Client "+id+": "+result+" at "+timeStamp+"\n");
				managerLog.flush();
			}
			
			if(option.equalsIgnoreCase("exit")) {
				System.exit(0);
			}
		}
		
	}

	//Checks if the user inputs a string (and only a string)!
	private static String CheckStringInputs(String userInput, BufferedReader br) throws IOException {
		while(!userInput.matches("^[a-zA-Z]*$") || userInput.matches("") || userInput.matches("\\s+")) {
			System.out.println("Try again: ");
			userInput = br.readLine();
		}
		return userInput;
	}

  	private static String CheckEmplID(String userInput, BufferedReader br) throws IOException {
	// TODO Auto-generated method stub
  		while( (!userInput.matches("^[0-9]+$")) || userInput.matches("") || userInput.matches("\\s+")) {
			System.out.println("Try again: ");
			userInput = br.readLine();
		}
  		return userInput;
  	}
  	
  	//CHECK if there are duplicate employeeIDs in the arraylist
	private static int DupEmplIDs(int employeeID, List<Integer> employeeIDs, BufferedReader br) throws Exception {
		// TODO Auto-generated method stub
		if(employeeIDs.size() == 0)
			employeeIDs.add(employeeID);
		else {
			//Check if employeeID exists already
			int i =0;
			while(i<employeeIDs.size()){
				if(employeeID == employeeIDs.get(i)) {
					System.out.println("Employee ID Exists, Enter Employee ID Again: ");
					String userInput = br.readLine();
					employeeID = Integer.parseInt(CheckEmplID(userInput, br));
					i=0;
					continue;
				}
				i++;
			}
			employeeIDs.add(employeeID);
		}
		return employeeID;
	}
	
	private static String CheckManID(String newVal, BufferedReader br) throws Exception {
		// TODO Auto-generated method stub
				boolean b = false;
				int trim=0;
				while(!b) {
					try {
						trim = Integer.parseInt(newVal.substring(2, 6));
						b = true;
					}catch(Exception e) {
						System.out.println("Input Invalid, Choose UK, US, or CA Server.");
						System.out.println("Enter Client ID: ");
						newVal = br.readLine();
					}
				}
				while( (!newVal.startsWith("CA") && !newVal.startsWith("UK") && !newVal.startsWith("US")) || newVal.length() != 6) {
					System.out.println("Input Invalid, Choose UK, US, or CA Server.");
					System.out.println("Enter Client ID: ");
					newVal = br.readLine();
				}
				while(!b) {
					try {
						trim = Integer.parseInt(newVal.substring(2, 6));
						b = true;
					}catch(Exception e) {
						System.out.println("Input Invalid, Choose UK, US, or CA Server.");
						System.out.println("Enter Client ID: ");
						newVal = br.readLine();
					}
				}
				return newVal;
	}
	
	private static String ProjCheckID(String newVal, BufferedReader br) throws Exception {
		// TODO Auto-generated method stub
		boolean b = false;
		int trim=0;
		while(!b) {
			try {
				trim = Integer.parseInt(newVal.substring(1, 6));
				b = true;
			}catch(Exception e) {
				System.out.println("Please enter the Project ID as follows 'P' followed by 5 integers");
				System.out.println("Project ID: ");
				newVal = br.readLine();
			}
		}
		while( !(newVal.startsWith("P")) || newVal.length() != 6 ) {
			System.out.println("Please enter the Project ID as follows: Letter 'P' followed by 5 integers");
			System.out.println("Project ID: ");
			newVal = br.readLine();
		}
		while(!b) {
			try {
				trim = Integer.parseInt(newVal.substring(1, 6));
				b = true;
			}catch(Exception e) {
				System.out.println("Please enter the Project ID as follows 'P' followed by 5 integers");
				System.out.println("Project ID: ");
				newVal = br.readLine();
			}
		}
		return newVal;
	}
	
	private static String CheckRecID(String newVal, BufferedReader br) throws Exception {
		// TODO Auto-generated method stub
		boolean b = false;
		int trim=0;
		while(!b) {
			try {
				trim = Integer.parseInt(newVal.substring(2, 7));
				b = true;
			}catch(Exception e) {
				System.out.println("Incorrect Syntax");
				System.out.println("Enter Record ID you want to change: ");
				newVal = br.readLine();
			}
		}
		while( (!newVal.startsWith("MR") && !newVal.startsWith("ER")) || newVal.length() != 7) {
			System.out.println("Incorrect Syntax");
			System.out.println("Enter Record ID you want to change: ");
			newVal = br.readLine();
		}
		while(!b) {
			try {
				trim = Integer.parseInt(newVal.substring(2, 7));
				b = true;
			}catch(Exception e) {
				System.out.println("Incorrect Syntax");
				System.out.println("Enter Record ID you want to change: ");
				newVal = br.readLine();
			}
		}
		return newVal;
	}

	
}