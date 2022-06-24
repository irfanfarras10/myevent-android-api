package id.myevent.model.dto;

import lombok.Data;

/** Ticket DTO. */
@Data
public class  TicketDto {
  private String name;
  private Long price;
  private Long dateTimeRegistrationStart;
  private Long dateTimeRegistrationEnd;
  private Long quotaPerDay;
  private Long quotaTotal;
}
