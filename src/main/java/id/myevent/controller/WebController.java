package id.myevent.controller;

import id.myevent.model.apiresponse.ViewEventApiResponse;
import id.myevent.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import id.myevent.model.apiresponse.DateEvent;

@CrossOrigin
@RestController
@RequestMapping("/web")
@Slf4j
public class WebController {

  @Autowired
  EventService eventService;

  /**
   * get detail event.
   */
  @GetMapping("events/{id}")
  public ViewEventApiResponse getDetailEvent(@PathVariable("id") Long id) {
    return eventService.getDetailEvent(id);
  }
  
   /**
   * get event date.
   */
  @GetMapping("/events/{eventId}/dates")
  public DateEvent getEventDate(@PathVariable("eventId") Long eventId) {
    return eventService.getListDate(eventId);
  }

}
