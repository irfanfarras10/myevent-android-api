package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.dao.*;
import id.myevent.model.dto.TicketDto;
import id.myevent.repository.EventPaymentCategoryRepository;
import id.myevent.repository.EventRepository;
import id.myevent.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Ticket Service. */
@Service
@Slf4j
public class TicketService {
  @Autowired TicketRepository ticketRepository;

  @Autowired EventPaymentCategoryRepository eventPaymentCategoryRepository;

  @Autowired EventRepository eventRepository;

  /** Create Ticket. */
  public void create(Long eventId, TicketDto ticketData) {
    // update event data (payment category and date time registration start and end)
    final EventDao eventData = eventRepository.findById(eventId).get();
    final Long eventCategoryId = ticketData.getPrice() > 0 ? 2L : 1L;
    final EventPaymentCategoryDao eventPaymentCategory =
        eventPaymentCategoryRepository.findById(eventCategoryId).get();

    eventData.setEventPaymentCategory(eventPaymentCategory);
    eventData.setDateTimeRegistrationStart(ticketData.getDateTimeRegistrationStart());
    eventData.setDateTimeRegistrationEnd(ticketData.getDateTimeRegistrationEnd());

    validateTicket(ticketData, eventData);
    eventRepository.save(eventData);

    // insert ticket data
    final TicketDao ticket = new TicketDao();
    ticket.setName(ticketData.getName());
    ticket.setPrice(ticketData.getPrice());
    if (ticketData.getQuotaPerDay() != null) {
      ticket.setQuotaPerDay(ticketData.getQuotaPerDay());
    }
    ticket.setQuotaTotal(ticketData.getQuotaTotal());
    ticket.setEvent(eventData);
    try {
      validateTicket(ticketData, eventData);
      ticketRepository.save(ticket);
    } catch (DataIntegrityViolationException exception) {
      String exceptionMessage = exception.getMostSpecificCause().getMessage();
      throw new ConflictException(exceptionMessage);
    }
  }

  /** Update Ticket*/
  public void updateTicket(Long eventId, Long ticketId, TicketDto ticketData){
    Optional<TicketDao> currentTicket = ticketRepository.findById(ticketId);
    EventDao eventData = eventRepository.findById(eventId).get();
    TicketDao newTicket = currentTicket.get();

    if(eventData.getEventStatus().getId()<=2){

      if(eventData.getEventStatus().getId()==1){
        if (ticketData.getPrice()!=null){
          final Long eventCategoryId = ticketData.getPrice() > 0 ? 2L : 1L;
          final EventPaymentCategoryDao eventPaymentCategory =
                  eventPaymentCategoryRepository.findById(eventCategoryId).get();
          eventData.setEventPaymentCategory(eventPaymentCategory);
        }
        if(ticketData.getDateTimeRegistrationStart()!=null && ticketData.getDateTimeRegistrationEnd()!=null){
          eventData.setDateTimeRegistrationStart(ticketData.getDateTimeRegistrationStart());
          eventData.setDateTimeRegistrationEnd(ticketData.getDateTimeRegistrationEnd());
          validateTicket(ticketData,eventData);
        }
        eventRepository.save(eventData);
      }

      if(ticketData.getName() != null){
        newTicket.setName(ticketData.getName());
      }
      if(ticketData.getPrice() != null){
        newTicket.setPrice(ticketData.getPrice());
      }
      if(ticketData.getQuotaPerDay() != null){
        newTicket.setQuotaPerDay(ticketData.getQuotaPerDay());
      }
      if(ticketData.getQuotaTotal() != null){
        newTicket.setQuotaTotal(ticketData.getQuotaTotal());
      }
      try {
        ticketRepository.save(newTicket);
      } catch (DataIntegrityViolationException exception) {
        String exceptionMessage = exception.getMostSpecificCause().getMessage();
        throw new ConflictException(exceptionMessage);
      }
    }

  }

  private void validateTicket(TicketDto ticketData, EventDao eventData) {
    if (ticketData.getDateTimeRegistrationStart() >= ticketData.getDateTimeRegistrationEnd()) {
      throw new ConflictException(
          "Tanggal registrasi awal tidak boleh melebihi tanggal registrasi akhir");
    }
    if (ticketData.getDateTimeRegistrationStart() >= eventData.getDateTimeEventStart()) {
      throw new ConflictException("Tanggal registrasi harus sebelum tanggal pelaksanaan event");
    }
  }
}
