package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.ViewEventApiResponse;
import id.myevent.model.apiresponse.ViewEventListApiResponse;
import id.myevent.model.apiresponse.ViewEventParticipantApiResponse;
import id.myevent.model.apiresponse.ViewEventParticipantListApiResponse;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.TicketParticipantDao;
import id.myevent.model.dto.ParticipantDto;
import id.myevent.service.ParticipantService;
import id.myevent.util.ImageUtil;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Participant Controller REST API.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class ParticipantController {

  @Autowired
  ParticipantService participantService;

  /**
   * Participant Regist.
   */
  @PostMapping("/events/{id}/participant/regist")
  public ResponseEntity create(
      @PathVariable("id") Long id,
      @RequestParam("name") String name,
      @RequestParam("email") String email,
      @RequestParam("phoneNumber") String phoneNumber,
      @RequestParam("ticketId") Long ticketId,
      @RequestParam(value = "paymentId", required = false) Long paymentId,
      @RequestParam("eventDate") Long eventDate,
      @RequestParam(value = "paymentPhoto", required = false) MultipartFile paymentPhoto
  ) throws IOException {
    ParticipantDto createParticipant = new ParticipantDto();
    createParticipant.setName(name);
    createParticipant.setEmail(email);
    createParticipant.setPhoneNumber(phoneNumber);
    createParticipant.setTicketId(ticketId);
    if (paymentId != null) {
      createParticipant.setPaymentId(paymentId);
    }
    if (paymentPhoto != null) {
      createParticipant.setPaymentProofPhoto(paymentPhoto.getBytes());
      createParticipant.setPaymentPhotoType(paymentPhoto.getContentType());
    }
    createParticipant.setDateEvent(eventDate);
    participantService.create(id, createParticipant);
    return new ResponseEntity(new ApiResponse("Anda Berhasil Terdaftar"), HttpStatus.CREATED);
  }

  @GetMapping("/events/{eventId}/participants/confirmed")
  public ViewEventParticipantListApiResponse getParticipantConfirmed(
      @PathVariable("eventId") Long eventId) {
    return participantService.getParticipantConfirmed(eventId);
  }

  @GetMapping("/events/{eventId}/participants/wait")
  public ViewEventParticipantListApiResponse getParticipantWait(
      @PathVariable("eventId") Long eventId) {
    return participantService.getParticipantWait(eventId);
  }

  @GetMapping("/events/{eventId}/participants/attend")
  public ViewEventParticipantListApiResponse getParticipantAttend(
      @PathVariable("eventId") Long eventId) {
    return participantService.getParticipantAttend(eventId);
  }

  /**
   * get participant detail.
   */
  @GetMapping("events/{eventId}/participant/{participantId}")
  public ViewEventParticipantApiResponse getDetailEvent(@PathVariable("eventId") Long eventId,
                                                        @PathVariable("participantId")
                                                        Long participantId) {
    return participantService.getDetailParticipant(eventId, participantId);
  }

  /**
   * get participant by name.
   */
  @GetMapping("events/{eventId}/participant/name")
  public ViewEventParticipantListApiResponse getEventByName(@RequestParam("name") String name) {
    return participantService.getParticipantByName(name);
  }

  /**
   * get image participant.
   */
  @GetMapping(path = {"/events/participant/image/{name}"})
  public ResponseEntity<byte[]> getImage(@PathVariable("name") String name) throws IOException {

    TicketParticipantDao image = participantService.getImage(name);

    return ResponseEntity.ok()
        .contentType(MediaType.valueOf(image.getPaymentPhotoType()))
        .body(ImageUtil.decompressImage(image.getPaymentPhotoProof()));
  }
}
