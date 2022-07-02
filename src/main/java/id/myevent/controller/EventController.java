package id.myevent.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.CreateEventApiResponse;
import id.myevent.model.apiresponse.ViewEventApiResponse;
import id.myevent.model.apiresponse.ViewEventListApiResponse;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dto.EventDto;
import id.myevent.model.location.Location;
import id.myevent.model.notification.NotificationData;
import id.myevent.service.EventService;
import id.myevent.service.NotificationService;
import id.myevent.util.ImageUtil;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * User REST Controller.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventController {
  @Autowired
  EventService eventService;

  @Autowired
  NotificationService notificationService;

  /**
   * create event.
   */
  @PostMapping(
      value = "/events/create",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<CreateEventApiResponse> createEvent(
      @RequestParam("name") String name,
      @RequestParam("description") String description,
      @RequestParam("dateEventStart") Long dateEventStart,
      @RequestParam("dateEventEnd") Long dateEventEnd,
      @RequestParam("timeEventStart") Long timeEventStart,
      @RequestParam("timeEventEnd") Long timeEventEnd,
      @RequestParam("location") String location,
      @RequestParam("bannerPhoto") MultipartFile bannerPhoto,
      @RequestParam("eventStatusId") Long eventStatusId,
      @RequestParam("eventCategoryId") Long eventCategoryId,
      @RequestParam("eventVenueCategoryId") Long eventVenueCategoryId,
      @RequestParam("eventOrganizerId") Long eventOrganizerId)
      throws IOException {
    EventDto eventData = new EventDto();
    eventData.setName(name);
    eventData.setDescription(description);
    eventData.setDateEventStart(dateEventStart);
    eventData.setDateEventEnd(dateEventEnd);
    eventData.setTimeEventStart(timeEventStart);
    eventData.setTimeEventEnd(timeEventEnd);
    eventData.setVenue(location);
    eventData.setBannerPhoto(bannerPhoto.getBytes());
    eventData.setBannerPhotoType(bannerPhoto.getContentType());
    eventData.setEventStatusId(eventStatusId);
    eventData.setEventCategoryId(eventCategoryId);
    eventData.setEventVenueCategoryId(eventVenueCategoryId);
    eventData.setEventOrganizerId(eventOrganizerId);
    long insertEventId = eventService.insertEvent(eventData);
    return new ResponseEntity<CreateEventApiResponse>(
        new CreateEventApiResponse("Data Tersimpan", insertEventId), HttpStatus.CREATED);
  }

  /**
   * delete event.
   */
  @DeleteMapping("/events/{id}")
  public ResponseEntity<ApiResponse> deleteEvent(@PathVariable("id") Long id) throws IOException {
    eventService.deleteEvent(id);
    return new ResponseEntity<ApiResponse>(
        new ApiResponse("Event Berhasil Dihapus"), HttpStatus.OK);
  }

  /**
   * get all event data.
   */
  @GetMapping("events")
  public ViewEventListApiResponse getEvents() {
    return eventService.getEvents();
  }

  /**
   * get draft event.
   */
  @GetMapping("events/draft")
  public ViewEventListApiResponse getEventDraft() {
    return eventService.getDraftEvent();
  }

  /**
   * get published event.
   */
  @GetMapping("events/published")
  public ViewEventListApiResponse getEventPublished() {
    return eventService.getPublisedEvent();
  }

  /**
   * get live event.
   */
  @GetMapping("events/live")
  public ViewEventListApiResponse getEventLive() {
    return eventService.getLiveEvent();
  }

  /**
   * get passed event.
   */
  @GetMapping("events/passed")
  public ViewEventListApiResponse getEventPassed() {
    return eventService.getPassedEvent();
  }

  /**
   * get cancel event.
   */
  @GetMapping("events/cancel")
  public ViewEventListApiResponse getEventCancel() {
    return eventService.getCancelEvent();
  }

  /**
   * get detail event.
   */
  @GetMapping("events/{id}")
  public ViewEventApiResponse getDetailEvent(@PathVariable("id") Long id) {
    return eventService.getDetailEvent(id);
  }

  /**
   * get event by name.
   */
  @GetMapping("events/name")
  public ViewEventListApiResponse getEventByName(@RequestParam("name") String name) {
    return eventService.getEventByName(name);
  }

  /**
   * get image event.
   */
  @GetMapping(path = {"/events/image/{name}"})
  public ResponseEntity<byte[]> getImage(@PathVariable("name") String name) throws IOException {

    EventDao image = eventService.getImage(name);

    return ResponseEntity.ok()
        .contentType(MediaType.valueOf(image.getBannerPhotoType()))
        .body(ImageUtil.decompressImage(image.getBannerPhoto()));
  }

  /**
   * Edit Event Endpoint.
   */
  @PutMapping("/events/update/{id}")
  public ResponseEntity<ApiResponse> editEvent(
      @PathVariable("id") Long id,
      @RequestParam("name") String name,
      @RequestParam("description") String description,
      @RequestParam("dateEventStart") Long dateEventStart,
      @RequestParam("dateEventEnd") Long dateEventEnd,
      @RequestParam("timeEventStart") Long timeEventStart,
      @RequestParam("timeEventEnd") Long timeEventEnd,
      @RequestParam("location") String location,
      @RequestParam("bannerPhoto") MultipartFile bannerPhoto,
      @RequestParam("eventStatusId") Long eventStatusId,
      @RequestParam("eventCategoryId") Long eventCategoryId,
      @RequestParam("eventVenueCategoryId") Long eventVenueCategoryId,
      @RequestParam("eventOrganizerId") Long eventOrganizerId)
      throws IOException {

    EventDto eventUpdate = new EventDto();
    eventUpdate.setName(name);
    eventUpdate.setDescription(description);
    eventUpdate.setDateEventStart(dateEventStart);
    eventUpdate.setDateEventEnd(dateEventEnd);
    eventUpdate.setTimeEventStart(timeEventStart);
    eventUpdate.setTimeEventEnd(timeEventEnd);
    eventUpdate.setVenue(location);
    eventUpdate.setBannerPhoto(bannerPhoto.getBytes());
    eventUpdate.setBannerPhotoType(bannerPhoto.getContentType());
    eventUpdate.setEventStatusId(eventStatusId);
    eventUpdate.setEventCategoryId(eventCategoryId);
    eventUpdate.setEventVenueCategoryId(eventVenueCategoryId);
    eventUpdate.setEventOrganizerId(eventOrganizerId);
    eventService.eventUpdate(id, eventUpdate);
    return ResponseEntity.ok(new ApiResponse("Event Berhasil di Update"));
  }

  /**
   * Publish Event.
   */
  @PostMapping("/events/{id}/publish")
  public ResponseEntity<ApiResponse> publish(@PathVariable long id) {
    eventService.publish(id);
    return ResponseEntity.ok(new ApiResponse("Event Berhasil Di Publish"));
  }

  @RequestMapping("/send-notification")
  @ResponseBody
  public String sendNotification(@RequestBody NotificationData note, @RequestParam String token)
      throws FirebaseMessagingException {
    return notificationService.sendNotification(note, token);
  }

  @PostMapping("/events/{id}/cancel")
  public ResponseEntity<ApiResponse> ResponseEntity(@PathVariable("id") Long id,
                                                    @RequestBody String message) {
    eventService.cancel(id, message);
    return ResponseEntity.ok(new ApiResponse("Event Berhasil di Cancel"));
  }

}
