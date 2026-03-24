package backend.capstone.domain.pinmarkplace.service;

import backend.capstone.domain.pinmarkplace.dto.PinmarkPlaceCreateRequest;
import backend.capstone.domain.pinmarkplace.dto.PinmarkPlaceCreateResponse;
import backend.capstone.domain.pinmarkplace.entity.PinmarkPlace;
import backend.capstone.domain.pinmarkplace.mapper.PinmarkPlaceMapper;
import backend.capstone.domain.pinmarkplace.repository.PinmarkPlaceRepository;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PinmarkPlaceService {

    private final PinmarkPlaceRepository pinmarkPlaceRepository;
    private final UserService userService;

    @Transactional
    public PinmarkPlaceCreateResponse createPinmarkPlace(Long userId,
        PinmarkPlaceCreateRequest request) {
        User user = userService.findById(userId);
        PinmarkPlace pinmarkPlace = PinmarkPlaceMapper.toEntity(user, request);
        PinmarkPlace savedPinmarkPlace = pinmarkPlaceRepository.save(pinmarkPlace);

        return PinmarkPlaceMapper.toCreateResponse(savedPinmarkPlace);
    }
}
