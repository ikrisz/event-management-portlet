package com.pfiks.intelligus.events.model.event;

public class EventOrganizer {

    private String organizerId;
    private String name;
    private String description;

    public String getOrganizerId() {
	return organizerId;
    }

    public void setOrganizerId(final String organizerId) {
	this.organizerId = organizerId;
    }

    public String getName() {
	return name;
    }

    public void setName(final String name) {
	this.name = name;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(final String description) {
	this.description = description;
    }

}
