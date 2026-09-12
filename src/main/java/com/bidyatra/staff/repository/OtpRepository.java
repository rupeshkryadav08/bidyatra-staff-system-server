package com.bidyatra.staff.repository;
import com.bidyatra.staff.model.OtpCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
public interface OtpRepository extends MongoRepository<OtpCode,String>{ Optional<OtpCode> findTopByUsernameOrderByExpiresAtDesc(String username); void deleteByUsername(String username); }
