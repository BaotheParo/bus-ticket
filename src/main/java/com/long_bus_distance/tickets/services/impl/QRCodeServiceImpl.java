package com.long_bus_distance.tickets.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.long_bus_distance.tickets.entity.QRCode;
import com.long_bus_distance.tickets.entity.QRCodeStatusEnum;
import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.exception.QRCodeGenerationException;
import com.long_bus_distance.tickets.exception.QRCodeNotFoundException;
import com.long_bus_distance.tickets.repository.QRCodeRepository;
import com.long_bus_distance.tickets.services.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRCodeServiceImpl implements QRCodeService {
    private final QRCodeWriter qrCodeWriter;
    private final QRCodeRepository qrCodeRepository;

    @Override
    public QRCode generateQRCode(Ticket ticket) {
        log.info("Tạo QR cho Ticket ID: {}", ticket.getId());
        try {
            // Content QR: ticketId + selectedSeat
            String content = ticket.getId().toString() + "-" + ticket.getSelectedSeat();

            // Generate Base64 image
            String base64Image = generateQRCodeImage(content);

            // Tạo QR entity (id = null để JPA auto-gen, tránh detached)
            QRCode qrCode = QRCode.builder()
                    .id(null)  // Fix: Bỏ UUID.randomUUID(), let JPA gen
                    .value(base64Image)
                    .status(QRCodeStatusEnum.ACTIVE)
                    .ticket(ticket)
                    .build();

            QRCode saved = qrCodeRepository.saveAndFlush(qrCode);  // Bây giờ OK, no stale
            log.info("Tạo QR thành công ID: {}", saved.getId());
            return saved;
        } catch (WriterException | IOException e) {
            log.error("Lỗi tạo QR cho Ticket: {}", ticket.getId(), e);
            throw new QRCodeGenerationException("Lỗi tạo QR Code", e);
        }
    }

    @Override
    public byte[] getQRCodeImageForUserAndTicket(UUID userId, UUID ticketId) {
        log.info("Lấy QR cho Ticket ID: {} của user: {}", ticketId, userId);
        QRCode qrCode = qrCodeRepository.findByTicketIdAndTicketPurchaserId(ticketId, userId)
                .orElseThrow(() -> new QRCodeNotFoundException("QR không tìm thấy cho Ticket: " + ticketId));
        try {
            return Base64.getDecoder().decode(qrCode.getValue());
        } catch (IllegalArgumentException e) {
            log.error("Base64 QR không hợp lệ cho Ticket: {}", ticketId, e);
            throw new IllegalArgumentException("QR Code không hợp lệ", e);
        }
    }

    private String generateQRCodeImage(String content) throws WriterException, IOException {
        int width = 300;
        int height = 300;

        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}