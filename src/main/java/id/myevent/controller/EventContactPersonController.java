package id.myevent.controller;

import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.dto.EventContactPersonDto;
import id.myevent.service.EventContactPersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Event Contact Person Controller.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventContactPersonController {
    @Autowired
    EventContactPersonService eventContactPersonService;

    /** Create Contact Person*/
    @PostMapping("/events/{id}/contact-person/create")
    public ResponseEntity create(
            @PathVariable Long id,
            @RequestBody EventContactPersonDto eventContactPersonDto) {
        eventContactPersonService.create(id, eventContactPersonDto);
        return new ResponseEntity(new ApiResponse("Contact Person Tersimpan"), HttpStatus.CREATED);
    }

    /** Update Contact Person*/
    @PutMapping("/events/{id}/contact-person/{id}")
    public ResponseEntity<ApiResponse> updateCP(@PathVariable("id") Long eventId,
                                                @PathVariable("id") Long cpId,
                                                @RequestBody EventContactPersonDto eventContactPersonDto) {
        eventContactPersonService.update(eventId, cpId, eventContactPersonDto);
        return new ResponseEntity(new ApiResponse("Contact Person Berhasil di Update"), HttpStatus.CREATED);
    }
}
