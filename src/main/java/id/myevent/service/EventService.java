package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.apiresponse.ViewEventApiResponse;
import id.myevent.model.apiresponse.ViewEventListApiResponse;
import id.myevent.model.dao.EventCategoryDao;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventOrganizerDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.model.dao.EventVenueCategoryDao;
import id.myevent.model.dto.EventDto;
import id.myevent.repository.EventCategoryRepository;
import id.myevent.repository.EventOrganizerRepository;
import id.myevent.repository.EventPaymentCategoryRepository;
import id.myevent.repository.EventRepository;
import id.myevent.repository.EventStatusRepository;
import id.myevent.repository.EventVenueCategoryRepository;
import id.myevent.repository.TicketRepository;
import id.myevent.task.LiveEventTask;
import id.myevent.task.PassedEventTask;
import id.myevent.task.ReminderOneEventTask;
import id.myevent.task.ReminderThreeEventTask;
import id.myevent.util.GlobalUtil;
import id.myevent.util.ImageUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * User Service.
 */
@Service
@Slf4j
public class EventService {
  @Autowired
  EventStatusRepository eventStatusRepository;
  @Autowired
  EventCategoryRepository eventCategoryRepository;

  @Autowired
  EventVenueCategoryRepository eventVenueCategoryRepository;
  @Autowired
  EventPaymentCategoryRepository eventPaymentCategoryRepository;
  @Autowired
  EventOrganizerRepository eventOrganizerRepository;
  @Autowired
  EventRepository eventRepository;
  @Autowired
  TicketRepository ticketRepository;

  @Autowired
  GlobalUtil globalUtil;

  @Autowired
  TaskScheduler taskScheduler;

  @Autowired
  LiveEventTask liveEventTask;
  @Autowired
  PassedEventTask passedEventTask;
  @Autowired
  ReminderThreeEventTask reminderThreeEventTask;
  @Autowired
  ReminderOneEventTask reminderOneEventTask;

  /**
   * insert event.
   */
  public long insertEvent(EventDto eventData) {
    EventDao newEvent = new EventDao();

    final Optional<EventStatusDao> eventStatus =
        eventStatusRepository.findById(eventData.getEventStatusId());
    final Optional<EventCategoryDao> eventCategory =
        eventCategoryRepository.findById(eventData.getEventCategoryId());
    final Optional<EventVenueCategoryDao> eventVenueCategory =
        eventVenueCategoryRepository.findById(eventData.getEventVenueCategoryId());
    final Optional<EventOrganizerDao> eventOrganizer =
        eventOrganizerRepository.findById(eventData.getEventOrganizerId());

    newEvent.setName(eventData.getName());
    newEvent.setDescription(eventData.getDescription());
    newEvent.setDateTimeEventStart(eventData.getDateTimeEventStart());
    newEvent.setDateTimeEventEnd(eventData.getDateTimeEventEnd());
    newEvent.setVenue(eventData.getVenue());
    newEvent.setBannerPhoto(ImageUtil.compressImage(eventData.getBannerPhoto()));
    newEvent.setBannerPhotoName(generateUniqueImageName());
    newEvent.setBannerPhotoType(eventData.getBannerPhotoType());
    newEvent.setEventStatus(eventStatus.get());
    newEvent.setEventCategory(eventCategory.get());
    newEvent.setEventVenueCategory(eventVenueCategory.get());
    newEvent.setEventOrganizer(eventOrganizer.get());
    try {
      validateEventDataForInsert(eventData);
      eventRepository.save(newEvent);
      return newEvent.getId();
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      String message = null;
      if (exceptionMessage.contains("name")) {
        message = "Event sudah dibuat sebelumnya";
      }
      throw new ConflictException(message);
    }
  }

  /**
   * Delete Event.
   */
  public void deleteEvent(Long id) {

    Optional<EventDao> newEvents = eventRepository.findById(id);

    Long status = newEvents.get().getEventStatus().getId();
    log.warn(status.toString());

    if (status == 1) {
      eventRepository.deleteById(id);
    } else {
      String message = "Event tidak bisa dihapus";
      throw new ConflictException(message);
    }
  }

  /**
   * View Event Draft Data.
   */
  public List<ViewEventListApiResponse> getDraftEvent() {
    List<ViewEventListApiResponse> newEvent = new ArrayList<>();
    List<EventDao> event = eventRepository.findByStatus(1L);

    for (int i = 0; i < event.size(); i++) {
      ViewEventListApiResponse eventData = new ViewEventListApiResponse();

      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateTimeEventStart(event.get(i).getDateTimeEventStart());
      eventData.setDateTimeEventEnd(event.get(i).getDateTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEvent.add(eventData);
    }
    return newEvent;
  }

  /**
   * View Event Published Data.
   */
  public List<ViewEventListApiResponse> getPublisedEvent() {
    List<EventDao> event = eventRepository.findByStatus(2L);
    List<ViewEventListApiResponse> newEvent = new ArrayList<>();

    for (int i = 0; i < event.size(); i++) {
      ViewEventListApiResponse eventData = new ViewEventListApiResponse();
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateTimeEventStart(event.get(i).getDateTimeEventStart());
      eventData.setDateTimeEventEnd(event.get(i).getDateTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEvent.add(eventData);
    }
    return newEvent;
  }

  /**
   * View Event Live Data.
   */
  public List<ViewEventListApiResponse> getLiveEvent() {
    List<EventDao> event = eventRepository.findByStatus(3L);
    List<ViewEventListApiResponse> newEvent = new ArrayList<>();

    for (int i = 0; i < event.size(); i++) {
      ViewEventListApiResponse eventData = new ViewEventListApiResponse();
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateTimeEventStart(event.get(i).getDateTimeEventStart());
      eventData.setDateTimeEventEnd(event.get(i).getDateTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEvent.add(eventData);
    }

    return newEvent;
  }

  /**
   * View Event Passed Data.
   */
  public List<ViewEventListApiResponse> getPassedEvent() {
    List<EventDao> event = eventRepository.findByStatus(4L);
    List<ViewEventListApiResponse> newEvent = new ArrayList<>();

    for (int i = 0; i < event.size(); i++) {
      ViewEventListApiResponse eventData = new ViewEventListApiResponse();
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateTimeEventStart(event.get(i).getDateTimeEventStart());
      eventData.setDateTimeEventEnd(event.get(i).getDateTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEvent.add(eventData);
    }

    return newEvent;
  }

  /**
   * View Event Cancel Data.
   */
  public List<ViewEventListApiResponse> getCancelEvent() {
    List<EventDao> event = eventRepository.findByStatus(5L);
    List<ViewEventListApiResponse> newEvent = new ArrayList<>();

    for (int i = 0; i < event.size(); i++) {
      ViewEventListApiResponse eventData = new ViewEventListApiResponse();
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateTimeEventStart(event.get(i).getDateTimeEventStart());
      eventData.setDateTimeEventEnd(event.get(i).getDateTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEvent.add(eventData);
    }

    return newEvent;
  }

  /**
   * View Detail Event.
   */
  public ViewEventApiResponse getDetailEvent(Long id) {
    ViewEventApiResponse newEvent = new ViewEventApiResponse();
    Optional<EventDao> eventData = eventRepository.findById(id);

    newEvent.setName(eventData.get().getName());
    newEvent.setDescription(eventData.get().getDescription());
    newEvent.setDateTimeEventStart(eventData.get().getDateTimeEventStart());
    newEvent.setDateTimeEventEnd(eventData.get().getDateTimeEventEnd());
    newEvent.setVenue(eventData.get().getVenue());
    newEvent.setBannerPhoto(generateBannerPhotoUrl(eventData.get().getBannerPhotoName()));
    newEvent.setDateTimeRegistrationStart(eventData.get().getDateTimeRegistrationStart());
    newEvent.setDateTimeRegistrationEnd(eventData.get().getDateTimeRegistrationEnd());
    newEvent.setEventStatus(eventData.get().getEventStatus());
    newEvent.setEventCategory(eventData.get().getEventCategory());
    newEvent.setEventVenueCategory(eventData.get().getEventVenueCategory());
    newEvent.setEventPaymentCategory(eventData.get().getEventPaymentCategory());
    newEvent.setEventOrganizer(eventData.get().getEventOrganizer());
    newEvent.setEventContactPerson(eventData.get().getEventContactPersons());
    newEvent.setTicket(eventData.get().getEventTicket());
    newEvent.setEventGuest(eventData.get().getEventGuest());
    newEvent.setEventPayment(eventData.get().getEventPayment());
    return newEvent;
  }

  /**
   * View Event By Name.
   */
  public List<ViewEventListApiResponse> getEventByName(String name) {
    List<EventDao> event = eventRepository.findByName(name);
    List<ViewEventListApiResponse> newEvent = new ArrayList<>();

    for (int i = 0; i < event.size(); i++) {
      ViewEventListApiResponse eventData = new ViewEventListApiResponse();
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateTimeEventStart(event.get(i).getDateTimeEventStart());
      eventData.setDateTimeEventEnd(event.get(i).getDateTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEvent.add(eventData);
    }

    return newEvent;
  }

  /**
   * Update event.
   */
  public void eventUpdate(Long id, EventDto event) {
    Optional<EventDao> currentEvent = eventRepository.findById(id);

    final Optional<EventStatusDao> eventStatus =
        eventStatusRepository.findById(event.getEventStatusId());
    final Optional<EventCategoryDao> eventCategory =
        eventCategoryRepository.findById(event.getEventCategoryId());
    final Optional<EventVenueCategoryDao> eventVenueCategory =
        eventVenueCategoryRepository.findById(event.getEventVenueCategoryId());

    EventDao newEvent = currentEvent.get();
    newEvent.setName(event.getName());
    newEvent.setDescription(event.getDescription());
    newEvent.setDateTimeEventStart(event.getDateTimeEventStart());
    newEvent.setDateTimeEventEnd(event.getDateTimeEventEnd());
    newEvent.setVenue(event.getVenue());
    newEvent.setBannerPhoto(event.getBannerPhoto());
    newEvent.setBannerPhotoName(generateUniqueImageName());
    newEvent.setBannerPhotoType(event.getBannerPhotoType());
    newEvent.setEventStatus(eventStatus.get());
    newEvent.setEventCategory(eventCategory.get());
    newEvent.setEventVenueCategory(eventVenueCategory.get());

    try {
      validateEventDataForUpdate(event);
      eventRepository.save(newEvent);
    } catch (DataIntegrityViolationException e) {
      String exceptionMessage = e.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
  }

  private String generateBannerPhotoUrl(String filename) {
    String url = "";
    url = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    url += "/api/events/image/" + filename;
    return url;
  }

  private String generateUniqueImageName() {
    String filename = "";
    long millis = System.currentTimeMillis();
    String datetime = new Date().toGMTString();
    datetime = datetime.replace(" ", "");
    datetime = datetime.replace(":", "");
    String uuid = UUID.randomUUID().toString();
    filename = uuid + "_" + datetime + "_" + millis;
    return filename;
  }

  /**
   * Get Event Image.
   */
  public EventDao getImage(String imageName) {
    final Optional<EventDao> event = eventRepository.findByImageName(imageName);

    return event.get();
  }

  private void validateEventDataForInsert(EventDto event) {
    if (globalUtil.isBlankString(event.getName())) {
      throw new ConflictException("Nama event harus diisi");
    }
    if (globalUtil.isBlankString(event.getDescription())) {
      throw new ConflictException("Deskripsi event harus diisi");
    }
    if (event.getDateTimeEventStart() == null) {
      throw new ConflictException("Tanggal mulai event harus diisi");
    }
    if (event.getDateTimeEventStart() > event.getDateTimeEventEnd()) {
      throw new ConflictException("Tanggal mulai tidak boleh melebihi tanggal selesai");
    }
    if (event.getDateTimeEventEnd() == null) {
      throw new ConflictException("Tanggal berakhir event harus diisi");
    }
    if (event.getDateTimeEventEnd() < event.getDateTimeEventStart()) {
      throw new ConflictException("Tanggal selesai tidak boleh sebelum dari tanggal mulai");
    }
    if (event.getBannerPhoto() == null) {
      throw new ConflictException("Foto event harus di unggah");
    }
    if (event.getEventStatusId() == null) {
      throw new ConflictException("Status event harus dilpilih");
    }
    if (event.getEventCategoryId() == null) {
      throw new ConflictException("Kategori event harus dilpilih");
    }
    if (event.getEventVenueCategoryId() == null) {
      throw new ConflictException("Jenis tempat event harus dilpilih");
    }
    if (event.getEventOrganizerId() == null) {
      throw new ConflictException("Tidak terdapat event organizer ID");
    }
  }

  private void validateEventDataForUpdate(EventDto event) {
    if (globalUtil.isBlankString(event.getName())) {
      throw new ConflictException("Nama event harus diisi");
    }
    if (globalUtil.isBlankString(event.getDescription())) {
      throw new ConflictException("Deskripsi event harus diisi");
    }
    if (event.getDateTimeEventStart() == null) {
      throw new ConflictException("Tanggal mulai event harus diisi");
    }
    if (event.getDateTimeEventStart() > event.getDateTimeEventEnd()) {
      throw new ConflictException("Tanggal mulai tidak boleh melebihi tanggal selesai");
    }
    if (event.getDateTimeEventEnd() == null) {
      throw new ConflictException("Tanggal berakhir event harus diisi");
    }
    if (event.getDateTimeEventEnd() < event.getDateTimeEventStart()) {
      throw new ConflictException("Tanggal selesai tidak boleh sebelum dari tanggal mulai");
    }
    if (event.getBannerPhoto() == null) {
      throw new ConflictException("Foto event harus di unggah");
    }
    if (event.getEventStatusId() == null) {
      throw new ConflictException("Status event harus dilpilih");
    }
    if (event.getEventCategoryId() == null) {
      throw new ConflictException("Kategori event harus dilpilih");
    }
    if (event.getEventVenueCategoryId() == null) {
      throw new ConflictException("Jenis tempat event harus dilpilih");
    }
    if (event.getEventOrganizerId() == null) {
      throw new ConflictException("Tidak terdapat event organizer ID");
    }
  }

  /**
   * Publish Event.
   */
  public void publish(Long id) {
    // set event status to published
    EventDao event = eventRepository.findById(id).get();
    final EventStatusDao publishedEventStatus = eventStatusRepository.findById(2L).get();
    event.setEventStatus(publishedEventStatus);
    eventRepository.save(event);

    // run schedule for set event status to live
    Date scheduleTime = new Date(event.getDateTimeEventStart());
    log.warn("tanggal event mulai: " + scheduleTime);
    liveEventTask.setEvent(event);
    taskScheduler.schedule(liveEventTask, scheduleTime);
    // TODO: run schedule for set event notification h-3
    long dateThree = (scheduleTime.getTime() + TimeUnit.DAYS.toMillis(-3));
    log.warn(String.valueOf(dateThree));
    // convert to date
    Date dayMinThree = new Date(dateThree);
    log.warn("d-3 event (date): " + dayMinThree);
    reminderThreeEventTask.setEvent(event);
    taskScheduler.schedule(reminderThreeEventTask, dayMinThree);

    // TODO: run schedule for set event notification h-1
    long dateOne = (scheduleTime.getTime() + TimeUnit.DAYS.toMillis(-1));
    log.warn(String.valueOf(dateOne));
    // convert to date
    Date dayMinOne = new Date(dateOne);
    log.warn("d-1 event (date): " + dayMinOne);
    reminderOneEventTask.setEvent(event);
    taskScheduler.schedule(reminderOneEventTask, dayMinOne);

    // TODO: run schedule for set event status to pass
    Date endEventTime = new Date(event.getDateTimeEventEnd());
    log.warn("tanggal event selesai: " + endEventTime);
    passedEventTask.setEvent(event);
    taskScheduler.schedule(passedEventTask, endEventTime);
  }
}
