package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.BookingSeatRequest;
import com.long_bus_distance.tickets.dto.BulkPurchaseRequestDto;
import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.repository.DeckRepository;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.QRCodeService;
import com.long_bus_distance.tickets.services.VNPayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkTicketPurchaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private DeckRepository deckRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private QRCodeService qrCodeService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private VNPayService vnPayService;
    @Mock
    private RLock rLock;
    @Mock
    private RLock multiLock;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private UUID userId;
    private UUID tripId;
    private UUID deckId;
    private User user;
    private Trip trip;
    private Deck deck;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        deckId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        trip = new Trip();
        trip.setId(tripId);
        trip.setBasePrice(100.0);

        deck = new Deck();
        deck.setId(deckId);
        deck.setLabel("A");
        deck.setPriceFactor(1.0);
        deck.setTrip(trip);
    }

    @Test
    void purchaseBulkTickets_Success() throws InterruptedException {
        // Arrange
        BulkPurchaseRequestDto request = new BulkPurchaseRequestDto();
        List<BookingSeatRequest> seats = new ArrayList<>();

        BookingSeatRequest seat1 = new BookingSeatRequest();
        seat1.setTripId(tripId);
        seat1.setDeckId(deckId);
        seat1.setSelectedSeat("1");
        seats.add(seat1);

        BookingSeatRequest seat2 = new BookingSeatRequest();
        seat2.setTripId(tripId);
        seat2.setDeckId(deckId);
        seat2.setSelectedSeat("2");
        seats.add(seat2);

        request.setBookingSeats(seats);

        // Relaxed matchers to avoid strict stubbing errors
        lenient().when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        lenient().when(deckRepository.findById(any(UUID.class))).thenReturn(Optional.of(deck));
        lenient().when(deckRepository.getReferenceById(any(UUID.class))).thenReturn(deck);
        lenient().when(tripRepository.findById(any(UUID.class))).thenReturn(Optional.of(trip));

        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);

        // Stub for 2 arguments (varargs expanded)
        lenient().when(redissonClient.getMultiLock(any(RLock.class), any(RLock.class))).thenReturn(multiLock);

        lenient().when(multiLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        lenient().when(multiLock.isHeldByCurrentThread()).thenReturn(true);

        lenient()
                .when(ticketRepository.countByTripIdAndDeckIdAndSelectedSeatAndStatusIn(any(), any(), anyString(),
                        anyList()))
                .thenReturn(0L);

        lenient().when(vnPayService.createPaymentUrl(anyString(), anyDouble())).thenReturn("http://payment-url");

        // Act
        String result = ticketService.purchaseBulkTickets(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("http://payment-url", result);

        // Verify locks
        verify(redissonClient, times(2)).getLock(anyString());
        verify(redissonClient).getMultiLock(any(RLock.class), any(RLock.class));
        verify(multiLock).tryLock(5, 10, TimeUnit.SECONDS);
        verify(multiLock).unlock();

        // Verify save
        verify(ticketRepository).saveAll(argThat(list -> ((List<Ticket>) list).size() == 2));
    }
}
