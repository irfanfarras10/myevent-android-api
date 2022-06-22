package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.dto.TicketDto;
import id.myevent.service.TicketService;
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
 * Ticket REST Controller.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class TicketController {
  @Autowired
  TicketService ticketService;

  @PostMapping("/events/{id}/ticket/create")
  public ResponseEntity create(@PathVariable("id") Long id, @RequestBody TicketDto ticketDto) {
    ticketService.create(id, ticketDto);
    return new ResponseEntity(new ApiResponse("Tiket Tersimpan"), HttpStatus.CREATED);
  }

  @PutMapping("/events/{eventId}/ticket/{ticketId}")
  public ResponseEntity<ApiResponse> editTicket(@PathVariable("eventId") Long eventId,
                                                @PathVariable("ticketId") Long ticketId,
                                                @RequestBody TicketDto ticketDto) {
    ticketService.updateTicket(eventId, ticketId, ticketDto);
    return new ResponseEntity(new ApiResponse("Tiket Berhasil di Update"), HttpStatus.OK);
  }
}
