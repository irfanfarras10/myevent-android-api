package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.dao.EventContactPersonDao;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventSocialMediaDao;
import id.myevent.model.dto.EventContactPersonDto;
import id.myevent.repository.EventContactPersonRepository;
import id.myevent.repository.EventRepository;
import id.myevent.repository.EventSocialMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Event Contact Person Service. */
@Service
public class EventContactPersonService {
  @Autowired EventSocialMediaRepository eventSocialMediaRepository;

  @Autowired EventRepository eventRepository;

  @Autowired EventContactPersonRepository eventContactPersonRepository;

  /** Create Event Social Media. */
  public void create(Long eventId, EventContactPersonDto eventData) {
    final EventSocialMediaDao eventSocialMedia =
        eventSocialMediaRepository.findById(eventData.getEventSocialMediaId()).get();
    final EventDao event = eventRepository.findById(eventId).get();

    EventContactPersonDao eventContactPerson = new EventContactPersonDao();

    eventContactPerson.setName(eventData.getName());
    eventContactPerson.setContact(eventData.getContact());
    eventContactPerson.setEvent(event);
    eventContactPerson.setEventSocialMedia(eventSocialMedia);

    try {
      eventContactPersonRepository.save(eventContactPerson);
    } catch (Exception e) {
      throw new ConflictException("Terjadi kesalahan pada saat menyimpan data");
    }
  }

  /** Update Event Social Media. */
  public void update(Long eventId, Long cpId, EventContactPersonDto cpData){
    Optional<EventContactPersonDao> currentCP = eventContactPersonRepository.findById(cpId);
    EventContactPersonDao newCP = currentCP.get();
    final EventDao event = eventRepository.findById(eventId).get();

    if(cpData.getName()!=null){
      newCP.setName(cpData.getName());
    }
    if(cpData.getContact()!=null){
      newCP.setContact(cpData.getContact());
    }
    if(cpData.getEventSocialMediaId()!=null){
      final Optional<EventSocialMediaDao> eventSocialMedia =
              eventSocialMediaRepository.findById(cpData.getEventSocialMediaId());
      newCP.setEventSocialMedia(eventSocialMedia.get());
    }
    newCP.setEvent(event);

    try {
      eventContactPersonRepository.save(newCP);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
  }
}
