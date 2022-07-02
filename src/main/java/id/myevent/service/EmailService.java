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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import javax.swing.ImageIcon;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
   * Invite Guest.
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

      //File image =
      //     getImage(eventData.getBannerPhoto(), eventData.getBannerPhotoName());

//      messageHelper.addInline("myfoto", image);
//
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

    DateFormat sdf = new SimpleDateFormat("EEEE, dd. MMMM yyyy HH:mm");
    String dateTime = sdf.format(eventData.getTimeEventStart());

    String lat = StringUtils.substringBefore(eventData.getVenue(), "|");
    String lon = StringUtils.substringAfter(eventData.getVenue(), "|");

    Location loc = getLocation(lat, lon);
    String name = loc.features.get(0).properties.name;
    String address_line2 = loc.features.get(0).properties.address_line2;

    String img = "https://myevent-android-api.herokuapp.com/api/events/image/"+eventData.getBannerPhotoName();

    final String emailMessage = "<html>\n"
        + "<body>\n"
        + "    <p>Kepada Bapak/Ibu,</p>\n"
        + "    <p>Kami ingin mengundang anda ke acara " + eventData.getName()
        + " yang diselenggarakan oleh " + eventData.getEventOrganizer().getOrganizerName()
        + ". Event tersebut akan dilaksanakan pada: </p>\n"
        + "    <p><b>Hari, Tanggal:</b> " + dateTime + "</p>\n"
        + "    <p><b>Tempat:</b> " + name + " " + address_line2 + "</p>\n"
        +
        "    <p>Demikian undangan ini disampaikan, kami berharap kedatangan Bapak/Ibu pada acara "
        + "kami.</p>\n"
        + "    <p>Untuk informasi lebih lanjut silakan menghubungi tim dari "
        + eventData.getEventOrganizer().getOrganizerName() + ".</p>\n"
        + "<src="+img
        + "         width=150\" height=\"70\">"
        + "</body>\n"
        + "</html>";

    return emailMessage;
  }

  public Location getLocation(String lat, String lon) {

    String url =
        "https://api.geoapify.com/v1/geocode/reverse" + "?lat=" + lat + "&lon=" + lon + "&apiKey=" +
            "26018a31a0aa41699818b7b50ea82935";
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
    //InputStream is = new ByteArrayInputStream(imageByte);
    ByteArrayInputStream inStreambj = new ByteArrayInputStream(imageByte);
    BufferedImage newBi = ImageIO.read(inStreambj);

    // save it
    ImageIO.write(newBi, "png", new File(fileLocation));
  }


}
