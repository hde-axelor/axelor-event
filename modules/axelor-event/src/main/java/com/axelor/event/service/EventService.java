package com.axelor.event.service;

import com.axelor.event.db.Event;

public interface EventService {

	public boolean exceedCapacity(Event event);

	public Event setEventDetails(Event event);
	
	 public void emailSend(Event event, String model);
}
