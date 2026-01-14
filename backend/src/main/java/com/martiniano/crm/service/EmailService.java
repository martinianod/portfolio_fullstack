package com.martiniano.crm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final JavaMailSender mailSender;
  
  @Value("${app.contact.recipient}")
  private String contactRecipient;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendContactEmail(String name, String email, String message) {
    try {
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setTo(contactRecipient);
      msg.setReplyTo(email);
      msg.setSubject("Nuevo contacto desde portfolio - " + name);
      msg.setText("Nombre: " + name + "\nEmail: " + email + "\n\nMensaje:\n" + message);
      mailSender.send(msg);
    } catch (Exception e) {
      // Log but don't fail - email is optional feature
      System.err.println("Failed to send email: " + e.getMessage());
    }
  }
}
