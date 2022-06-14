package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.dto.EventDto;
import id.myevent.service.EventService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

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
@CrossOrigin @RestController @RequestMapping("/api") @Slf4j
public class EventController {
  @Autowired
  EventService eventService;

  /**
   * create event.
   * 
   * @throws IOException
   */
  @PostMapping(value = "/events/create", consumes = {
      MediaType.MULTIPART_FORM_DATA_VALUE
  })
  public ResponseEntity<ApiResponse> createEvent(
      @RequestParam("name") String name, 
      @RequestParam("description") String description, 
      @RequestParam("dateTimeEventStart") int dateTimeEventStart,
      @RequestParam("dateTimeEventEnd") int dateTimeEventEnd, 
      @RequestParam("location") String location,
      @RequestParam("bannerPhoto") MultipartFile bannerPhoto,
      @RequestParam("dateTimeRegistrationStart") int dateTimeRegistrationStart, 
      @RequestParam("dateTimeRegistrationEnd") int dateTimeRegistrationEnd,
      @RequestParam("eventStatusId") long eventStatusId,
      @RequestParam("eventCategoryId") long eventCategoryId,
      @RequestParam("eventPaymentCategoryId") long eventPaymentCategoryId,
      @RequestParam("eventOrganizerId") long eventOrganizerId
  ) throws IOException {
    EventDto eventData = new EventDto();
    eventData.setName(name);
    eventData.setDescription(description);
    eventData.setDateTimeEventStart(dateTimeEventStart);
    eventData.setDateTimeEventEnd(dateTimeEventEnd);
    eventData.setLocation(location);
    eventData.setBannerPhoto(bannerPhoto.getBytes());
    eventData.setDateTimeRegistrationStart(dateTimeRegistrationStart);
    eventData.setDateTimeRegistrationEnd(dateTimeRegistrationEnd);
    eventData.setEventStatusId(eventStatusId);
    eventData.setEventCategoryId(eventCategoryId);
    eventData.setEventPaymentCategoryId(eventPaymentCategoryId);
    eventData.setEventOrganizerId(eventOrganizerId);
    eventService.insertEvent(eventData);
    return new ResponseEntity<ApiResponse>(
        new ApiResponse("Event Berhasil Dibuat"), HttpStatus.CREATED);
  }
}
