package com.axelor.event.service;

import java.math.BigDecimal;

import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.meta.db.MetaFile;

public interface EventRegistrationService {

	public boolean checkRegDate(EventRegistration eventRegistration, Event event);

	public BigDecimal registrationAmount(EventRegistration eventRegistration, Event event);
	
	public void importEventRegistrationData(MetaFile file, Event event);

}
