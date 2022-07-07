package id.myevent.model.apiresponse;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class DateEvent {
  private List<LocalDate> localDates;
}
