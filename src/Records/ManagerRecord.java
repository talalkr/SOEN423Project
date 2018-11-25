package Records;

import java.io.Serializable;

import org.omg.CORBA.StringHolder;

import HRSystem.Project;
import HRSystem.ProjectHolder;

public class ManagerRecord extends Record implements Serializable {

	private String location;
	private Projects proj;
	
	public ManagerRecord() {
		super();
		this.location = "";
	}

	public ManagerRecord(String id, String firstName, String lastName, int employeeID, String mailID, Projects project, String location) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.employeeID = employeeID;
		this.mailID = mailID;
		this.location = location;
		this.proj = project;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Projects getPj() {
		return proj;
	}

	public void setPj(Projects pj) {
		this.proj = pj;
	}
	
	
}
