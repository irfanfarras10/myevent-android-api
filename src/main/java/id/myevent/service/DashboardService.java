package id.myevent.service;

import id.myevent.model.apiresponse.ViewTicketReportApiResponse;
import id.myevent.model.dao.TicketParticipantDao;
import id.myevent.repository.TicketParticipantRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Dashboard Service.
 */
@Service
@Slf4j
public class DashboardService {

  @Autowired
  TicketParticipantRepository ticketParticipantRepository;

  public ViewTicketReportApiResponse viewDashboardTicket(Long eventId){
    List<TicketParticipantDao> tickets =
        (List<TicketParticipantDao>) ticketParticipantRepository.purchaseByDate(eventId);

    ViewTicketReportApiResponse viewTicketReportApiResponse = new ViewTicketReportApiResponse();
    viewTicketReportApiResponse.setTicketReportList(tickets);
    return viewTicketReportApiResponse;
  }
}
