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
public class QRCodeServiceImpl implements QRCodeService{
    private final QRCodeWriter qrCodeWriter;
    private final QRCodeRepository qrCodeRepository;

    @Override
    public QRCode generateQRCode(Ticket ticket) {
        log.info("Generating QR code for ticket ID: {}", ticket.getId());
        try {
            //Tạo ra UUID random cho QR
            UUID uniqueId= UUID.randomUUID();

            //Tạo ra ảnh QR
            String base64Image= generateQRCodeImage(uniqueId.toString());

            //Tạo và điền thông tin QR
            QRCode qrCode =new QRCode();
            qrCode.setId(uniqueId);
            qrCode.setValue(base64Image);
            qrCode.setStatus(QRCodeStatusEnum.ACTIVE);
            qrCode.setTicket(ticket);

            //Lưu QR vào DB
            QRCode savedQRCode = qrCodeRepository.saveAndFlush(qrCode);
            log.info("Successfully generated and saved QR code with ID: {}",savedQRCode.getId());
            return savedQRCode;

        }catch (WriterException | IOException e){
            log.error("Failed to generate QR code for ticket ID: {}",ticket.getId(),e);
            throw new QRCodeGenerationException("Error generating QR code", e);
        }
    }
    //Phương thức tạo ảnh QRcode từ chuỗi Base64
    private String generateQRCodeImage(String content) throws WriterException, IOException {
        //Kích thước mã QR
        int qrWidth = 300;
        int qrHeight = 300;

        //Mã hóa vào BitMatrix
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrWidth,qrHeight);

        //Chuyển từ BitMatrix sang BufferedImage
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        //Tạo BufferedImage sang ByteArrayOutputStream từ PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage,"png", baos);

        //Mã hóa byte thành chuỗi Bas64
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
