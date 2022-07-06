package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.ViewEventPaymentApiResponse;
import id.myevent.model.dto.EventPaymentDto;
import id.myevent.service.EventPaymentService;
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
 * Event Payment Controller.
 */

@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventPaymentController {

  @Autowired
  EventPaymentService eventPaymentService;

  /**
   * Insert Payment.
   */
  @PostMapping("/events/{id}/payment/create")
  public ResponseEntity create(@PathVariable("id") Long id,
                               @RequestBody EventPaymentDto paymentDto) {
    eventPaymentService.create(id, paymentDto);
    return new ResponseEntity(new ApiResponse("Pengaturan Pembayaran Tersimpan"),
        HttpStatus.CREATED);
  }

  /**
   * Update Payment.
   */
  @PutMapping("/events/{eventId}/payment/{eventPaymentId}")
  public ResponseEntity<ApiResponse> editGuest(@PathVariable("eventId") Long eventId,
                                               @PathVariable("eventPaymentId") Long eventPaymentId,
                                               @RequestBody EventPaymentDto paymentDto) {
    eventPaymentService.updatePayment(eventId, eventPaymentId, paymentDto);
    return new ResponseEntity(new ApiResponse("Pengaturan Pembayaran Berhasil di Update"),
        HttpStatus.OK);
  }

  @GetMapping("/events/{eventId}/payments")
  public ViewEventPaymentApiResponse getEventPayments(@PathVariable("eventId") Long eventId) {
    return eventPaymentService.getEventPayments(eventId);
  }
}
