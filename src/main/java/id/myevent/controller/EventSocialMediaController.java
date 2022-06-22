package id.myevent.controller;

import id.myevent.model.dao.EventSocialMediaDao;
import id.myevent.service.EventSocialMediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventSocialMediaController {

    @Autowired
    EventSocialMediaService eventSocialMediaService;

    @GetMapping("/events/social-media")
    public List<EventSocialMediaDao> getSocialMedia() {
        return eventSocialMediaService.getEventSocialMedia();
    }
}
