package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.helper.ExcelHelper;
import id.myevent.model.apiresponse.ParticipantPresentData;
import id.myevent.model.apiresponse.ViewEventParticipantApiResponse;
import id.myevent.model.apiresponse.ViewEventParticipantListApiResponse;
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
import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Participant Service.
 */
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
    if (eventData.getEventPaymentCategory().getId() == 1) {
      participant.setStatus("Terkonfirmasi");
    } else {
      participant.setStatus("Menunggu Konfirmasi");
    }
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
    if (eventData.getEventPaymentCategory().getId() == 1) {
      ticketParticipant.setStatus("Terbayar");
    } else {
      ticketParticipant.setStatus("Menunggu Konfirmasi");
    }

    if (participantData.getPaymentProofPhoto() != null) {
      ticketParticipant.setPaymentPhotoProof(
          ImageUtil.compressImage(participantData.getPaymentProofPhoto()));
      ticketParticipant.setPaymentPhotoName(
          generateUniqueImageName(participantData.getPaymentPhotoName()));
      ticketParticipant.setPaymentPhotoType(participantData.getPaymentPhotoType());
    } else {
      ticketParticipant.setPaymentPhotoProof(null);
      ticketParticipant.setPaymentPhotoType(null);
      ticketParticipant.setPaymentPhotoName(null);
    }

    if (participantData.getPaymentId() != null) {
      EventPaymentDao paymentData =
          eventPaymentRepository.findById(participantData.getPaymentId()).get();
      ticketParticipant.setEventPayment(paymentData);
    } else {
      ticketParticipant.setEventPayment(null);
    }
    
    //reduce ticket quota if free event
    if(eventData.getEventPaymentCategory().getId() == 1){
      ticketData.setQuotaTotal(ticketData.getQuotaTotal() - 1);
    }
    
    ticketParticipant.setTicket(ticketData);
    ticketParticipant.setParticipant(participant);
    try {
      ticketParticipantRepository.save(ticketParticipant);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
    //send email if event is free
    if (eventData.getEventPaymentCategory().getId() == 1) {
      emailService.sendMessage(eventId, participant.getId(), ticketParticipant.getId());
    }
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

  /**
   * Get List of Participant Confirmed.
   */
  public ViewEventParticipantListApiResponse getParticipantConfirmed(Long eventId) {
    List<ParticipantDao> participantEvent = participantRepository.findByStatusConfirmed(eventId);
    ViewEventParticipantListApiResponse viewEventGuestListApiResponse =
        new ViewEventParticipantListApiResponse();
    viewEventGuestListApiResponse.setListParticipant(participantEvent);
    return viewEventGuestListApiResponse;
  }

  /**
   * Get List of Participant Wait.
   */
  public ViewEventParticipantListApiResponse getParticipantWait(Long eventId) {
    List<ParticipantDao> participantEvent = participantRepository.findByStatusWait(eventId);
    ViewEventParticipantListApiResponse viewEventGuestListApiResponse =
        new ViewEventParticipantListApiResponse();
    viewEventGuestListApiResponse.setListParticipant(participantEvent);
    return viewEventGuestListApiResponse;
  }


  /**
   * Get List of Participant Attend.
   */
  public ViewEventParticipantListApiResponse getParticipantAttend(Long eventId) {
    List<ParticipantDao> participantEvent = participantRepository.findByStatusAttend(eventId);
    ViewEventParticipantListApiResponse viewEventGuestListApiResponse =
        new ViewEventParticipantListApiResponse();
    viewEventGuestListApiResponse.setListParticipant(participantEvent);
    return viewEventGuestListApiResponse;
  }

  /**
   * Search Participant By Name.
   */
  public ViewEventParticipantListApiResponse getParticipantByName(String name) {
    List<ParticipantDao> participant = participantRepository.findByName(name);
    List<ParticipantDao> newParticipantData = new ArrayList<>();
    ViewEventParticipantListApiResponse newParticipant = new ViewEventParticipantListApiResponse();

    for (int i = 0; i < participant.size(); i++) {
      ParticipantDao participants = new ParticipantDao();
      participants.setId(participant.get(i).getId());
      participants.setName(participant.get(i).getName());
      participants.setEmail(participant.get(i).getEmail());
      participants.setPhoneNumber(participant.get(i).getPhoneNumber());
      participants.setStatus(participant.get(i).getStatus());
      newParticipantData.add(participants);
    }
    newParticipant.setListParticipant(newParticipantData);
    return newParticipant;
  }

  /**
   * View Detail Participant.
   */
  public ViewEventParticipantApiResponse getDetailParticipant(Long eventId, Long participantId) {
    ViewEventParticipantApiResponse newParticipant = new ViewEventParticipantApiResponse();
    Optional<ParticipantDao> participantData = participantRepository.findById(participantId);
    Optional<TicketParticipantDao> ticketData =
        ticketParticipantRepository.findByParticipantId(participantId);

    newParticipant.setId(participantData.get().getId());
    newParticipant.setName(participantData.get().getName());
    newParticipant.setEmail(participantData.get().getEmail());
    newParticipant.setPhoneNumber(participantData.get().getPhoneNumber());
    newParticipant.setTicket(ticketData.get().getTicket().getName());
    if (ticketData.get().getPaymentPhotoProof() != null) {
      newParticipant.setPaymentProofPhoto(
          generateBannerPhotoUrl(ticketData.get().getPaymentPhotoName()));
    } else {
      newParticipant.setPaymentProofPhoto(null);
    }

    return newParticipant;
  }

  private String generateBannerPhotoUrl(String filename) {
    String url = "";
    url = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    url += "/web/events/participant/image/" + filename;
    return url;
  }

  /**
   * Get Participant Proof Image.
   */
  public TicketParticipantDao getImage(String imageName) {
    final Optional<TicketParticipantDao> event =
        ticketParticipantRepository.findByImageName(imageName);

    return event.get();
  }

  /**
   * Confirmation.
   */
  public void confirm(Long eventId, Long participantId) {
    EventDao eventData = eventRepository.findById(eventId).get();
    ParticipantDao participantData = participantRepository.findById(participantId).get();
    TicketParticipantDao ticketParticipant =
        ticketParticipantRepository.findByParticipantId(participantId).get();
    Long ticketParticipantId = ticketParticipant.getId();
    //update status di participant
    participantData.setStatus("Terkonfirmasi");
    participantRepository.save(participantData);
    //update status di ticket participant
    ticketParticipant.setStatus("Terbayar");
    
    //reduce ticket quota
    TicketDao ticketData = ticketParticipant.getTicket();
    ticketData.setQuotaTotal(ticketParticipant.getTicket().getQuotaTotal() - 1);
    ticketParticipant.setTicket(ticketData);
    
    ticketParticipantRepository.save(ticketParticipant);
    emailService.sendMessage(eventId, participantId, ticketParticipantId);
  }

  /**
   * Download Excel.
   */
  public ByteArrayInputStream load(long id) {
    List<ParticipantDao> participants = participantRepository.findByEvent(id);
    ByteArrayInputStream in = ExcelHelper.downloadExcel(participants);
    return in;
  }

  /**
   * Check participant data by email and date.
   */
  public ParticipantPresentData getParticipantPresence(Long eventId, String email, Long dates) {
    ParticipantPresentData newParticipant = new ParticipantPresentData();

    List<ParticipantDao> participantData = participantRepository.findByEmail(email, eventId);

    for (int i = 0; i < participantData.size(); i++) {
      for (int j = 0; j < participantData.get(i).getTicketParticipants().size(); j++) {

        if (participantData.get(i).getTicketParticipants().get(j).getEvent_date().equals(dates)) {
          newParticipant.setId(participantData.get(i).getId());
          newParticipant.setName(participantData.get(i).getName());
          newParticipant.setEmail(participantData.get(i).getEmail());
          newParticipant.setPhoneNumber(participantData.get(i).getPhoneNumber());
          newParticipant.setStatus(participantData.get(i).getStatus());
        }
      }
    }
    return newParticipant;
  }

  /**
   * Update status participant present.
   */
  public void presence(Long eventId, Long participantId) {
    ParticipantDao participant = participantRepository.findById(participantId).get();

    //update participant status
    participant.setStatus("Hadir");
    participantRepository.save(participant);

  }

}
