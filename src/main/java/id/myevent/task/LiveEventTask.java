package id.myevent.task;

import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.repository.EventRepository;
import id.myevent.repository.EventStatusRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
public class LiveEventTask implements Runnable{
    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventStatusRepository eventStatusRepository;

    public EventDao getEvent() {
        return event;
    }

    public void setEvent(EventDao event) {
        this.event = event;
    }

    EventDao event;

    @Override
    public void run() {
        //update event status to live

        final EventStatusDao publishedEventStatus = eventStatusRepository.findById(3L).get();
        event.setEventStatus(publishedEventStatus);
        eventRepository.save(event);
        log.warn("update status menjadi live untuk event dengan id " + event.getId());

    }
}
