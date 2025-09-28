package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.GetTicketResponseDto;
import com.long_bus_distance.tickets.dto.ListTicketResponseDto;
import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.mapper.TicketMapper;
import com.long_bus_distance.tickets.services.QRCodeService;
import com.long_bus_distance.tickets.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;
    private final QRCodeService qrCodeService;

    //Liệt kê toàn bộ vé cho user đã xác thực
    @GetMapping
    public ResponseEntity<Page<ListTicketResponseDto>> listTickets(JwtAuthenticationToken authenticationToken, Pageable pageable){
        log.info("Received GET request to list ticket for user with pagination: {}", pageable);
        //Trích user ID từ JWT
        Jwt jwt = (Jwt) authenticationToken.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        //Gọi service để fetch ticket cho user
        Page<Ticket> tickets =ticketService.listTicketForUser(userId,pageable);
        //Ánh xạ Page<Ticket> vào Page<ListTicketResponseDto>
        Page<ListTicketResponseDto> responseDtos = tickets.map(ticketMapper::toListTicketResponseDto);
        //tra ve
        return ResponseEntity.ok(responseDtos);
    }
    //Lấy vé cụ thể theo user đã xác thực
    public ResponseEntity<GetTicketResponseDto> getTicket(@PathVariable UUID ticketId, JwtAuthenticationToken jwtAuthenticationToken){
        log.info("Reveived GET request for ticket ID: {} by user", ticketId);
        //Trích user ID từ JWT
        Jwt jwt = (Jwt) jwtAuthenticationToken.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        //Gọi service để fetch ticket cho user
        Optional<Ticket>optionalTicket = ticketService.getTicketForUser(ticketId,userId);
        //Ánh xạ kết quả vào ResponseEntity
        return optionalTicket.map(ticketMapper::toGetTicketResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.notFound().build());
    }

    //Lấy hình QR của một vé cụ thể
    @GetMapping("/{ticketId}/qrcodes")
    public ResponseEntity<byte[]> getTicketQRCode (@PathVariable UUID ticketId,
                                                   JwtAuthenticationToken authenticationToken){
        log.info("Received GET request for QR code of ticket ID: {}", ticketId);
        //Trích user ID từ JWT
        Jwt jwt = (Jwt) authenticationToken.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        //Gọi service để lấy hình QR
        byte[]qrCodeImage = qrCodeService.getQRCodeImageForUserAndTicket(userId,ticketId);
        //Đặt HTTP header cho phản hồi hình ảnh
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeImage.length);
        //Trả về phản hồi
        return ResponseEntity.ok().headers(headers)
                .body(qrCodeImage);
    }
}
