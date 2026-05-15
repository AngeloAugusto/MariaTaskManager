package dev.axxiv.mariaTasksApi.dto;

import pt.axxiv.mariatasks.data.Section;

public class SectionDTO {
	private String id;
	private String title;
	private String icon;
	private String ownerId;
    
    // Constructor from Task entity
    public static SectionDTO fromSection(Section section) {
        SectionDTO dto = new SectionDTO();
        dto.setId(section.getId() == null ? null : section.getId().toString());
        dto.setTitle(section.getTitle() == null ? null : section.getTitle());
        dto.setIcon(section.getIcon() == null ? null : section.getIcon());
        dto.setOwnerId(section.getOwnerId() == null ? null : section.getOwnerId().toString());
        return dto;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

    
}
