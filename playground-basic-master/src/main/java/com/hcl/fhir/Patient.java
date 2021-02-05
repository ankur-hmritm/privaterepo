package com.hcl.fhir;

import java.util.Date;

public class Patient {

	private String firstName;
	
	private String lastName;
	
	private Date dob;
	
	public Patient(String firstName, String lastName, Date dob) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public Date getDob() {
		return dob;
	}
	
	public void setDob(Date dob) {
		this.dob = dob;
	}
	
}
