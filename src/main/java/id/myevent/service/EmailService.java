package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.apiresponse.CancelMessage;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xhtmlrenderer.pdf.ITextRenderer;

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
   * Send email message to participant.
   */
  public void sendMessage(Long eventId, Long participantId, Long ticketParticipantId) {
    EventDao event = eventRepository.findById(eventId).get();
    ParticipantDao participant = participantRepository.findById(participantId).get();
    TicketParticipantDao ticketParticipant =
        ticketParticipantRepository.findById(ticketParticipantId).get();

    final String emailMessage = mailParticipant(event, participant, ticketParticipant);

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
  public String mailParticipant(EventDao eventData, ParticipantDao participantData,
                                TicketParticipantDao ticketParticipantData) {

    DateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
    String dateTime = sdf.format(ticketParticipantData.getEvent_date());

    String lat = StringUtils.substringBefore(eventData.getVenue(), "|");
    String lon = StringUtils.substringAfter(eventData.getVenue(), "|");

    Location loc = getLocation(lat, lon);
    String name = loc.features.get(0).properties.name;
    String address_line2 = loc.features.get(0).properties.address_line2;

//    final String emailMessage = "<!DOCTYPE html>\n" +
//        "<html>\n" +
//        "<head>\n" +
//        "  <title>'Tiket'</title>\n" +
//        "  <style type=\"text/css\">\n" +
//        "    @import url('https://fonts.googleapis.com/css?family=Oswald');\n" +
//        "  * {\n" +
//        "    margin: 0;\n" +
//        "    padding: 0;\n" +
//        "    border: 0;\n" +
//        "    box-sizing: border-box\n" +
//        "  }\n" +
//        "\n" +
//        "  body {\n" +
//        "    background-color: #e8e9eb;\n" +
//        "    font-family: arial\n" +
//        "  }\n" +
//        "\n" +
//        "  .container {\n" +
//        "  padding-top: 20px;\n" +
//        "  padding-bottom: 20px;\n" +
//        "  padding-right: 20px;\n" +
//        "  padding-left: 20px;\n" +
//        "  }\n" +
//        "\n" +
//        "  .fl-left {\n" +
//        "    float: left\n" +
//        "  }\n" +
//        "\n" +
//        "  .fl-right {\n" +
//        "    float: right\n" +
//        "  }\n" +
//        "\n" +
//        "  h1 {\n" +
//        "    text-transform: uppercase;\n" +
//        "    font-weight: 900;\n" +
//        "    border-left: 10px solid #fec500;\n" +
//        "    padding-left: 10px;\n" +
//        "    margin-bottom: 10px\n" +
//        "  }\n" +
//        "\n" +
//        "  .information, p {\n" +
//        "  padding-left: 20px,\n" +
//        "  }\n" +
//        "\n" +
//        "  .row {\n" +
//        "    overflow: hidden\n" +
//        "  }\n" +
//        "\n" +
//        "  .card {\n" +
//        "    display: table-row;\n" +
//        "    width: 49%;\n" +
//        "    background-color: #fff;\n" +
//        "    color: #989898;\n" +
//        "    margin-bottom: 10px;\n" +
//        "    font-family: 'Oswald', sans-serif;\n" +
//        "    text-transform: uppercase;\n" +
//        "    border-radius: 4px;\n" +
//        "    position: relative\n" +
//        "  }\n" +
//        "\n" +
//        "  .card+.card {\n" +
//        "    margin-left: 2%\n" +
//        "  }\n" +
//        "\n" +
//        "  .date {\n" +
//        "    display: table-cell;\n" +
//        "    width: 25%;\n" +
//        "    position: relative;\n" +
//        "    text-align: center;\n" +
//        "    border-right: 2px dashed #e8e9eb\n" +
//        "  }\n" +
//        "\n" +
//        "  .date:before,\n" +
//        "  .date:after {\n" +
//        "    content: \"\";\n" +
//        "    display: block;\n" +
//        "    width: 30px;\n" +
//        "    height: 30px;\n" +
//        "    background-color: #e8e9eb;\n" +
//        "    position: absolute;\n" +
//        "    top: -15px;\n" +
//        "    right: -15px;\n" +
//        "    z-index: 1;\n" +
//        "    border-radius: 50%\n" +
//        "  }\n" +
//        "\n" +
//        "  .date:after {\n" +
//        "    top: auto;\n" +
//        "    bottom: -15px\n" +
//        "  }\n" +
//        "\n" +
//        "  .date time {\n" +
//        "    display: block;\n" +
//        "    position: absolute;\n" +
//        "    top: 50%;\n" +
//        "    left: 50%;\n" +
//        "    -webkit-transform: translate(-50%, -50%);\n" +
//        "    -ms-transform: translate(-50%, -50%);\n" +
//        "    transform: translate(-50%, -50%)\n" +
//        "  }\n" +
//        "\n" +
//        "  .date time span {\n" +
//        "    display: block\n" +
//        "  }\n" +
//        "\n" +
//        "  .date time span:first-child {\n" +
//        "    color: #2b2b2b;\n" +
//        "    font-weight: 600;\n" +
//        "    font-size: 250%\n" +
//        "  }\n" +
//        "\n" +
//        "  .date time span:last-child {\n" +
//        "    text-transform: uppercase;\n" +
//        "    font-weight: 600;\n" +
//        "    margin-top: -10px\n" +
//        "  }\n" +
//        "\n" +
//        "  .card-cont {\n" +
//        "    display: table-cell;\n" +
//        "    width: 75%;\n" +
//        "    font-size: 85%;\n" +
//        "    padding: 10px 10px 30px 50px\n" +
//        "  }\n" +
//        "\n" +
//        "  .card-cont h3 {\n" +
//        "    color: #3C3C3C;\n" +
//        "    font-size: 130%\n" +
//        "  }\n" +
//        "\n" +
//        "  .card-cont>div {\n" +
//        "    display: table-row\n" +
//        "  }\n" +
//        "\n" +
//        "  .card-cont .even-date i,\n" +
//        "  .card-cont .even-info i,\n" +
//        "  .card-cont .even-date time,\n" +
//        "  .card-cont .even-info p {\n" +
//        "    display: table-cell\n" +
//        "  }\n" +
//        "\n" +
//        "  .card-cont .even-date i,\n" +
//        "  .card-cont .even-info i {\n" +
//        "    padding: 5% 5% 0 0\n" +
//        "  }\n" +
//        "\n" +
//        "  .card-cont .even-info p {\n" +
//        "    padding: 30px 50px 0 0\n" +
//        "  }\n" +
//        "\n" +
//        "  .card-cont .even-date time span {\n" +
//        "    display: block\n" +
//        "  }\n" +
//        "\n" +
//        "  .card-cont a {\n" +
//        "    display: block;\n" +
//        "    text-decoration: none;\n" +
//        "    width: 80px;\n" +
//        "    height: 30px;\n" +
//        "    background-color: #D8DDE0;\n" +
//        "    color: #fff;\n" +
//        "    text-align: center;\n" +
//        "    line-height: 30px;\n" +
//        "    border-radius: 2px;\n" +
//        "    position: absolute;\n" +
//        "    right: 10px;\n" +
//        "    bottom: 10px\n" +
//        "  }\n" +
//        "\n" +
//        "  .row:last-child .card:first-child .card-cont a {\n" +
//        "    background-color: #037FDD\n" +
//        "  }\n" +
//        "\n" +
//        "  .row:last-child .card:last-child .card-cont a {\n" +
//        "    background-color: #F8504C\n" +
//        "  }\n" +
//        "\n" +
//        "  td {\n" +
//        "            border: 1px solid black;\n" +
//        "            padding: 10px;\n" +
//        "         }\n" +
//        "\n" +
//        "  @media screen and (max-width: 860px) {\n" +
//        "    .card {\n" +
//        "        display: block;\n" +
//        "        float: none;\n" +
//        "        width: 100%;\n" +
//        "        margin-bottom: 10px\n" +
//        "    }\n" +
//        "    .card+.card {\n" +
//        "        margin-left: 0\n" +
//        "    }\n" +
//        "    .card-cont .even-date,\n" +
//        "    .card-cont .even-info {\n" +
//        "        font-size: 75%\n" +
//        "    }\n" +
//        "  }\n" +
//        "  </style>\n" +
//        "</head>\n" +
//        "<body>\n" +
//        "  <h1>Pembelian Tiket Berhasil</h1>\n" +
//        "  <section class=\"container\">\n" +
//        "    <!--event information-->\n" +
//        "    <p>Detail Event</p>\n" +
//        "    <table>\n" +
//        "      <tr>\n" +
//        "        <td>Nama</td>\n" +
//        "        <td>Nama Peserta</td>\n" +
//        "      </tr>\n" +
//        "      <tr>\n" +
//        "        <td>Tanggal Event</td>\n" +
//        "        <td>2 Juni 2022</td>\n" +
//        "      </tr>\n" +
//        "    </table><br></br>\n" +
//        "    <h1>Tiket</h1>\n" +
//        "    <div class=\"row\">\n" +
//        "      <article class=\"card fl-left\">\n" +
//        "        <section class=\"date\">\n" +
//        "          <time datetime=\"23th feb\"><!--jenis tiket-->\n" +
//        "           Jenis Tiket</time>\n" +
//        "        </section>\n" +
//        "        <section class=\"card-cont\">\n" +
//        "          <!--nama peserta-->\n" +
//        "          <small>Nama Peserta</small> <!--nama event-->\n" +
//        "          <h3>Nama Event</h3>\n" +
//        "          <div class=\"even-date\">\n" +
//        "             <span>Rabu, 10 Desember 2022</span> <!--waktu event-->\n" +
//        "             <span>08:55 - 12:00</span>\n" +
//        "          </div>\n" +
//        "          <div class=\"even-info\">\n" +
//        "            <p><!--lokasi event-->\n" +
//        "             Lokasi Event</p>\n" +
//        "          </div><a href=\"#\">booked</a>\n" +
//        "        </section>\n" +
//        "      </article>\n" +
//        "    </div>\n" +
//        "  </section>\n" +
//        "</body>\n" +
//        "</html>";

//    final String emailMessage = "<html>\n" +
//        "    <body>\n" +
//        "        <h1>Hello Simple Solution</h1>\n" +
//        "        <img width=\"140\" src=\"https://simplesolution.dev/images/Logo_S_v1.png\" />\n" +
//        "     </body>\n" +
//        "</html>";
//
//    generatePdf(emailMessage);

    final String emailMessage = "<!DOCTYPE html>\n" +
        "<html>\n" +
        "  <head>\n" +
        "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n" +
        "    <title>'Tiket'</title>\n" +
        "    <style>\n" +
        "      .date:before{content:\"\";display:block;width:30px;height:30px;background-color:#e8e9eb;position:absolute;top:-15px;right:-15px;z-index:1;border-radius:50%}\n" +
        "      .date:after{content:\"\";display:block;width:30px;height:30px;background-color:#e8e9eb;position:absolute;top:-15px;right:-15px;z-index:1;border-radius:50%}\n" +
        "      .date:after{top:auto;bottom:-15px}\n" +
        "    </style>\n" +
        "    <style type=\"text/css\">\n" +
        "      @import url('https://fonts.googleapis.com/css?family=Oswald');\n" +
        "      @media screen and (max-width: 860px) {\n" +
        "          .card {\n" +
        "              display: block;\n" +
        "              float: none;\n" +
        "              width: 100%;\n" +
        "              margin-bottom: 10px\n" +
        "          }\n" +
        "          .card+.card {\n" +
        "              margin-left: 0\n" +
        "          }\n" +
        "          .card-cont .even-date,\n" +
        "          .card-cont .even-info {\n" +
        "              font-size: 75%\n" +
        "          }\n" +
        "      }\n" +
        "    </style>\n" +
        "  </head>\n" +
        "  <body style=\"margin:0;padding:0;border:0;box-sizing:border-box;background-color:#e8e9eb;font-family:arial;\">\n" +
        "    <h1 style=\"margin:0;padding:0;border:0;box-sizing:border-box;text-transform:uppercase;font-weight:900;border-left:10px solid #fec500;padding-left:10px;margin-bottom:10px;\">Pembelian Tiket Berhasil</h1>\n" +
        "    <section class=\"container\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;padding-top:20px;padding-bottom:20px;padding-right:20px;padding-left:20px;\">\n" +
        "      <!--event information-->\n" +
        "      <p style=\"margin:0;padding:0;border:0;box-sizing:border-box;padding-left:20px,;\"> Detail Event </p>\n" +
        "      <table style=\"margin:0;padding:0;border:0;box-sizing:border-box;\">\n" +
        "        <tr style=\"margin:0;padding:0;border:0;box-sizing:border-box;\">\n" +
        "          <td style=\"margin:0;padding:0;border:0;box-sizing:border-box;border:1px solid black;padding:10px;\">Nama</td>\n" +
        "          <td style=\"margin:0;padding:0;border:0;box-sizing:border-box;border:1px solid black;padding:10px;\">"+participantData.getName()+"</td>\n" +
        "        </tr>\n" +
        "        <tr style=\"margin:0;padding:0;border:0;box-sizing:border-box;\">\n" +
        "          <td style=\"margin:0;padding:0;border:0;box-sizing:border-box;border:1px solid black;padding:10px;\">Tanggal Event</td>\n" +
        "          <td style=\"margin:0;padding:0;border:0;box-sizing:border-box;border:1px solid black;padding:10px;\">"+dateTime+"</td>\n" +
        "        </tr>\n" +
        "      </table>\n" +
        "      <br />\n" +
        "      <h1 style=\"margin:0;padding:0;border:0;box-sizing:border-box;text-transform:uppercase;font-weight:900;border-left:10px solid #fec500;padding-left:10px;margin-bottom:10px;\">Tiket</h1>\n" +
        "      <div class=\"row\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;overflow:hidden;\">\n" +
        "        <article class=\"card fl-left\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;float:left;display:table-row;width:49%;background-color:#fff;color:#989898;margin-bottom:10px;font-family:'Oswald', sans-serif;text-transform:uppercase;border-radius:4px;position:relative;\">\n" +
        "          <section class=\"date\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:table-cell;width:25%;position:relative;text-align:center;border-right:2px dashed #e8e9eb;\">\n" +
        "            <time datetime=\"23th feb\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:block;position:absolute;top:50%;left:50%;-webkit-transform:translate(-50%, -50%);-ms-transform:translate(-50%, -50%);transform:translate(-50%, -50%);\">\n" +
        "              "+ticketParticipantData.getTicket().getName()+"\n" +
        "        </time>\n" +
        "          </section>\n" +
        "          <section class=\"card-cont\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:table-cell;width:75%;font-size:85%;padding:10px 10px 30px 50px;\">\n" +
        "            <small style=\"margin:0;padding:0;border:0;box-sizing:border-box;\">"+participantData.getName()+"</small>\n" +
        "            <h3 style=\"margin:0;padding:0;border:0;box-sizing:border-box;color:#3C3C3C;font-size:130%;\">"+eventData.getName()+"</h3>\n" +
        "            <div class=\"even-date\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:table-row;\">\n" +
        "              <i class=\"fa fa-calendar\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:table-cell;padding:5% 5% 0 0;\"></i>\n" +
        "              <time style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:table-cell;\">\n" +
        "                <!--tanggal event-->\n" +
        "                <span style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:block;\">"+dateTime+"</span>\n" +
        "                <!--waktu event-->\n" +
        "                <span style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:block;\">08:55 - 12:00</span>\n" +
        "              </time>\n" +
        "            </div>\n" +
        "            <div class=\"even-info\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:table-row;\">\n" +
        "              <i class=\"fa fa-map-marker\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:table-cell;padding:5% 5% 0 0;\"></i>\n" +
        "              <p style=\"margin:0;padding:0;border:0;box-sizing:border-box;padding-left:20px,;display:table-cell;padding:30px 50px 0 0;\">\n" +
        "                <!--lokasi event-->\n" +
        "                "+name+" "+address_line2+"\n" +
        "          </p>\n" +
        "            </div>\n" +
        "            <a href=\"#\" style=\"margin:0;padding:0;border:0;box-sizing:border-box;display:block;text-decoration:none;width:80px;height:30px;background-color:#D8DDE0;color:#fff;text-align:center;line-height:30px;border-radius:2px;position:absolute;right:10px;bottom:10px;background-color:#037FDD;background-color:#F8504C;\">booked</a>\n" +
        "          </section>\n" +
        "        </article>\n" +
        "      </div>\n" +
        "    </section>\n" +
        "  </body>\n" +
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

  public void generatePdf(String htmlContent){
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(htmlContent);
    renderer.layout();
    renderer.createPDF(outputStream, false);
    renderer.finishPDF();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
  }

}
