package com.axelor.event.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.mail.MessagingException;

import javax.validation.ValidationException;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.db.repo.EventRegistrationRepository;
import com.axelor.event.exception.ITranslation;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EventServiceImpl implements EventService {
	
	@Inject
	MetaModelRepository metaModelRepository;
	
	@Inject
	TemplateRepository templateRepo;
	
	@Inject 
	MessageService messageService;
	
	@Inject
	EventRegistrationRepository eventRegistrationRepository;
	
	@Inject 
	TemplateMessageService templateMessageService;

  @Override
  public boolean exceedCapacity(Event event) {
    if (event.getEventRegistrations().size() > event.getCapacity()) return true;
    else {
      return false;
    }
  }

  @Override
  public Event setEventDetails(Event event) {

    BigDecimal totalAmount = BigDecimal.ZERO;
    List<EventRegistration> registrations = event.getEventRegistrations();
    for (EventRegistration regestartion : registrations) {
      totalAmount = totalAmount.add(regestartion.getAmount());
    }
    int totalEntry = event.getEventRegistrations().size();
    event.setTotalDiscount(
        event.getEventFees().multiply(new BigDecimal(totalEntry)).subtract(totalAmount));
    event.setTotalEntry(totalEntry);
    event.setAmountCollected(totalAmount);
    return event;
  }

  @Override
  @Transactional
  public void emailSend(Event event, String model) {

    MetaModel metaModel = metaModelRepository.all().filter("self.fullName = ?", model).fetchOne();

    Template template =
        templateRepo.all().filter("self.metaModel = ?", metaModel.getId()).fetchOne();
    if (template == null) {
    	throw new ValidationException(I18n.get(ITranslation.TEMPLATE_MISSING));
    }

    List<EventRegistration> eventRegistrationList = event.getEventRegistrations();
    for (EventRegistration eventRegistration : eventRegistrationList) {
      if (eventRegistration.getEmail() != null && (!eventRegistration.getEmailSent())) {

        try {

          Message message = templateMessageService.generateMessage(event, template);
          messageService.sendByEmail(message);
          eventRegistration.setEmailSent(true);
          eventRegistrationRepository.save(eventRegistration);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        } catch (InstantiationException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (AxelorException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (MessagingException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
