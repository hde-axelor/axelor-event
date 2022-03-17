package com.axelor.event.web;

import java.math.BigDecimal;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.service.EventRegistrationService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventRegistartionController {

	@Inject
	EventRegistrationService eventRegistrationService;

	public void eventRegistrationAmount(ActionRequest request, ActionResponse response)throws ValidationException {
		
		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
         Event  event = request.getContext().getParent().asType(Event.class);
		if(event==null) {
    	return;
    }else {
    
    BigDecimal amount = eventRegistrationService.registrationAmount(eventRegistration, event);
		response.setValue("amount", amount);
	}
}
	
    public Object updateRegistration(Object bean,Map<String, Object> values)throws ValidationException{
			    assert bean instanceof EventRegistration;
			    assert values.get("_event") instanceof Event;
			    Event event = (Event) values.get("_event");
			    if ((event.getCapacity() - event.getTotalEntry()) <= 0) {
			      return bean;
			    }

			    EventRegistration eventRegistration = (EventRegistration) bean;
			    eventRegistration.setEvent(event);
			    eventRegistrationService.registrationAmount(eventRegistration ,event);
			    event.setTotalEntry(event.getTotalEntry() + 1);
			    return bean;
			  }
}
