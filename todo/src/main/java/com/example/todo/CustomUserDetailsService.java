package com.example.todo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final AppUserRepository appUserRepository;

  public CustomUserDetailsService(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AppUser user = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());

    return new User(user.getUsername(), user.getPassword(), authorities);
  }
}
