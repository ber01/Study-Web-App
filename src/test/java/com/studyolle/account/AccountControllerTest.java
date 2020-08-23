package com.studyolle.account;

import com.studyolle.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    private JavaMailSender javaMailSender;

    @DisplayName("회원 가입 화면 폼 테스트")
    @Test
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
        ;
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    public void singUpSubmitWithWrongInput() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "kyunghwan")
                .param("email", "email..")
                .param("password", "12345")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
        ;
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    public void singUpSubmitWithCorrectInput() throws Exception {
        String email = "ksyj8256@gmail.com";
        String password = "12345678";

        mockMvc.perform(post("/sign-up")
                .param("nickname", "kyunghwan")
                .param("email", email)
                .param("password", password)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
        ;

        Account account = accountRepository.findByEmail(email);
        assertThat(account).isNotNull();
        assertThat(account.getPassword()).isNotEqualTo(password);

        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

}