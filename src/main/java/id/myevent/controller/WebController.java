package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.DateEvent;
import id.myevent.model.apiresponse.ParticipantPresentData;
import id.myevent.model.apiresponse.ViewEventApiResponse;
import id.myevent.model.dao.TicketParticipantDao;
import id.myevent.model.dto.ParticipantDto;
import id.myevent.service.EventService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Web controller REST API.
 */
@CrossOrigin
@RestController
@RequestMapping("/web")
@Slf4j
public class WebController {

  @Autowired
  EventService eventService;

  @Autowired
  ParticipantService participantService;

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

  /**
   * Cek by email.
   */
  @GetMapping("/events/{eventId}/participant")
  public ParticipantPresentData getParticipantData(
      @PathVariable("eventId") Long eventId,
      @RequestBody ParticipantDto participant,
      @RequestParam("dates") Long dates) {
    return participantService.getParticipantPresence(eventId, participant.getEmail(), dates);
  }

  /**
   * Participant Presence.
   */
  @PostMapping("/events/{id}/participant/presence")
  public ResponseEntity presence(
      @PathVariable("id") Long id,
      @RequestParam("partId") Long participantId
  ) throws IOException {
    participantService.presence(id, participantId);
    return new ResponseEntity(new ApiResponse("Anda Berhasil Melakukan Presensi."),
        HttpStatus.CREATED);
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
