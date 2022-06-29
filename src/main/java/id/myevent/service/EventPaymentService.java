package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventPaymentDao;
import id.myevent.model.dto.EventPaymentDto;
import id.myevent.repository.EventPaymentRepository;
import id.myevent.repository.EventRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Event Payment Service.
 */
@Service
@Slf4j
public class EventPaymentService {

  @Autowired
  EventPaymentRepository eventPaymentRepository;

  @Autowired
  EventRepository eventRepository;

  /**
   * Create Event Payment.
   */
  public void create(Long eventId, EventPaymentDto paymentDto) {
    final EventDao eventData = eventRepository.findById(eventId).get();
    final EventPaymentDao payment = new EventPaymentDao();
    //insert payment
    payment.setType(paymentDto.getType());
    payment.setInformation(paymentDto.getInformation());
    payment.setEvent(eventData);
    try {
      eventPaymentRepository.save(payment);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
  }

  /**
   * Update Payment.
   */
  public void updatePayment(Long eventId, Long eventPaymentId, EventPaymentDto paymentData) {
    Optional<EventPaymentDao> currentPayment = eventPaymentRepository.findById(eventPaymentId);
    EventDao eventData = eventRepository.findById(eventId).get();
    EventPaymentDao newPayment = currentPayment.get();

    if (eventData.getEventStatus().getId() == 1) {
      if (paymentData.getType() != null) {
        newPayment.setType(paymentData.getType());
      }
      if (paymentData.getInformation() != null) {
        newPayment.setInformation(paymentData.getInformation());
      }
      try {
        eventPaymentRepository.save(newPayment);
      } catch (DataIntegrityViolationException exception) {
        String exceptionMessage = exception.getMostSpecificCause().getMessage();
        throw new ConflictException(exceptionMessage);
      }
    } else {
      throw new ConflictException("Event harus di status Draft");
    }
  }
}
