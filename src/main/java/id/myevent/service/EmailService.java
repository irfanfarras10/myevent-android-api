package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.apiresponse.CancelMessage;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventGuestDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.model.location.Location;
import id.myevent.repository.EventGuestRepository;
import id.myevent.repository.EventRepository;
import id.myevent.repository.EventStatusRepository;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Email Service.
 */
@Service
@Slf4j
public class EmailService {

  @Autowired
  EventGuestRepository eventGuestRepository;
  @Autowired
  EventRepository eventRepository;
  @Autowired
  EventStatusRepository eventStatusRepository;
  @Autowired
  JavaMailSender javaMailSender;

  /**
   * Invite All Guest.
   */
  public void inviteAll(Long eventId) {
    final EventDao eventData = eventRepository.findById(eventId).get();
    List<String> guests = new ArrayList<>();
    List<EventGuestDao> eventGuest = eventGuestRepository.findByEvent(eventId);

    final String emailMessage = mailMessage(eventData);

    try {
      generateIcs(eventData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      MimeMessage message = javaMailSender.createMimeMessage();

      MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

      // get multiple email
      for (int i = 0; i < eventGuest.size(); i++) {
        String email = eventGuest.get(i).getEmail();
        if(eventGuest.get(i).isAlreadyShared() == false){
          guests.add(email);
        }
      }
      String[] mailsArray = guests.toArray(new String[0]);
      log.warn(String.valueOf(mailsArray));
      messageHelper.setTo(mailsArray);
      messageHelper.setCc(eventData.getEventOrganizer().getEmail());
      messageHelper.setSubject("Event Invitation - " + eventData.getName());
      messageHelper.setText(emailMessage, true);

      String filePath = new File("calendar").getAbsolutePath() + "\\" + eventData.getName()
          + eventData.getTimeEventStart() + ".ics";
      FileSystemResource resource = new FileSystemResource(new File(filePath));

      messageHelper.addAttachment("event.ics", resource);

      javaMailSender.send(message);

      // change email status in Event Guest
      for (int j = 0; j < eventGuest.size(); j++) {
        eventGuest.get(j).setAlreadyShared(true);
        eventGuestRepository.save(eventGuest.get(j));
      }
    } catch (Exception e) {
      throw new ConflictException("Email gagal dikirim");
    }
  }

  /**
   * Cancel Event.
   */
  public void cancel(Long id, CancelMessage message) {

    EventDao event = eventRepository.findById(id).get();
    final EventStatusDao cancelledEventStatus = eventStatusRepository.findById(5L).get();
    List<String> guests = new ArrayList<>();
    List<EventGuestDao> eventGuest = eventGuestRepository.findByEvent(id);
    String valueMessage = message.getMessage();

    final String emailMessage = mailCancelMessage(event, valueMessage);

    //send message to all participants & guest
    try {
      MimeMessage messages = javaMailSender.createMimeMessage();

      MimeMessageHelper messageHelper = new MimeMessageHelper(messages, true);

      //TODO: get all data participants & guest
      //get multiple email guests
      for (int i = 0; i < eventGuest.size(); i++) {
        String email = eventGuest.get(i).getEmail();
        guests.add(email);
      }
      //get multiple email participants
      String[] mailsArray = guests.toArray(new String[0]);
      log.warn(String.valueOf(mailsArray));
      messageHelper.setTo(mailsArray);
      messageHelper.setCc(event.getEventOrganizer().getEmail());
      messageHelper.setSubject("Event Cancellation - " + event.getName());
      messageHelper.setText(emailMessage, true);

      javaMailSender.send(messages);

      //set event status to cancel
      event.setEventStatus(cancelledEventStatus);
      eventRepository.save(event);
    } catch (Exception e) {
      throw new ConflictException("Email gagal dikirim");
    }

  }

  /**
   * Generate Message for cancel event.
   */
  public String mailCancelMessage(EventDao eventData, String message) {

    DateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    String dateTime = sdf.format(eventData.getTimeEventStart());

    final String emailMessage = "<html>\n"
        + "<body>\n"
        + "    <p>Kepada Bapak/Ibu,</p>\n"
        + "    <p>Dengan email ini, kami ingin menginformasikan anda bahwa acara "
        + eventData.getName() + " pada tanggal " + dateTime
        + " yang diselenggarakan oleh " + eventData.getEventOrganizer().getOrganizerName()
        + "<b> Dibatalkan </b> dengan alasan " + message + ".</p>\n"
        +
        "    <p>Demikian informasi ini disampaikan, kami memohon maaf sebesar-besarnya atas "
        + "pembatalan acara ini.</p>\n"
        + "    <p>Untuk informasi lebih lanjut silakan menghubungi tim dari "
        + eventData.getEventOrganizer().getOrganizerName() + ".</p>\n"
        + "</body>\n"
        + "</html>";

    return emailMessage;
  }

  /**
   * Mail Invitation message.
   */
  public String mailMessage(EventDao eventData) {

    DateFormat sdf = new SimpleDateFormat("EEEE, dd. MMMM yyyy HH:mm");
    String dateTime = sdf.format(eventData.getTimeEventStart());

    String lat = StringUtils.substringBefore(eventData.getVenue(), "|");
    String lon = StringUtils.substringAfter(eventData.getVenue(), "|");

    Location loc = getLocation(lat, lon);
    String name = loc.features.get(0).properties.name;
    String address_line2 = loc.features.get(0).properties.address_line2;

    String imgSrc =
        "<img src=\"https://myevent-android-api.herokuapp.com/api/events/image/"
            + eventData.getBannerPhotoName()
            + "\" height=300>";

    final String emailMessage =
        "<html>\n"
            + "<body>\n"
            + "    <p>Kepada Bapak/Ibu,</p>\n"
            + "    <p>Kami ingin mengundang anda ke acara "
            + eventData.getName()
            + " yang diselenggarakan oleh "
            + eventData.getEventOrganizer().getOrganizerName()
            + ". Event tersebut akan dilaksanakan pada: </p>\n"
            + "    <p><b>Hari, Tanggal:</b> "
            + dateTime
            + "</p>\n"
            + "    <p><b>Tempat:</b> "
            + name
            + " "
            + address_line2
            + "</p>\n"
            + "    <p>Demikian undangan ini disampaikan, kami berharap kedatangan Bapak/Ibu pada "
            + "acara kami.</p>\n"
            + "    <p>Untuk informasi lebih lanjut silakan menghubungi tim dari "
            + eventData.getEventOrganizer().getOrganizerName()
            + ".</p>\n"
            + imgSrc
            + "</body>\n"
            + "</html>";

    return emailMessage;
  }

  /**
   * Get Location.
   */
  public Location getLocation(String lat, String lon) {

    String url =
        "https://api.geoapify.com/v1/geocode/reverse"
            + "?lat="
            + lat
            + "&lon="
            + lon
            + "&apiKey="
            + "26018a31a0aa41699818b7b50ea82935";
    log.warn(url);
    RestTemplate restTemplate = new RestTemplate();

    Location loc = restTemplate.getForObject(url, Location.class);

    return loc;
  }

  private void generateIcs(EventDao eventData) throws IOException {

    System.setProperty("net.fortuna.ical4j.timezone.cache.impl",
        "net.fortuna.ical4j.util.MapTimeZoneCache");
    System.setProperty("-Dical4j.validation.relaxed", "true");

    // Generate a UID for the event..
    UidGenerator ug = new RandomUidGenerator();
    Uid uid = ug.generateUid();

    /* Create the event */
    LocalDateTime start =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(eventData.getTimeEventStart()),
            TimeZone.getDefault().toZoneId());

    LocalDateTime end =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(eventData.getTimeEventEnd()),
            TimeZone.getDefault().toZoneId());

    String eventSummary = eventData.getName();
    String lat = StringUtils.substringBefore(eventData.getVenue(), "|");
    String lon = StringUtils.substringAfter(eventData.getVenue(), "|");
    Location loc = getLocation(lat, lon);

    String name = loc.features.get(0).properties.name;
    String address_line2 = loc.features.get(0).properties.address_line2;

    VEvent event = new VEvent(start, end, eventSummary);
    event.add(new Description(eventData.getDescription()));
    event.add(new net.fortuna.ical4j.model.property.Location(name + " " + address_line2));
    event.add(uid);

    //create calendar
    Calendar calendar = new Calendar();
    calendar.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.add(Version.VERSION_2_0);
    calendar.add(CalScale.GREGORIAN);

    /* Add event to calendar */
    calendar.add(event);

    String filePath = new File("calendar").getAbsolutePath() + "\\" + eventData.getName()
        + eventData.getTimeEventStart() + ".ics";
    FileOutputStream fout = null;

    try {

      fout = new FileOutputStream(filePath);
      CalendarOutputter outputter = new CalendarOutputter();
      outputter.output(calendar, fout);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
