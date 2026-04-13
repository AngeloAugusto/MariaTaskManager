package pt.axxiv.mariatasks.data;

import java.util.Objects;

import org.bson.types.ObjectId;

public class Section implements Comparable<Section> {

	private ObjectId id;
	private String title;
	private String icon;
	private ObjectId ownerId;
	
	public Section(){}
	
	public Section(String title, String icon, ObjectId ownerId) {
		this.title=title;
		this.icon=icon;
		this.ownerId=ownerId;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIcon() {
		if (icon==null) {
			icon = "z-icon-image";
		}
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public ObjectId getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(ObjectId ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public int compareTo(Section arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof Section)) return false;
	    Section section = (Section) o;
	    return Objects.equals(id, section.id);
	}

	@Override
	public int hashCode() {
	    return Objects.hash(id);
	}
}
