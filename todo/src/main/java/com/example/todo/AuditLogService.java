package com.example.todo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

  private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(String action, String detail) {
    log.info("AUDIT action={} detail={}", action, detail);
  }
}
