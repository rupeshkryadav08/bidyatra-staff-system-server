package com.bidyatra.staff.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Data @Document(collection="admin_otps")
public class OtpCode { @Id private String id; private String username; private String codeHash; private LocalDateTime expiresAt; private int attempts; }
