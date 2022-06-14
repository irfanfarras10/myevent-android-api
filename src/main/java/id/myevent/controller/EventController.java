package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.dto.EventDto;
import id.myevent.service.EventService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
      @RequestParam("dateTimeRegistrationStart") Integer dateTimeRegistrationStart,
      @RequestParam("dateTimeRegistrationEnd") Integer dateTimeRegistrationEnd,
      @RequestParam("eventStatusId") Long eventStatusId,
      @RequestParam("eventCategoryId") Long eventCategoryId,
      @RequestParam("eventVenueCategoryId") Long eventVenueCategoryId,
      @RequestParam(value = "eventPaymentCategoryId", required = false) Long eventPaymentCategoryId,
      @RequestParam("eventOrganizerId") Long eventOrganizerId)
      throws IOException {
    EventDto eventData = new EventDto();
    eventData.setName(name);
    eventData.setDescription(description);
    eventData.setDateTimeEventStart(dateTimeEventStart);
    eventData.setDateTimeEventEnd(dateTimeEventEnd);
    eventData.setVenue(location);
    eventData.setBannerPhoto(bannerPhoto.getBytes());
    eventData.setDateTimeRegistrationStart(dateTimeRegistrationStart);
    eventData.setDateTimeRegistrationEnd(dateTimeRegistrationEnd);
    eventData.setEventStatusId(eventStatusId);
    eventData.setEventCategoryId(eventCategoryId);
    eventData.setEventVenueCategoryId(eventVenueCategoryId);
    if (eventPaymentCategoryId != null) {
      eventData.setEventPaymentCategoryId(eventPaymentCategoryId);
    }
    eventData.setEventOrganizerId(eventOrganizerId);
    eventService.insertEvent(eventData);
    return new ResponseEntity<ApiResponse>(
        new ApiResponse("Event Berhasil Dibuat"), HttpStatus.CREATED);
  }
}
