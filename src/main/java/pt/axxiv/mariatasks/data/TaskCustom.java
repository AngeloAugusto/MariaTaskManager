package pt.axxiv.mariatasks.data;

import java.time.LocalTime;

import org.bson.types.ObjectId;

public class TaskCustom extends Task{
	
	private int period;
	private FrequencyTypes frequencyTypes;

	public TaskCustom() {
		super();
	}
	
	public TaskCustom(String title, String notes, ObjectId section, ObjectId user, LocalTime localTime) {
		super(title, notes, section, user, localTime);
	}
	
	public TaskCustom(String title, String notes, int period, FrequencyTypes frequencyTypes, ObjectId section, ObjectId user, LocalTime localTime) {
		super(title, notes, section, user, localTime);
		this.period=period;
		this.frequencyTypes=frequencyTypes;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public FrequencyTypes getFrequencyTypes() {
		return frequencyTypes;
	}

	public void setFrequencyTypes(FrequencyTypes frequencyTypes) {
		this.frequencyTypes = frequencyTypes;
	}
	
	
}
