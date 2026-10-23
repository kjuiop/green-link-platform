package com.greenlink.core.application.auth.port;

import java.util.List;

public interface TokenProvider {
    String generateToken(Long id, List<String> roles);
    String generateToken(Long id, List<String> roles, Long companyId);
}
