package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.apiresponse.EventData;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
  @Autowired
  JavaMailSender javaMailSender;

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
    newEvent.setDateEventStart(eventData.getDateEventStart());
    newEvent.setDateEventEnd(eventData.getDateEventEnd());
    newEvent.setTimeEventStart(eventData.getTimeEventStart());
    newEvent.setTimeEventEnd(eventData.getTimeEventEnd());
    newEvent.setVenue(eventData.getVenue());
    newEvent.setBannerPhoto(ImageUtil.compressImage(eventData.getBannerPhoto()));
    newEvent.setBannerPhotoName(generateUniqueImageName(eventData.getBannerPhotoType()));
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
   * View Event Data.
   */
  public ViewEventListApiResponse getEvents() {

    List<EventDao> eventDraft = (List<EventDao>) eventRepository.findAllByOrderByIdAsc();
    List<EventData> newEventData = new ArrayList<>();
    ViewEventListApiResponse newEvent = new ViewEventListApiResponse();

    for (int i = 0; i < eventDraft.size(); i++) {
      EventData eventData = new EventData();

      eventData.setId(eventDraft.get(i).getId());
      eventData.setName(eventDraft.get(i).getName());
      eventData.setDescription(eventDraft.get(i).getDescription());
      eventData.setDateEventStart(eventDraft.get(i).getDateEventStart());
      eventData.setDateEventEnd(eventDraft.get(i).getDateEventEnd());
      eventData.setTimeEventStart(eventDraft.get(i).getTimeEventStart());
      eventData.setTimeEventEnd(eventDraft.get(i).getTimeEventEnd());
      eventData.setVenue(eventDraft.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(eventDraft.get(i).getBannerPhotoName()));
      eventData.setEventStatus(eventDraft.get(i).getEventStatus());
      eventData.setEventCategory(eventDraft.get(i).getEventCategory());
      eventData.setEventVenueCategory(eventDraft.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(eventDraft.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(eventDraft.get(i).getEventOrganizer());

      newEventData.add(eventData);
    }

    newEvent.setEventDataList(newEventData);
    return newEvent;
  }

  /**
   * View Event Draft Data.
   */
  public ViewEventListApiResponse getDraftEvent() {

    List<EventDao> eventDraft = eventRepository.findByStatusOrderByIdAsc(1L);
    List<EventData> newEventData = new ArrayList<>();
    ViewEventListApiResponse newEvent = new ViewEventListApiResponse();

    for (int i = 0; i < eventDraft.size(); i++) {
      EventData eventData = new EventData();

      eventData.setId(eventDraft.get(i).getId());
      eventData.setName(eventDraft.get(i).getName());
      eventData.setDescription(eventDraft.get(i).getDescription());
      eventData.setDateEventStart(eventDraft.get(i).getDateEventStart());
      eventData.setDateEventEnd(eventDraft.get(i).getDateEventEnd());
      eventData.setTimeEventStart(eventDraft.get(i).getTimeEventStart());
      eventData.setTimeEventEnd(eventDraft.get(i).getTimeEventEnd());
      eventData.setVenue(eventDraft.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(eventDraft.get(i).getBannerPhotoName()));
      eventData.setEventStatus(eventDraft.get(i).getEventStatus());
      eventData.setEventCategory(eventDraft.get(i).getEventCategory());
      eventData.setEventVenueCategory(eventDraft.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(eventDraft.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(eventDraft.get(i).getEventOrganizer());

      newEventData.add(eventData);
    }

    newEvent.setEventDataList(newEventData);
    return newEvent;
  }

  /**
   * View Event Published Data.
   */
  public ViewEventListApiResponse getPublisedEvent() {

    List<EventDao> event = eventRepository.findByStatusOrderByIdAsc(2L);
    List<EventData> newEventData = new ArrayList<>();
    ViewEventListApiResponse newEvent = new ViewEventListApiResponse();

    for (int i = 0; i < event.size(); i++) {
      EventData eventData = new EventData();

      eventData.setId(event.get(i).getId());
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateEventStart(event.get(i).getDateEventStart());
      eventData.setDateEventEnd(event.get(i).getDateEventEnd());
      eventData.setTimeEventStart(event.get(i).getTimeEventStart());
      eventData.setTimeEventEnd(event.get(i).getTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEventData.add(eventData);
    }

    newEvent.setEventDataList(newEventData);
    return newEvent;
  }

  /**
   * View Event Live Data.
   */
  public ViewEventListApiResponse getLiveEvent() {

    List<EventDao> event = eventRepository.findByStatusOrderByIdAsc(3L);
    List<EventData> newEventData = new ArrayList<>();
    ViewEventListApiResponse newEvent = new ViewEventListApiResponse();

    for (int i = 0; i < event.size(); i++) {
      EventData eventData = new EventData();

      eventData.setId(event.get(i).getId());
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateEventStart(event.get(i).getDateEventStart());
      eventData.setDateEventEnd(event.get(i).getDateEventEnd());
      eventData.setTimeEventStart(event.get(i).getTimeEventStart());
      eventData.setTimeEventEnd(event.get(i).getTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEventData.add(eventData);
    }

    newEvent.setEventDataList(newEventData);
    return newEvent;
  }

  /**
   * View Event Passed Data.
   */
  public ViewEventListApiResponse getPassedEvent() {
    List<EventDao> event = eventRepository.findByStatusOrderByIdAsc(4L);
    List<EventData> newEventData = new ArrayList<>();
    ViewEventListApiResponse newEvent = new ViewEventListApiResponse();

    for (int i = 0; i < event.size(); i++) {
      EventData eventData = new EventData();

      eventData.setId(event.get(i).getId());
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateEventStart(event.get(i).getDateEventStart());
      eventData.setDateEventEnd(event.get(i).getDateEventEnd());
      eventData.setTimeEventStart(event.get(i).getTimeEventStart());
      eventData.setTimeEventEnd(event.get(i).getTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEventData.add(eventData);
    }

    newEvent.setEventDataList(newEventData);
    return newEvent;
  }

  /**
   * View Event Cancel Data.
   */
  public ViewEventListApiResponse getCancelEvent() {
    List<EventDao> event = eventRepository.findByStatusOrderByIdAsc(5L);
    List<EventData> newEventData = new ArrayList<>();
    ViewEventListApiResponse newEvent = new ViewEventListApiResponse();

    for (int i = 0; i < event.size(); i++) {
      EventData eventData = new EventData();

      eventData.setId(event.get(i).getId());
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateEventStart(event.get(i).getDateEventStart());
      eventData.setDateEventEnd(event.get(i).getDateEventEnd());
      eventData.setTimeEventStart(event.get(i).getTimeEventStart());
      eventData.setTimeEventEnd(event.get(i).getTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEventData.add(eventData);
    }
    newEvent.setEventDataList(newEventData);
    return newEvent;
  }

  /**
   * View Detail Event.
   */
  public ViewEventApiResponse getDetailEvent(Long id) {
    ViewEventApiResponse newEvent = new ViewEventApiResponse();
    Optional<EventDao> eventData = eventRepository.findById(id);

    newEvent.setId(eventData.get().getId());
    newEvent.setName(eventData.get().getName());
    newEvent.setDescription(eventData.get().getDescription());
    newEvent.setDateEventStart(eventData.get().getDateEventStart());
    newEvent.setDateEventEnd(eventData.get().getDateEventEnd());
    newEvent.setTimeEventStart(eventData.get().getTimeEventStart());
    newEvent.setTimeEventEnd(eventData.get().getTimeEventEnd());
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
  public ViewEventListApiResponse getEventByName(String name) {
    List<EventDao> event = eventRepository.findByName(name);
    List<EventData> newEventData = new ArrayList<>();
    ViewEventListApiResponse newEvent = new ViewEventListApiResponse();

    for (int i = 0; i < event.size(); i++) {
      EventData eventData = new EventData();
      eventData.setName(event.get(i).getName());
      eventData.setDescription(event.get(i).getDescription());
      eventData.setDateEventStart(event.get(i).getDateEventStart());
      eventData.setDateEventEnd(event.get(i).getDateEventEnd());
      eventData.setTimeEventStart(event.get(i).getTimeEventStart());
      eventData.setTimeEventEnd(event.get(i).getTimeEventEnd());
      eventData.setVenue(event.get(i).getVenue());
      eventData.setBannerPhoto(generateBannerPhotoUrl(event.get(i).getBannerPhotoName()));
      eventData.setEventStatus(event.get(i).getEventStatus());
      eventData.setEventCategory(event.get(i).getEventCategory());
      eventData.setEventVenueCategory(event.get(i).getEventVenueCategory());
      eventData.setEventPaymentCategory(event.get(i).getEventPaymentCategory());
      eventData.setEventOrganizer(event.get(i).getEventOrganizer());
      newEventData.add(eventData);
    }

    newEvent.setEventDataList(newEventData);
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
    newEvent.setDateEventStart(event.getDateEventStart());
    newEvent.setDateEventEnd(event.getDateEventEnd());
    newEvent.setTimeEventStart(event.getTimeEventStart());
    newEvent.setTimeEventEnd(event.getTimeEventEnd());
    newEvent.setVenue(event.getVenue());
    if(event.getBannerPhoto() != null){
      newEvent.setBannerPhoto(ImageUtil.compressImage(event.getBannerPhoto()));
      newEvent.setBannerPhotoName(generateUniqueImageName(event.getBannerPhotoType()));
      newEvent.setBannerPhotoType(event.getBannerPhotoType());
    }
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

  private String generateUniqueImageName(String imageFormat) {
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
    Date scheduleTime = new Date(event.getTimeEventStart());
    log.warn("tanggal event mulai: " + scheduleTime);
    liveEventTask.setEvent(event);
    taskScheduler.schedule(liveEventTask, scheduleTime);

    // run schedule for set event notification h-3
    long dateThree = (scheduleTime.getTime() + TimeUnit.DAYS.toMillis(-3));
    log.warn(String.valueOf(dateThree));
    // convert to date
    Date dayMinThree = new Date(dateThree);
    log.warn("d-3 event (date): " + dayMinThree);
    reminderThreeEventTask.setEvent(event);
    taskScheduler.schedule(reminderThreeEventTask, dayMinThree);

    // run schedule for set event notification h-1
    long dateOne = (scheduleTime.getTime() + TimeUnit.DAYS.toMillis(-1));
    log.warn(String.valueOf(dateOne));
    // convert to date
    Date dayMinOne = new Date(dateOne);
    log.warn("d-1 event (date): " + dayMinOne);
    reminderOneEventTask.setEvent(event);
    taskScheduler.schedule(reminderOneEventTask, dayMinOne);

    // run schedule for set event status to pass
    Date endEventTime = new Date(event.getTimeEventEnd());
    log.warn("tanggal event selesai: " + endEventTime);
    passedEventTask.setEvent(event);
    taskScheduler.schedule(passedEventTask, endEventTime);
  }


}
