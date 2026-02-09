package backend.capstone.domain.timeline.controller;

import backend.capstone.domain.timeline.gpspoint.service.GpsPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timeline")
public class TimeLineController {

    private final GpsPointService gpsPointService;


}
