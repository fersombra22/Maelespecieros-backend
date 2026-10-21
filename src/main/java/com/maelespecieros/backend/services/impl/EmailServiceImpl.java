package com.maelespecieros.backend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.services.EmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${app.mail.from:ventas@maelespecieros.com}")
    private String mailFrom;

    @Override
    public void enviarComprobanteVenta(String destinatario, String numeroVenta, byte[] pdfBytes) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new BusinessException("El correo destinatario es obligatorio.");
        }

        if (mailSender == null || smtpUsername == null || smtpUsername.isBlank()) {
            log.warn("Intento de envío de email sin credenciales SMTP configuradas.");
            throw new BusinessException("El servicio de correo no está configurado en el servidor. Defina SMTP_USERNAME y SMTP_PASSWORD en el archivo de configuración o variables de entorno.");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destinatario.trim());
            helper.setFrom(mailFrom);
            helper.setSubject("Comprobante de compra #" + numeroVenta + " - Mael Especieros");

            String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <div style="text-align: center; border-bottom: 2px solid #2c3e50; padding-bottom: 15px;">
                        <h2 style="color: #2c3e50; margin: 0;">MAEL ESPECIEROS</h2>
                        <p style="color: #7f8c8d; margin: 5px 0 0 0; font-size: 14px;">Especias, Condimentos y Sabores Gourmet</p>
                    </div>
                    <div style="padding: 20px 0;">
                        <p style="font-size: 16px; color: #333;">¡Muchas gracias por su compra!</p>
                        <p style="font-size: 14px; color: #555; line-height: 1.5;">
                            Adjunto a este correo encontrará el comprobante digital correspondiente a su operación <strong>#%s</strong>.
                        </p>
                        <div style="background-color: #f8f9fa; border-left: 4px solid #3498db; padding: 12px; margin: 15px 0; font-size: 13px; color: #555;">
                            <em>Nota: Este documento es un comprobante de entrega y control comercial interno emitido en el punto de venta.</em>
                        </div>
                    </div>
                    <div style="text-align: center; border-top: 1px solid #eee; padding-top: 15px; font-size: 12px; color: #999;">
                        <p style="margin: 0;">Mael Especieros - Feria y Venta Mayorista/Minorista</p>
                    </div>
                </div>
            """.formatted(numeroVenta);

            helper.setText(htmlContent, true);

            String attachmentFilename = "Comprobante_" + numeroVenta + ".pdf";
            helper.addAttachment(attachmentFilename, new ByteArrayResource(pdfBytes));

            mailSender.send(mimeMessage);
            log.info("Comprobante de venta #{} enviado exitosamente por email a {}", numeroVenta, destinatario);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Error al enviar comprobante por correo a {}: {}", destinatario, e.getMessage(), e);
            throw new BusinessException("No se pudo enviar el correo: " + e.getMessage());
        }
    }
}
