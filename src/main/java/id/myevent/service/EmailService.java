package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventGuestDao;
import id.myevent.model.location.Feature;
import id.myevent.model.location.Location;
import id.myevent.model.location.Properties;
import id.myevent.repository.EventGuestRepository;
import id.myevent.repository.EventRepository;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletOutputStream;
import javax.swing.ImageIcon;
import javax.xml.bind.ValidationException;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
      MimeMessage message = javaMailSender.createMimeMessage();

      MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

      // get multiple email
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
   * Invite Guest.
   */
  public void invite(Long eventId, Long guestId) throws IOException {
    final EventDao eventData = eventRepository.findById(eventId).get();
    EventGuestDao guestData = eventGuestRepository.findById(guestId).get();

    final String emailMessage = mailMessage(eventData);

    generateIcs(eventData);

    try {
      MimeMessage message = javaMailSender.createMimeMessage();

      MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

      messageHelper.setTo(guestData.getEmail());
      messageHelper.setCc(eventData.getEventOrganizer().getEmail());
      messageHelper.setSubject("Event Invitation - " + eventData.getName());
      messageHelper.setText(emailMessage, true);

      //File ics = generateIcs(eventData);

      //messageHelper.addInline("myfoto", image);
      //
      javaMailSender.send(message);

      // change email status in Event Guest
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
            +
            "    <p>Demikian undangan ini disampaikan, kami berharap kedatangan Bapak/Ibu pada "
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

  private void getImage(byte[] imageByte, String name) throws IOException {

    //    File convertFile = new File("/photos/"+name);
    //    convertFile.getParentFile().mkdirs();
    //    convertFile.createNewFile();
    //    log.warn(String.valueOf(convertFile.getParentFile().mkdirs()));
    //    FileOutputStream fout = new FileOutputStream(convertFile);
    //    fout.write(imageByte);
    //    fout.close();

    String fileLocation = new File("photos").getAbsolutePath() + "\\" + name;
    Path target = Paths.get(fileLocation);
    //  FileOutputStream fos = new FileOutputStream(fileLocation);

    //    BufferedImage bImage = ImageIO.read(new File(fileLocation));
    //    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //    ImageIO.write(bImage, "png", bos );
    //    byte [] data = bos.toByteArray();
    //    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    //    BufferedImage bImage2 = ImageIO.read(bis);
    //    ImageIO.write(bImage2, "png", new File("output.png") );
    //    System.out.println("image created");

    // convert byte[] back to a BufferedImage
    // InputStream is = new ByteArrayInputStream(imageByte);
    ByteArrayInputStream inStreambj = new ByteArrayInputStream(imageByte);
    BufferedImage newBi = ImageIO.read(inStreambj);

    // save it
    ImageIO.write(newBi, "png", new File(fileLocation));
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
    VEvent event = new VEvent(start, end, eventSummary);
    event.add(uid);

    //create calendar
    Calendar calendar = new Calendar();
    calendar.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.add(Version.VERSION_2_0);
    calendar.add(CalScale.GREGORIAN);

    /* Add event to calendar */
    calendar.add(event);

    String filePath = new File("photos").getAbsolutePath() + "\\" + "mymeeting.ics";
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
