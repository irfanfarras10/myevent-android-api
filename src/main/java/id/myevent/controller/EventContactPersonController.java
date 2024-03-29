package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.dto.EventContactPersonDto;
import id.myevent.service.EventContactPersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Event Contact Person Controller.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventContactPersonController {
  @Autowired
  EventContactPersonService eventContactPersonService;

  /**
   * Create Contact Person.
   */
  @PostMapping("/events/{id}/contact-person/create")
  public ResponseEntity create(
      @PathVariable Long id,
      @RequestBody EventContactPersonDto eventContactPersonDto) {
    eventContactPersonService.create(id, eventContactPersonDto);
    return new ResponseEntity(new ApiResponse("Contact Person Tersimpan"), HttpStatus.CREATED);
  }

  /**
   * Update Contact Person.
   */
  @PutMapping("/events/{eventId}/contact-person/{cpId}")
  public ResponseEntity<ApiResponse> updateCp(@PathVariable("eventId") Long eventId,
                                              @PathVariable("cpId") Long cpId,
                                              @RequestBody
                                              EventContactPersonDto eventContactPersonDto) {
    eventContactPersonService.update(eventId, cpId, eventContactPersonDto);
    return new ResponseEntity(new ApiResponse("Contact Person Berhasil di Update"),
        HttpStatus.CREATED);
  }
}
