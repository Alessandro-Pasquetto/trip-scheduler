package io.github.ale.tripscheduler.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
public class RegisterRequest {
    private String username;
    private String password;
}
