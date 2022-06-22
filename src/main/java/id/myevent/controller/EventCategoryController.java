package id.myevent.controller;

import id.myevent.model.dao.EventCategoryDao;
import id.myevent.service.EventCategoryService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Event Category Controller. */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventCategoryController {

  @Autowired
  EventCategoryService eventCategoryService;

  @GetMapping("/events/category")
  public List<EventCategoryDao> getCategoryEvent() {
    return eventCategoryService.getEventCategory();
  }
}
