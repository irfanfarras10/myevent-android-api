package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.ViewEventApiResponse;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dto.EventDto;
import id.myevent.service.EventService;
import java.io.IOException;
import java.util.List;

import id.myevent.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** User REST Controller. */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventController {
  @Autowired EventService eventService;

  /**
   * create event.
   */
  @PostMapping(
      value = "/events/create",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<ApiResponse> createEvent(
      @RequestParam("name") String name,
      @RequestParam("description") String description,
      @RequestParam("dateTimeEventStart") Integer dateTimeEventStart,
      @RequestParam("dateTimeEventEnd") Integer dateTimeEventEnd,
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
    eventData.setDateTimeEventStart(dateTimeEventStart);
    eventData.setDateTimeEventEnd(dateTimeEventEnd);
    eventData.setVenue(location);
    eventData.setBannerPhoto(bannerPhoto.getBytes());
    eventData.setBannerPhotoType(bannerPhoto.getContentType());
    eventData.setEventStatusId(eventStatusId);
    eventData.setEventCategoryId(eventCategoryId);
    eventData.setEventVenueCategoryId(eventVenueCategoryId);
    eventData.setEventOrganizerId(eventOrganizerId);
    eventService.insertEvent(eventData);
    return new ResponseEntity<ApiResponse>(
        new ApiResponse("Event Berhasil Dibuat"), HttpStatus.CREATED);
  }

  /**
   * delete event.
   */
  @DeleteMapping("/events/{id}")
  public ResponseEntity<ApiResponse> deleteEvent(@PathVariable("id") Long id) throws IOException{
    eventService.deleteEvent(id);
    return new ResponseEntity<ApiResponse>(
            new ApiResponse("Event Berhasil Dihapus"), HttpStatus.OK);
  }

  /**
   * get draft event.
   */
  @GetMapping("events/draft")
  public List<ViewEventApiResponse> getEventDraft(){
    return eventService.getDraftEvent();
  }

  /**
   * get published event.
   */
  @GetMapping("events/published")
  public List<ViewEventApiResponse> getEventPublished(){
    return eventService.getPublisedEvent();
  }

  /**
   * get live event.
   */
  @GetMapping("events/live")
  public List<ViewEventApiResponse> getEventLive(){
    return eventService.getLiveEvent();
  }

  /**
   * get passed event.
   */
  @GetMapping("events/passed")
  public List<ViewEventApiResponse> getEventPassed(){
    return eventService.getPassedEvent();
  }

  /**
   * get detail event.
   */
  @GetMapping("events/{id}")
  public ViewEventApiResponse getDetailEvent(@PathVariable("id") Long id) {
    return eventService.getDetailEvent(id);
  }

  /**
   * get image event.
   */
  @GetMapping(path = {"/events/image/{name}"})
  public ResponseEntity<byte[]> getImage(@PathVariable("name") String name) throws IOException {

    EventDao image = eventService.getImage(name);

    return ResponseEntity
            .ok()
            .contentType(MediaType.valueOf(image.getBannerPhotoType()))
            .body(ImageUtil.decompressImage(image.getBannerPhoto()));
  }

  /** Edit Event Endpoint. */
  @PutMapping("/events/update/{id}")
  public ResponseEntity<ApiResponse> editEvent(
          @PathVariable("id") Long id,
          @RequestParam("name") String name,
          @RequestParam("description") String description,
          @RequestParam("dateTimeEventStart") Integer dateTimeEventStart,
          @RequestParam("dateTimeEventEnd") Integer dateTimeEventEnd,
          @RequestParam("location") String location,
          @RequestParam("bannerPhoto") MultipartFile bannerPhoto,
          @RequestParam("dateTimeRegistrationStart") Integer dateTimeRegistrationStart,
          @RequestParam("dateTimeRegistrationEnd") Integer dateTimeRegistrationEnd,
          @RequestParam("eventStatusId") Long eventStatusId,
          @RequestParam("eventCategoryId") Long eventCategoryId,
          @RequestParam("eventVenueCategoryId") Long eventVenueCategoryId,
          @RequestParam("eventPaymentCategoryId") Long eventPaymentCategoryId,
          @RequestParam("eventOrganizerId") Long eventOrganizerId)
          throws IOException {

    EventDto eventUpdate = new EventDto();
    eventUpdate.setName(name);
    eventUpdate.setDescription(description);
    eventUpdate.setDateTimeEventStart(dateTimeEventStart);
    eventUpdate.setDateTimeEventEnd(dateTimeEventEnd);
    eventUpdate.setVenue(location);
    eventUpdate.setBannerPhoto(bannerPhoto.getBytes());
    eventUpdate.setBannerPhotoType(bannerPhoto.getContentType());
    eventUpdate.setDateTimeRegistrationStart(dateTimeRegistrationStart);
    eventUpdate.setDateTimeRegistrationEnd(dateTimeRegistrationEnd);
    eventUpdate.setEventStatusId(eventStatusId);
    eventUpdate.setEventCategoryId(eventCategoryId);
    eventUpdate.setEventVenueCategoryId(eventVenueCategoryId);
    if (eventPaymentCategoryId != null) {
      eventUpdate.setEventPaymentCategoryId(eventPaymentCategoryId);
    }
    eventUpdate.setEventOrganizerId(eventOrganizerId);
    eventService.eventUpdate(id, eventUpdate);
    return ResponseEntity.ok(new ApiResponse("Event Berhasil di Update"));
  }
}
