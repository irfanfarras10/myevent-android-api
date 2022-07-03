package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.ViewEventGuestListApiResponse;
import id.myevent.model.dto.EventGuestDto;
import id.myevent.service.EmailService;
import id.myevent.service.EventGuestService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Event Guest Controller.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventGuestController {

  @Autowired
  EventGuestService eventGuestService;

  @Autowired
  EmailService emailService;

  @PostMapping("/events/{id}/guest/create")
  public ResponseEntity create(@PathVariable("id") Long id, @RequestBody EventGuestDto guestDto) {
    eventGuestService.create(id, guestDto);
    return new ResponseEntity(new ApiResponse("Undangan Tersimpan"), HttpStatus.CREATED);
  }

  @PutMapping("/events/{eventId}/guest/{guestId}")
  public ResponseEntity<ApiResponse> editGuest(@PathVariable("eventId") Long eventId,
                                               @PathVariable("guestId") Long guestId,
                                               @RequestBody EventGuestDto guestDto) {
    eventGuestService.updateGuest(eventId, guestId, guestDto);
    return new ResponseEntity(new ApiResponse("Undangan Berhasil di Update"), HttpStatus.OK);
  }

  @GetMapping("/events/{eventId}/guest")
  public ViewEventGuestListApiResponse getGuest(@PathVariable("eventId") Long eventId) {
    return eventGuestService.getGuestList(eventId);
  }

  @GetMapping("/events/{eventId}/guest/invite")
  public ResponseEntity sendEmailAll(@PathVariable("eventId") Long eventId) {
    emailService.inviteAll(eventId);
    return new ResponseEntity(new ApiResponse("Email berhasil terkirim"), HttpStatus.OK);
  }

  @GetMapping("/events/{eventId}/guest/{guestId}/invite")
  public ResponseEntity sendEmail(@PathVariable("eventId") Long eventId,
                                  @PathVariable("guestId") Long guestId) {
    emailService.invite(eventId, guestId);
    return new ResponseEntity(new ApiResponse("Email berhasil terkirim"), HttpStatus.OK);
  }


}
