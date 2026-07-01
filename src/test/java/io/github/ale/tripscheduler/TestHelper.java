package io.github.ale.tripscheduler;

import io.github.ale.tripscheduler.entity.UserAccount;
import io.github.ale.tripscheduler.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class TestHelper {

    protected static final class TestUserData {
        public static final String USERNAME = "testUsername";
        public static final String PASSWORD = "Test123!";
    }

    protected UserAccount testUser;

    @Autowired
    protected UserAccountRepository userAccountRepository;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected void registerTestUser() {
        UserAccount testUser = UserAccount.builder()
                .username(TestUserData.USERNAME)
                .password(passwordEncoder.encode(TestUserData.PASSWORD))
                .build();

        userAccountRepository.save(testUser);

        this.testUser = testUser;
    }

    protected void loginTestUser() throws Exception {
        registerTestUser();

        mockMvc.perform(post("/login")
                .param("username", TestUserData.USERNAME)
                .param("password", TestUserData.PASSWORD)
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
