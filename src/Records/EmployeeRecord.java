
package Records;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import org.omg.CORBA.StringHolder;

import java.io.Serializable;
import java.rmi.*;

public class EmployeeRecord extends Record implements Serializable {

	private String projectID;
	
	public EmployeeRecord() {
		super();
		// TODO Auto-generated constructor stub
	}

	public EmployeeRecord(String id, String firstName, String lastName, int employeeID, String mailID, String projectID) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.employeeID = employeeID;
		this.mailID = mailID;
		this.projectID = projectID;
	}


	public String getProjectID() {
		return projectID;
	}

	public void setProjectID(String projectID) {
		this.projectID = projectID;
	}
	
	
}
