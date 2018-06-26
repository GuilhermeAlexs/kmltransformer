package models;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TrailType implements Serializable{
	WELL_KNOWN(1), BARELY_KNOWN(2), UNKNOWN(3);
	
	@JsonProperty
	private int value;
	
	private TrailType(int val){
		this.value = val;
	}
	
	public int getValue(){
		return this.value;
	}
}