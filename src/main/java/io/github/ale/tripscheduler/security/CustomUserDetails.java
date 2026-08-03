package io.github.ale.tripscheduler.security;

import io.github.ale.tripscheduler.entity.UserAccount;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.List;

@Getter
public class CustomUserDetails extends User {

    private final Long id;

    public CustomUserDetails(UserAccount user) {
        super(
            user.getUsername(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("USER"))
        );
        this.id = user.getId();
    }
}