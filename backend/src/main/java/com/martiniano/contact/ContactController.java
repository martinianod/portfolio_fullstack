package com.martiniano.contact;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

  private final EmailService emailService;

  public ContactController(EmailService emailService) {
    this.emailService = emailService;
  }

  @PostMapping
  public ResponseEntity<?> receiveContact(@Valid @RequestBody ContactDto dto) {
    try {
      emailService.sendContactEmail(dto);
      return ResponseEntity.ok().build();
    } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.status(500).body("Error sending email");
    }
  }
}
