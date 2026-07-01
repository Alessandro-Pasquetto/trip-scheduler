package io.github.ale.tripscheduler;

import io.github.ale.tripscheduler.entity.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthTest extends TestHelper {

    @Test
    void registerSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "%s",
                        "password": "%s",
                        "confirmPassword": "%s"
                    }
                    """.formatted(
                            TestUserData.USERNAME,
                            TestUserData.PASSWORD,
                            TestUserData.PASSWORD
                    ))
                .with(csrf()))
                .andExpect(status().isOk());

        UserAccount checkUser = userAccountRepository.findByUsername(TestUserData.USERNAME).orElseThrow();
        assertTrue(passwordEncoder.matches(TestUserData.PASSWORD, checkUser.getPassword()));
    }

    @Test
    void loginSuccess() throws Exception {
        registerTestUser();

        mockMvc.perform(post("/login")
                .param("username", TestUserData.USERNAME)
                .param("password", TestUserData.PASSWORD)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(authenticated().withUsername(TestUserData.USERNAME));
    }
}