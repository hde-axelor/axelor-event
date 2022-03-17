package com.axelor.event.web;

import java.util.LinkedHashMap;
import java.util.List;

import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.db.repo.EventRepository;
import com.axelor.event.exception.IExceptionMessage;
import com.axelor.event.service.EventRegistrationService;
import com.axelor.event.service.EventService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventController {

	@Inject
	EventService eventService;
	
	@Inject
	EventRepository eventRepository;

	@Inject
	EventRegistrationService eventRegistrationService;

	public void eventCapicityValidation(ActionRequest request, ActionResponse response) {

		Event event = request.getContext().asType(Event.class);

		if (eventService.exceedCapacity(event)) {

			response.setError(I18n.get(IExceptionMessage.EXCEEDS_CAPACITY));
		} else {
			List<EventRegistration> eventRegistrations = event.getEventRegistrations();
			for (EventRegistration rg : eventRegistrations) {
				if (!eventRegistrationService.checkRegDate(rg, event)) {
					response.setError(I18n.get(IExceptionMessage.REGISTRATION_DATE_INVALID));
				}
				event = eventService.setEventDetails(event);
			}

			response.setValues(event);
		}
	}

	public void getEventSummary(ActionRequest request, ActionResponse response) {

		Event event = request.getContext().asType(Event.class);

		event = eventService.setEventDetails(event);

		response.setValue("totalDiscount", event.getTotalDiscount());
		response.setValue("totalEntry", event.getTotalEntry());
		response.setValue("amountCollected", event.getAmountCollected());

	}

	public void importFile(ActionRequest request, ActionResponse response) {
		
		Long id = Long.valueOf(request.getContext().get("_id").toString());

	    Event event = eventRepository.find(id);

	    @SuppressWarnings("unchecked")
	    LinkedHashMap<String, Object> map =
	        (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
	    MetaFile dataFile =
	        Beans.get(MetaFileRepository.class).find(((Integer) map.get("id")).longValue());

	    if (!dataFile.getFileType().equals("text/csv")) {
	      response.setError("Only .csv formats are Accepted!!!");
	      return;
	    }

	    response.setAlert("Total Imports Can be: " + (event.getCapacity() - event.getTotalEntry()));
	    eventRegistrationService.importEventRegistrationData(dataFile, event);
	    response.setFlash("Data Imported!!!");
	    response.setReload(true);
	  }
	
	public void emailSend(ActionRequest request ,  ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		eventService.emailSend(event, request.getModel());
	}
}
