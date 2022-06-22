package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.dto.EventGuestDto;
import id.myevent.service.EventGuestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Event Guest Controller. */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventGuestController {

    @Autowired
    EventGuestService eventGuestService;

    @PostMapping("/events/{id}/guest/create")
    public ResponseEntity create(@PathVariable("id") Long id, @RequestBody EventGuestDto guestDto) {
        eventGuestService.create(id, guestDto);
        return new ResponseEntity(new ApiResponse("Undangan Tersimpan"), HttpStatus.CREATED);
    }
}
