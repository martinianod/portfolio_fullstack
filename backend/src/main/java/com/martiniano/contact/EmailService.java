package com.martiniano.contact;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendContactEmail(ContactDto dto) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(System.getenv().getOrDefault("CONTACT_RECIPIENT","your.email@domain.com"));
    msg.setReplyTo(dto.getEmail());
    msg.setSubject("Contacto desde landing - " + dto.getName());
    msg.setText("Nombre: " + dto.getName() + "\nEmail: " + dto.getEmail() + "\n\nMensaje:\n" + dto.getMessage());
    mailSender.send(msg);
  }
}
