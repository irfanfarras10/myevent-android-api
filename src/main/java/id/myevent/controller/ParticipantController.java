package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.CancelMessage;
import id.myevent.model.apiresponse.ViewEventParticipantApiResponse;
import id.myevent.model.apiresponse.ViewEventParticipantListApiResponse;
import id.myevent.model.dao.TicketParticipantDao;
import id.myevent.model.dto.ParticipantDto;
import id.myevent.service.EmailService;
import id.myevent.service.ParticipantService;
import id.myevent.util.ImageUtil;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
 * Participant Controller REST API.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class ParticipantController {

  @Autowired
  ParticipantService participantService;

  @Autowired
  EmailService emailService;

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

  @PostMapping("/events/{eventId}/participant/{participantId}/confirm")
  public ResponseEntity<ApiResponse> confirmPayment(
      @PathVariable("eventId") Long eventId,
      @PathVariable("participantId") Long participantId) {
    participantService.confirm(eventId, participantId);
    return ResponseEntity.ok(new ApiResponse("Pembayaran Berhasil dikonfirmasi."));
  }

  @PostMapping("/events/{eventId}/participant/{participantId}/reject")
  public ResponseEntity<ApiResponse> rejectPayment(
      @PathVariable("eventId") Long eventId,
      @PathVariable("participantId") Long participantId,
      @RequestBody CancelMessage message) {
    emailService.reject(eventId, participantId, message);
    return ResponseEntity.ok(new ApiResponse("Pembayaran Berhasil ditolak."));
  }

  /**
   * Download Participant List.
   */
  @GetMapping("/events/{eventId}/participants/download")
  public ResponseEntity<Resource> getFile(@PathVariable("eventId") Long eventId) {
    String filename = "participantList.xlsx";
    InputStreamResource file = new InputStreamResource(participantService.load(eventId));
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(file);
  }

}
