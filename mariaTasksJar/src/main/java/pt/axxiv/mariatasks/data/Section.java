package pt.axxiv.mariatasks.data;

import org.bson.types.ObjectId;

public class Section implements Comparable<Section> {

	private ObjectId id;
	private String title;
	private String imgPath;
	private ObjectId ownerId;
	
	public Section(){}
	
	public Section(String title, String imgPath, ObjectId ownerId) {
		this.title=title;
		this.imgPath=imgPath;
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

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
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
}
