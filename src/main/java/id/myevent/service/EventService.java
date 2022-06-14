package id.myevent.service;

import id.myevent.model.dao.EventCategoryDao;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventOrganizerDao;
import id.myevent.model.dao.EventPaymentCategoryDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.model.dto.EventDto;
import id.myevent.repository.EventCategoryRepository;
import id.myevent.repository.EventOrganizerRepository;
import id.myevent.repository.EventPaymentCategoryRepository;
import id.myevent.repository.EventRepository;
import id.myevent.repository.EventStatusRepository;
import id.myevent.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** User Service. */
@Service
@Slf4j
public class EventService {
  @Autowired EventStatusRepository eventStatusRepository;
  @Autowired EventCategoryRepository eventCategoryRepository;
  @Autowired EventPaymentCategoryRepository eventPaymentCategoryRepository;
  @Autowired EventOrganizerRepository eventOrganizerRepository;
  @Autowired EventRepository eventRepository;
  /** insert event. */
  public void insertEvent(EventDto eventData) {
    EventDao newEvent = new EventDao();

    Optional<EventStatusDao> eventStatus =
        eventStatusRepository.findById(eventData.getEventStatusId());
    Optional<EventCategoryDao> eventCategory =
        eventCategoryRepository.findById(eventData.getEventCategoryId());
    Optional<EventPaymentCategoryDao> eventPaymentCategory =
        eventPaymentCategoryRepository.findById(eventData.getEventPaymentCategoryId());
    Optional<EventOrganizerDao> eventOrganizer =
        eventOrganizerRepository.findById(eventData.getEventOrganizerId());

    newEvent.setName(eventData.getName());
    newEvent.setDescription(eventData.getDescription());
    newEvent.setDateTimeEventStart(eventData.getDateTimeEventStart());
    newEvent.setDateTimeEventEnd(eventData.getDateTimeEventEnd());
    newEvent.setLocation(eventData.getLocation());
    newEvent.setBannerPhoto(ImageUtil.compressImage(eventData.getBannerPhoto()));
    newEvent.setDateTimeRegistrationStart(eventData.getDateTimeRegistrationStart());
    newEvent.setDateTimeRegistrationEnd(eventData.getDateTimeRegistrationEnd());
    newEvent.setEventStatus(eventStatus.get());
    newEvent.setEventCategory(eventCategory.get());
    newEvent.setEventPaymentCategory(eventPaymentCategory.get());
    newEvent.setEventOrganizer(eventOrganizer.get());
    // TODO: validasi
    eventRepository.save(newEvent);
  }
}
