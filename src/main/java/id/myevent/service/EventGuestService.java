package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.apiresponse.ViewEventGuestListApiResponse;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventGuestDao;
import id.myevent.model.dto.EventGuestDto;
import id.myevent.repository.EventGuestRepository;
import id.myevent.repository.EventRepository;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

/**
 * Event Guest Service.
 */
@Service
@Slf4j
public class EventGuestService {

  @Autowired
  EventGuestRepository eventGuestRepository;
  @Autowired
  EventRepository eventRepository;

  @Autowired
  JavaMailSender javaMailSender;

  /**
   * Create Event Guest.
   */
  public void create(Long eventId, EventGuestDto guestEvent) {
    final EventDao eventData = eventRepository.findById(eventId).get();
    final EventGuestDao guest = new EventGuestDao();
    //insert guest
    guest.setName(guestEvent.getName());
    guest.setPhoneNumber(guestEvent.getPhoneNumber());
    guest.setEmail(guestEvent.getEmail());
    guest.setAlreadyShared(false);
    guest.setEvent(eventData);
    try {
      eventGuestRepository.save(guest);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
  }

  /**
   * Update Guest.
   */
  public void updateGuest(Long eventId, Long guestId, EventGuestDto guestData) {
    Optional<EventGuestDao> currentGuest = eventGuestRepository.findById(guestId);
    final EventDao eventData = eventRepository.findById(eventId).get();
    EventGuestDao newGuest = currentGuest.get();

    if (eventData.getEventStatus().getId() == 1) {
      if (guestData.getName() != null) {
        newGuest.setName(guestData.getName());
      }
      if (guestData.getEmail() != null) {
        newGuest.setEmail(guestData.getEmail());
      }
      if (guestData.getPhoneNumber() != null) {
        newGuest.setPhoneNumber(guestData.getPhoneNumber());
      }
      newGuest.setEvent(eventData);

      try {
        eventGuestRepository.save(newGuest);
      } catch (DataIntegrityViolationException exception) {
        String exceptionMessage = exception.getMostSpecificCause().getMessage();
        throw new ConflictException(exceptionMessage);
      }
    } else {
      throw new ConflictException("Event harus di status Draft");
    }
  }

  /**
   * Get List of Guest.
   */
  public ViewEventGuestListApiResponse getGuestList(Long eventId) {
    List<EventGuestDao> guestEvent = eventGuestRepository.findByEvent(eventId);
    ViewEventGuestListApiResponse viewEventGuestListApiResponse =
        new ViewEventGuestListApiResponse();
    viewEventGuestListApiResponse.setListGuest(guestEvent);
    return viewEventGuestListApiResponse;
  }

  /**
   * Invite All Guest.
   */
  public void inviteAll(Long eventId) {
    final EventDao eventData = eventRepository.findById(eventId).get();
    List<String> guests = new ArrayList<>();
    List<EventGuestDao> eventGuest = eventGuestRepository.findByEvent(eventId);

    final String emailMessage = mailMessage(eventData);

    try {
      MimeMessage message = javaMailSender.createMimeMessage();

      MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

      //get multiple email
      for (int i = 0; i < eventGuest.size(); i++) {
        String email = eventGuest.get(i).getEmail();
        guests.add(email);
      }
      String[] mailsArray = guests.toArray(new String[0]);
      log.warn(String.valueOf(mailsArray));
      messageHelper.setTo(mailsArray);
      messageHelper.setCc(eventData.getEventOrganizer().getEmail());
      messageHelper.setSubject("Event Invitation - " + eventData.getName());
      messageHelper.setText(emailMessage, true);

      javaMailSender.send(message);

      //change email status in Event Guest
      for (int j = 0; j < eventGuest.size(); j++) {
        eventGuest.get(j).setAlreadyShared(true);
        eventGuestRepository.save(eventGuest.get(j));
      }
    } catch (Exception e) {
      throw new ConflictException("Email gagal dikirim");
    }
  }

  /**
   * Invite All Guest.
   */
  public void invite(Long eventId, Long guestId) {
    final EventDao eventData = eventRepository.findById(eventId).get();
    EventGuestDao guestData = eventGuestRepository.findById(guestId).get();

    final String emailMessage = mailMessage(eventData);

    try {
      MimeMessage message = javaMailSender.createMimeMessage();

      MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

      messageHelper.setTo(guestData.getEmail());
      messageHelper.setCc(eventData.getEventOrganizer().getEmail());
      messageHelper.setSubject("Event Invitation - " + eventData.getName());
      messageHelper.setText(emailMessage, true);

      javaMailSender.send(message);

      //change email status in Event Guest
      guestData.setAlreadyShared(true);
      eventGuestRepository.save(guestData);
    } catch (Exception e) {
      throw new ConflictException("Email gagal dikirim");
    }
  }

  /**
   * Mail Invitation message.
   */
  public String mailMessage(EventDao eventData) {

    DateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    String dateTime = sdf.format(eventData.getDateTimeEventStart());

    final String emailMessage = "<html>\n"
        + "<body>\n"
        + "    <p>Kepada Bapak/Ibu,</p>\n"
        + "    <p>Kami ingin mengundang anda ke acara " + eventData.getName()
        + " yang diselenggarakan oleh " + eventData.getEventOrganizer().getOrganizerName()
        + ". Event tersebut akan dilaksanakan pada: </p>\n"
        + "    <p><b>Hari, Tanggal:</b> " + dateTime + "</p>\n"
        + "    <p><b>Tempat:</b> " + eventData.getVenue() + "</p>\n"
        +
        "    <p>Demikian undangan ini disampaikan, kami berharap kedatangan Bapak/Ibu pada acara "
        + "kami.</p>\n"
        + "    <p>Untuk informasi lebih lanjut silakan menghubungi tim dari "
        + eventData.getEventOrganizer().getOrganizerName() + ".</p>\n"
        + "</body>\n"
        + "</html>";

    return emailMessage;
  }

}
