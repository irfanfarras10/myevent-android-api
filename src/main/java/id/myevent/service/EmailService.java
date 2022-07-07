package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.apiresponse.CancelMessage;
import id.myevent.model.apiresponse.HtmlToImageApiResponse;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventGuestDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.model.dao.ParticipantDao;
import id.myevent.model.dao.TicketParticipantDao;
import id.myevent.model.location.Location;
import id.myevent.repository.EventGuestRepository;
import id.myevent.repository.EventRepository;
import id.myevent.repository.EventStatusRepository;
import id.myevent.repository.ParticipantRepository;
import id.myevent.repository.TicketParticipantRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.TimeZone;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
  @Autowired
  ParticipantRepository participantRepository;
  @Autowired
  TicketParticipantRepository ticketParticipantRepository;

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
        if (eventGuest.get(i).isAlreadyShared() == false) {
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
    List<ParticipantDao> participants = participantRepository.findByEvent(id);
    String valueMessage = message.getMessage();

    final String emailMessage = mailCancelMessage(event, valueMessage);

    //send message to all participants & guest
    try {
      MimeMessage messages = javaMailSender.createMimeMessage();

      MimeMessageHelper messageHelper = new MimeMessageHelper(messages, true);

      //get multiple email guests
      for (int i = 0; i < eventGuest.size(); i++) {
        String email = eventGuest.get(i).getEmail();
        guests.add(email);
      }
      //get multiple email participants
      for (int j = 0; j <participants.size(); j++){
        String email = participants.get(j).getEmail();
        guests.add(email);
      }
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
   * Send email message to participant.
   */
  public void sendMessage(Long eventId, Long participantId, Long ticketParticipantId) {
    EventDao event = eventRepository.findById(eventId).get();
    ParticipantDao participant = participantRepository.findById(participantId).get();
    TicketParticipantDao ticketParticipant =
        ticketParticipantRepository.findById(ticketParticipantId).get();

    String usernameColonPassword = "d2f83dca-182d-4d85-9e6f-7c6e9c697745:9711011a-8f20-4860-af2b-f787085cdb73";
    String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());

    String url = "https://hcti.io/v1/image";
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.add("Authorization", basicAuthPayload);
    headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("html", ticketHtml(event, ticketParticipant, participant));
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    log.warn(basicAuthPayload);
    HtmlToImageApiResponse ticket = restTemplate.postForObject(url, request, HtmlToImageApiResponse.class);

    log.warn(ticket.getUrl());
    final String emailMessage = mailParticipant(event, ticket.getUrl());

    try {
      generateIcs(event);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      MimeMessage message = javaMailSender.createMimeMessage();

      MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

      messageHelper.setTo(participant.getEmail());
      messageHelper.setCc(event.getEventOrganizer().getEmail());
      messageHelper.setSubject("Event - " + event.getName());
      messageHelper.setText(emailMessage, true);

      String filePath = new File("calendar").getAbsolutePath() + "\\" + event.getName()
          + event.getTimeEventStart() + ".ics";
      FileSystemResource resource = new FileSystemResource(new File(filePath));

      messageHelper.addAttachment("event.ics", resource);

      javaMailSender.send(message);

    } catch (Exception e) {
      throw new ConflictException("Email gagal dikirim");
    }
  }

  /**
   * Generate Message for participant.
   */
  public String mailParticipant(EventDao eventData, String url) {

    String ticketSrc =        "    <form action=\""+url+"\" style=\"font-family: 'Inter', sans-serif;\">\n" +
        "        <button class=\"gfg\" type=\"submit\" style=\"font-family: 'Inter', sans-serif;background-color: white;border: 2px solid black;padding: 5px 10px;text-align: center;display: inline-block;font-size: 20px;cursor: pointer;\">\n" +
        "            Unduh Tiket\n" +
        "        </button>\n" +
        "    </form>\n";

    final String emailMessage = "<!DOCTYPE html>\n" +
        "<html style=\"font-family: 'Inter', sans-serif;\">\n" +
        "<head style=\"font-family: 'Inter', sans-serif;\">\n" +
        "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\" style=\"font-family: 'Inter', sans-serif;\">\n" +
        "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin style=\"font-family: 'Inter', sans-serif;\">\n" +
        " \n" +
        " \n" +
        "    \n" +
        "    </head>\n" +
        "<body style=\"font-family: 'Inter', sans-serif;\">\n" +
        "    <h1 style=\"font-family: 'Inter', sans-serif;\">Registrasi Berhasil</h1>\n" +
        "    <div style=\"font-family: 'Inter', sans-serif;\">Selamat! Registrasi Anda berhasil untuk mengikuti event "+eventData.getName()+"</div><br style=\"font-family: 'Inter', sans-serif;\">\n" +
        "    <div style=\"font-family: 'Inter', sans-serif;\">Silakan mengunduh file Tiket untuk menjadi bukti keikutsertaan Anda dalam mengikuti Event.</div>\n" +
        "    <br style=\"font-family: 'Inter', sans-serif;\">\n" +
        ""+ticketSrc+
        "</body>\n" +
        "\n" +
        "</html>";

    return emailMessage;
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
    try {
      event.add(new Organizer(eventData.getEventOrganizer().getEmail()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
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

  public String ticketHtml(EventDao eventData, TicketParticipantDao ticketData, ParticipantDao participantData){
    DateFormat sdf = new SimpleDateFormat("EEEE, dd. MMMM yyyy HH:mm");
    String dateTime = sdf.format(eventData.getTimeEventStart());

    String lat = StringUtils.substringBefore(eventData.getVenue(), "|");
    String lon = StringUtils.substringAfter(eventData.getVenue(), "|");

    Location loc = getLocation(lat, lon);
    String name = loc.features.get(0).properties.name;
    String address_line2 = loc.features.get(0).properties.address_line2;

    String ticketHtlm = "<!DOCTYPE html>\n" +
        "<html>\n" +
        "  <head>\n" +
        "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
        "    <title>'Tiket'</title>\n" +
        "   <style type=\"text/css\">\n" +
        "    @import url('https://fonts.googleapis.com/css?family=Oswald');\n" +
        "* {\n" +
        "    margin: 0;\n" +
        "    padding: 0;\n" +
        "    border: 0;\n" +
        "    box-sizing: border-box\n" +
        "}\n" +
        "\n" +
        "body {\n" +
        "    background-color: #e8e9eb;\n" +
        "    font-family: arial\n" +
        "}\n" +
        "\n" +
        ".container {\n" +
        "  padding-top: 20px;\n" +
        "  padding-bottom: 20px;\n" +
        "  padding-right: 20px;\n" +
        "  padding-left: 20px;\n" +
        "}\n" +
        "\n" +
        ".fl-left {\n" +
        "    float: left\n" +
        "}\n" +
        "\n" +
        ".fl-right {\n" +
        "    float: right\n" +
        "}\n" +
        "\n" +
        "h1 {\n" +
        "    text-transform: uppercase;\n" +
        "    font-weight: 900;\n" +
        "    border-left: 10px solid #fec500;\n" +
        "    padding-left: 10px;\n" +
        "    margin-bottom: 10px\n" +
        "}\n" +
        "\n" +
        ".information, p {\n" +
        "  padding-left: 20px,\n" +
        "}\n" +
        "\n" +
        ".row {\n" +
        "    overflow: hidden\n" +
        "}\n" +
        "\n" +
        ".card {\n" +
        "    display: table-row;\n" +
        "    width: 49%;\n" +
        "    background-color: #fff;\n" +
        "    color: #989898;\n" +
        "    margin-bottom: 10px;\n" +
        "    font-family: 'Oswald', sans-serif;\n" +
        "    text-transform: uppercase;\n" +
        "    border-radius: 4px;\n" +
        "    position: relative\n" +
        "}\n" +
        "\n" +
        ".card+.card {\n" +
        "    margin-left: 2%\n" +
        "}\n" +
        "\n" +
        ".date {\n" +
        "    display: table-cell;\n" +
        "    width: 25%;\n" +
        "    position: relative;\n" +
        "    text-align: center;\n" +
        "    border-right: 2px dashed #e8e9eb\n" +
        "}\n" +
        "\n" +
        ".date:before,\n" +
        ".date:after {\n" +
        "    content: \"\";\n" +
        "    display: block;\n" +
        "    width: 30px;\n" +
        "    height: 30px;\n" +
        "    background-color: #e8e9eb;\n" +
        "    position: absolute;\n" +
        "    top: -15px;\n" +
        "    right: -15px;\n" +
        "    z-index: 1;\n" +
        "    border-radius: 50%\n" +
        "}\n" +
        "\n" +
        ".date:after {\n" +
        "    top: auto;\n" +
        "    bottom: -15px\n" +
        "}\n" +
        "\n" +
        ".date time {\n" +
        "    display: block;\n" +
        "    position: absolute;\n" +
        "    top: 50%;\n" +
        "    left: 50%;\n" +
        "    -webkit-transform: translate(-50%, -50%);\n" +
        "    -ms-transform: translate(-50%, -50%);\n" +
        "    transform: translate(-50%, -50%)\n" +
        "}\n" +
        "\n" +
        ".date time span {\n" +
        "    display: block\n" +
        "}\n" +
        "\n" +
        ".date time span:first-child {\n" +
        "    color: #2b2b2b;\n" +
        "    font-weight: 600;\n" +
        "    font-size: 250%\n" +
        "}\n" +
        "\n" +
        ".date time span:last-child {\n" +
        "    text-transform: uppercase;\n" +
        "    font-weight: 600;\n" +
        "    margin-top: -10px\n" +
        "}\n" +
        "\n" +
        ".card-cont {\n" +
        "    display: table-cell;\n" +
        "    width: 75%;\n" +
        "    font-size: 85%;\n" +
        "    padding: 10px 10px 30px 50px\n" +
        "}\n" +
        "\n" +
        ".card-cont h3 {\n" +
        "    color: #3C3C3C;\n" +
        "    font-size: 130%\n" +
        "}\n" +
        "\n" +
        ".card-cont>div {\n" +
        "    display: table-row\n" +
        "}\n" +
        "\n" +
        ".card-cont .even-date i,\n" +
        ".card-cont .even-info i,\n" +
        ".card-cont .even-date time,\n" +
        ".card-cont .even-info p {\n" +
        "    display: table-cell\n" +
        "}\n" +
        "\n" +
        ".card-cont .even-date i,\n" +
        ".card-cont .even-info i {\n" +
        "    padding: 5% 5% 0 0\n" +
        "}\n" +
        "\n" +
        ".card-cont .even-info p {\n" +
        "    padding: 30px 50px 0 0\n" +
        "}\n" +
        "\n" +
        ".card-cont .even-date time span {\n" +
        "    display: block\n" +
        "}\n" +
        "\n" +
        ".card-cont a {\n" +
        "    display: block;\n" +
        "    text-decoration: none;\n" +
        "    width: 80px;\n" +
        "    height: 30px;\n" +
        "    background-color: #D8DDE0;\n" +
        "    color: #fff;\n" +
        "    text-align: center;\n" +
        "    line-height: 30px;\n" +
        "    border-radius: 2px;\n" +
        "    position: absolute;\n" +
        "    right: 10px;\n" +
        "    bottom: 10px\n" +
        "}\n" +
        "\n" +
        ".row:last-child .card:first-child .card-cont a {\n" +
        "    background-color: #037FDD\n" +
        "}\n" +
        "\n" +
        ".row:last-child .card:last-child .card-cont a {\n" +
        "    background-color: #F8504C\n" +
        "}\n" +
        "\n" +
        "td {\n" +
        "            border: 1px solid black;\n" +
        "            padding: 10px;\n" +
        "         }\n" +
        "\n" +
        "@media screen and (max-width: 860px) {\n" +
        "    .card {\n" +
        "        display: block;\n" +
        "        float: none;\n" +
        "        width: 100%;\n" +
        "        margin-bottom: 10px\n" +
        "    }\n" +
        "    .card+.card {\n" +
        "        margin-left: 0\n" +
        "    }\n" +
        "    .card-cont .even-date,\n" +
        "    .card-cont .even-info {\n" +
        "        font-size: 75%\n" +
        "    }\n" +
        "}\n" +
        "  </style>\n" +
        "  </head>\n" +
        "  <body>\n" +
        "<h1>Tiket</h1>\n" +
        "  <div class=\"row\">\n" +
        "    <article class=\"card fl-left\">\n" +
        "      <section class=\"date\">\n" +
        "        <time datetime=\"23th feb\">\n" +
        "          <!--jenis tiket-->\n" +
        "          "+ticketData.getTicket().getName()+"\n" +
        "        </time>\n" +
        "      </section>\n" +
        "      <section class=\"card-cont\">\n" +
        "          <!--nama peserta-->\n" +
        "        <small>"+participantData.getName()+"</small>\n" +
        "         <!--nama event-->\n" +
        "        <h3>"+eventData.getName()+"</h3>\n" +
        "        <div class=\"even-date\">\n" +
        "         <i class=\"fa fa-calendar\"></i>\n" +
        "         <time>\n" +
        "            <!--tanggal event-->\n" +
        "           <span>"+dateTime+"</span>\n" +
        "         </time>\n" +
        "        </div>\n" +
        "        <div class=\"even-info\">\n" +
        "          <i class=\"fa fa-map-marker\"></i>\n" +
        "          <p>\n" +
        "            <!--lokasi event-->\n" +
        "            "+name+" "+address_line2+"\n" +
        "          </p>\n" +
        "        </div>\n" +
        "        <a href=\"#\">booked</a>\n" +
        "      </section>\n" +
        "    </article>\n" +
        "\n" +
        "  </div>\n" +
        "</div>\n" +
        "\n" +
        "  </body>\n" +
        "</html>";

    return ticketHtlm;
  }

}
