package id.myevent.controller;

import id.myevent.model.apiresponse.ViewTicketReportApiResponse;
import id.myevent.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class DashboardController {

  @Autowired
  DashboardService dashboardService;

  @GetMapping("/events/{eventId}/dashboard/chart")
  public ViewTicketReportApiResponse getChart(@PathVariable("eventId") Long eventId) {
    return dashboardService.viewDashboardTicket(eventId);
  }
}
