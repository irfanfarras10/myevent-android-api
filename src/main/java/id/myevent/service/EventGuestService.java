package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventGuestDao;
import id.myevent.model.dto.EventGuestDto;
import id.myevent.repository.EventGuestRepository;
import id.myevent.repository.EventRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Event Guest Service.
 */
@Service
@Slf4j
public class EventGuestService {

  @Autowired
  EventGuestRepository eventGuestRepository;
  @Autowired
  EventRepository eventRepository;

  /**
   * Create Event Guest.
   */
  public void create(Long eventId, EventGuestDto guestEvent) {
    final EventDao eventData = eventRepository.findById(eventId).get();
    final EventGuestDao guest = new EventGuestDao();
    //insert guest
    guest.setName(guestEvent.getName());
    guest.setPhoneNumber(guestEvent.getPhoneNumber());
    guest.setEmail(guestEvent.getEmail());
    guest.setAlreadyShared(false);
    guest.setEvent(eventData);
    try {
      eventGuestRepository.save(guest);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
  }

  /**
   * Update Guest.
   */
  public void updateGuest(Long eventId, Long guestId, EventGuestDto guestData) {
    Optional<EventGuestDao> currentGuest = eventGuestRepository.findById(guestId);
    final EventDao eventData = eventRepository.findById(eventId).get();
    EventGuestDao newGuest = currentGuest.get();

    if (eventData.getEventStatus().getId() == 1) {
      if (guestData.getName() != null) {
        newGuest.setName(guestData.getName());
      }
      if (guestData.getEmail() != null) {
        newGuest.setEmail(guestData.getEmail());
      }
      if (guestData.getPhoneNumber() != null) {
        newGuest.setPhoneNumber(guestData.getPhoneNumber());
      }
      newGuest.setEvent(eventData);

      try {
        eventGuestRepository.save(newGuest);
      } catch (DataIntegrityViolationException exception) {
        String exceptionMessage = exception.getMostSpecificCause().getMessage();
        throw new ConflictException(exceptionMessage);
      }
    } else {
      throw new ConflictException("Event harus di status Draft");
    }


  }
}
