package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.dao.EventCategoryDao;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventOrganizerDao;
import id.myevent.model.dao.EventPaymentCategoryDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.model.dao.EventVenueCategoryDao;
import id.myevent.model.dto.EventDto;
import id.myevent.repository.EventCategoryRepository;
import id.myevent.repository.EventOrganizerRepository;
import id.myevent.repository.EventPaymentCategoryRepository;
import id.myevent.repository.EventRepository;
import id.myevent.repository.EventStatusRepository;
import id.myevent.repository.EventVenueCategoryRepository;
import id.myevent.util.GlobalUtil;
import id.myevent.util.ImageUtil;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** User Service. */
@Service
@Slf4j
public class EventService {
  @Autowired EventStatusRepository eventStatusRepository;
  @Autowired EventCategoryRepository eventCategoryRepository;

  @Autowired EventVenueCategoryRepository eventVenueCategoryRepository;
  @Autowired EventPaymentCategoryRepository eventPaymentCategoryRepository;
  @Autowired EventOrganizerRepository eventOrganizerRepository;
  @Autowired EventRepository eventRepository;

  @Autowired GlobalUtil globalUtil;

  /** insert event. */
  public void insertEvent(EventDto eventData) {
    EventDao newEvent = new EventDao();

    final Optional<EventStatusDao> eventStatus =
        eventStatusRepository.findById(eventData.getEventStatusId());
    final Optional<EventCategoryDao> eventCategory =
        eventCategoryRepository.findById(eventData.getEventCategoryId());
    final Optional<EventVenueCategoryDao> eventVenueCategory =
        eventVenueCategoryRepository.findById(eventData.getEventVenueCategoryId());
    Optional<EventPaymentCategoryDao> eventPaymentCategory = null;
    if (eventData.getEventPaymentCategoryId() != null) {
      eventPaymentCategory =
          eventPaymentCategoryRepository.findById(eventData.getEventPaymentCategoryId());
    }
    final Optional<EventOrganizerDao> eventOrganizer =
        eventOrganizerRepository.findById(eventData.getEventOrganizerId());

    newEvent.setName(eventData.getName());
    newEvent.setDescription(eventData.getDescription());
    newEvent.setDateTimeEventStart(eventData.getDateTimeEventStart());
    newEvent.setDateTimeEventEnd(eventData.getDateTimeEventEnd());
    newEvent.setVenue(eventData.getVenue());
    newEvent.setBannerPhoto(ImageUtil.compressImage(eventData.getBannerPhoto()));
    newEvent.setDateTimeRegistrationStart(eventData.getDateTimeRegistrationStart());
    newEvent.setDateTimeRegistrationEnd(eventData.getDateTimeRegistrationEnd());
    newEvent.setEventStatus(eventStatus.get());
    newEvent.setEventCategory(eventCategory.get());
    newEvent.setEventVenueCategory(eventVenueCategory.get());
    if (eventPaymentCategory != null) {
      newEvent.setEventPaymentCategory(eventPaymentCategory.get());
    }
    newEvent.setEventOrganizer(eventOrganizer.get());
    try {
      validateEventData(eventData);
      eventRepository.save(newEvent);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      String message = null;
      if (exceptionMessage.contains("name")) {
        message = "Event sudah dibuat sebelumnya";
      }
      throw new ConflictException(message);
    }
  }

  private void validateEventData(EventDto event) {
    if (globalUtil.isBlankString(event.getName())) {
      throw new ConflictException("Nama event harus diisi");
    }
    if (globalUtil.isBlankString(event.getDescription())) {
      throw new ConflictException("Deskripsi event harus diisi");
    }
    if (event.getDateTimeEventStart() == null) {
      throw new ConflictException("Tanggal mulai event harus diisi");
    }
    if (event.getDateTimeEventStart() > event.getDateTimeEventEnd()) {
      throw new ConflictException("Tanggal mulai tidak boleh melebihi tanggal selesai");
    }
    if (event.getDateTimeEventEnd() == null) {
      throw new ConflictException("Tanggal berakhir event harus diisi");
    }
    if (event.getDateTimeEventEnd() < event.getDateTimeEventStart()) {
      throw new ConflictException("Tanggal selesai tidak boleh sebelum dari tanggal mulai");
    }
    if (event.getBannerPhoto() == null) {
      throw new ConflictException("Foto event harus di unggah");
    }
    if (event.getDateTimeRegistrationStart() == null) {
      throw new ConflictException("Tanggal mulai registrasi harus diisi");
    }
    if (event.getDateTimeRegistrationStart() < event.getDateTimeRegistrationEnd()) {
      throw new ConflictException(
          "Tanggal mulai registrasi tidak boleh sebelum dari tanggal selesai registrasi");
    }
    if (event.getDateTimeRegistrationStart() > event.getDateTimeEventStart()) {
      throw new ConflictException(
          "Tanggal mulai registrasi tidak boleh melebihi tinggal mulai event");
    }
    if (event.getDateTimeRegistrationStart() > event.getDateTimeEventEnd()) {
      throw new ConflictException(
          "Tanggal mulai registrasi tidak boleh melebihi tinggal selesai event");
    }
    if (event.getDateTimeRegistrationEnd() == null) {
      throw new ConflictException("Tanggal selesai registrasi harus diisi");
    }
    if (event.getDateTimeRegistrationEnd() < event.getDateTimeRegistrationStart()) {
      throw new ConflictException(
          "Tanggal selesai registrasi tidak boleh sebelum dari tanggal mulai registrasi");
    }
    if (event.getDateTimeRegistrationEnd() > event.getDateTimeEventStart()) {
      throw new ConflictException(
          "Tanggal selesai registrasi tidak boleh melebihi tanggal mulai event");
    }
    if (event.getDateTimeRegistrationEnd() > event.getDateTimeEventEnd()) {
      throw new ConflictException(
          "Tanggal selesai registrasi tidak boleh melebihi tanggal selesai event");
    }
    if (event.getEventStatusId() == null) {
      throw new ConflictException("Status event harus dilpilih");
    }
    if (event.getEventCategoryId() == null) {
      throw new ConflictException("Kategori event harus dilpilih");
    }
    if (event.getEventVenueCategoryId() == null) {
      throw new ConflictException("Jenis tempat event harus dilpilih");
    }
    if (event.getEventPaymentCategoryId() == null) {
      throw new ConflictException("Jenis pembayaran event harus dilpilih");
    }
    if (event.getEventOrganizerId() == null) {
      throw new ConflictException("Tidak terdapat event organizer ID");
    }
  }
}
