package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return toDto(requestRepository.save(request), List.of());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        List<ItemRequest> requests = requestRepository.findAllByRequestor(requestor, SORT_BY_CREATED_DESC);
        return attachItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        List<ItemRequest> requests = requestRepository.findAllByRequestorNot(user, SORT_BY_CREATED_DESC);
        return attachItems(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        List<ItemDto> items = itemRepository.findAllByRequest(request).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return toDto(request, items);
    }

    private List<ItemRequestDto> attachItems(List<ItemRequest> requests) {
        Map<Long, List<ItemDto>> itemsByRequest = itemRepository.findAllByRequestIn(requests)
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                ));
        return requests.stream()
                .map(r -> toDto(r, itemsByRequest.getOrDefault(r.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private ItemRequestDto toDto(ItemRequest request, List<ItemDto> items) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items
        );
    }
}