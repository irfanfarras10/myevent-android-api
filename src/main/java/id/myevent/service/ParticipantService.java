package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventPaymentDao;
import id.myevent.model.dao.ParticipantDao;
import id.myevent.model.dao.TicketDao;
import id.myevent.model.dao.TicketParticipantDao;
import id.myevent.model.dto.ParticipantDto;
import id.myevent.repository.EventPaymentRepository;
import id.myevent.repository.EventRepository;
import id.myevent.repository.ParticipantRepository;
import id.myevent.repository.TicketParticipantRepository;
import id.myevent.repository.TicketRepository;
import id.myevent.util.ImageUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ParticipantService {

  @Autowired
  ParticipantRepository participantRepository;

  @Autowired
  TicketParticipantRepository ticketParticipantRepository;

  @Autowired
  EventPaymentRepository eventPaymentRepository;

  @Autowired
  EventRepository eventRepository;

  @Autowired
  TicketRepository ticketRepository;

  @Autowired
  EmailService emailService;

  /**
   * Create Participant.
   */
  public void create(Long eventId, ParticipantDto participantData) {

    final EventDao eventData = eventRepository.findById(eventId).get();
    TicketDao ticketData = ticketRepository.findById(participantData.getTicketId()).get();
    // insert to participant data
    final ParticipantDao participant = new ParticipantDao();
    participant.setName(participantData.getName());
    participant.setEmail(participantData.getEmail());
    participant.setPhoneNumber(participantData.getPhoneNumber());
    participant.setStatus("Terdaftar");
    participant.setEvent(eventData);
    try {
      participantRepository.save(participant);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
    final TicketParticipantDao ticketParticipant = new TicketParticipantDao();
    // insert to ticket participant data

    Date today = Calendar.getInstance().getTime();
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    String currentTime = sdf.format(today);
    Long epochTime;
    try {
      Date date = sdf.parse(currentTime);
      epochTime = date.getTime();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }

    ticketParticipant.setEvent_date(participantData.getDateEvent());
    ticketParticipant.setPurchase_date(epochTime);
    ticketParticipant.setStatus("Terbayar");
    ticketParticipant.setPaymentPhotoProof(ImageUtil.compressImage(participantData.getPaymentProofPhoto()));
    ticketParticipant.setPaymentPhotoName(generateUniqueImageName(participantData.getPaymentPhotoName()));
    ticketParticipant.setPaymentPhotoType(participantData.getPaymentPhotoType());
    ticketParticipant.setTicket(ticketData);
    if(participantData.getPaymentId() != null){
      EventPaymentDao paymentData = eventPaymentRepository.findById(participantData.getPaymentId()).get();
      ticketParticipant.setEventPayment(paymentData);
    }else{
      ticketParticipant.setEventPayment(null);
    }
    ticketParticipant.setParticipant(participant);
    try {
      ticketParticipantRepository.save(ticketParticipant);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
    //TODO: send email
    emailService.sendMessage(eventId, participant.getId(), ticketParticipant.getId());
  }

  private String generateUniqueImageName(String imageFormat) {
    String filename = "";
    long millis = System.currentTimeMillis();
    String datetime = new Date().toGMTString();
    datetime = datetime.replace(" ", "");
    datetime = datetime.replace(":", "");
    String uuid = UUID.randomUUID().toString();
    filename = uuid + "_" + datetime + "_" + millis;
    return filename;
  }
}
