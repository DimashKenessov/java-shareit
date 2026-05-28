package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void create_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> requestService.create(99L, new ItemRequestDto()));
    }

    @Test
    void getOwn_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> requestService.getOwn(99L));
    }

    @Test
    void getById_requestNotFound_throwsNotFoundException() {
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> requestService.getById(1L, 99L));
    }
}