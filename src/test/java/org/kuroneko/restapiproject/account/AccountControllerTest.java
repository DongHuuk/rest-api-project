package org.kuroneko.restapiproject.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.account.domain.AccountPasswordForm;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.config.WithAccount;
import org.kuroneko.restapiproject.notification.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
<<<<<<< HEAD
=======
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
>>>>>>> 2610874ce5b33ab3ae254bb431af6eb7170d45d0
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
class AccountControllerTest extends AccountMethods{

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private CommentsRepository commentsRepository;
    @Autowired private NotificationRepository notificationRepository;

    @AfterEach
    private void deleteAccountRepository_After(){
        this.notificationRepository.deleteAll();
        this.commentsRepository.deleteAll();
        this.articleRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Account 생성 - 201")
    public void createAccount_201() throws Exception {
        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("TestUser");
        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("create-Account",
                    links(
                            linkWithRel("Account Profile").description("Account's Profile"),
                            linkWithRel("get Articles").description("Account's find Articles"),
                            linkWithRel("get Comments").description("Account's find Comments"),
                            linkWithRel("get Notification").description("Account's find Notification"),
                            linkWithRel("DOCS").description("REST API DOCS")
                    ),
                    requestHeaders(
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON을 지원한다."),
                            headerWithName(HttpHeaders.ACCEPT).description("이 API에서는 HAL을 지원한다.")
                    ),
                    requestFields(
                            fieldWithPath("username").description("생성할 계정의 닉네임"),
                            fieldWithPath("email").description("생성할 계정의 아이디(로그인시 사용), 특수문자는 허용하지 않는다."),
                            fieldWithPath("password").description("생성할 계정의 비밀번호 8-12자, 문자 규칙은 없다."),
                            fieldWithPath("checkingPassword").description("생성할 계정의 비밀번호를 확인 할 비밀번호.")
                    )
                ));
    }

    @Test
    @DisplayName("Account 생성 실패_415 error(MediaType 미지원)")
    public void createAccount_415_error() throws Exception{
        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaTypes.HAL_JSON)
                .param("username", "흑우냥이")
                .param("password", "12341234")
                .param("checkingPassword","12341234")
                .param("email", "test@gmail.com")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Account 생성 실패_400 error(Validator_password)")
    public void createAccount_error_validator() throws Exception{
        AccountForm accountForm = createAccountForm();
        accountForm.setPassword("123456");

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("Account 생성 실패_400 error(Validator_duplicate_email)")
    public void createAccount_error_emailDuplicate() throws Exception{
        AccountForm accountForm = createAccountForm();
        saveAccount(accountForm);
        accountForm.setUsername("테스트1");

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andDo(document("create-Account-Errors"));
    }

    @Test
    @DisplayName("Account 생성 실패_400 error(Validator_duplicate_username)")
    public void createAccount_error_usernameDuplicate() throws Exception{
        AccountForm accountForm = createAccountForm();
        saveAccount(accountForm);
        accountForm.setEmail("test2@gmail.com");

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("Account 조회 성공_success")
    @WithAccount("Test@naver.com")
    public void sendAccount() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        this.mockMvc.perform(get("/accounts/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Account",
                        links(
                                linkWithRel("self").description("Account Profile"),
                                linkWithRel("Account Profile").description("Account Profile"),
                                linkWithRel("get Articles").description("Account's get Articles"),
                                linkWithRel("get Comments").description("Account's get Comments"),
                                linkWithRel("get Notification").description("Account's get Notification"),
                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("이 API에서는 HAL을 지원한다.")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API는 hal+json을 지원한다.")
                        )
                    ));
    }

    @Test
    @DisplayName("Account 조회 실패_noneAuthenticated")
    public void sendAccount_noneAuthenticated() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        this.mockMvc.perform(get("/accounts/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Account 조회 실패_notFound_AccountId")
    @WithAccount("Test@naver.com")
    public void sendAccount_notFound() throws Exception {
        AccountForm accountForm = createAccountForm();
        saveAccount(accountForm);

        this.mockMvc.perform(get("/accounts/532151235")
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account 갱신_success - 303 Redirect")
    public void updateAccount_303() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        accountForm.setUsername("테스트2");

        this.mockMvc.perform(put("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("update-Account",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API는 json 타입의 요청을 받는다"),
                                headerWithName(HttpHeaders.ACCEPT).description("이 API의 응답은 hal+json타입을 지원한다.")
                        ),
                        requestFields(
                                fieldWithPath("username").description("수정을 원하는 값을 입력한다."),
                                fieldWithPath("email").description("수정을 원하는 값을 입력한다."),
                                fieldWithPath("password").description("수정을 원하는 값을 입력한다."),
                                fieldWithPath("checkingPassword").description("입력한 Password를 다시한번 입력한다.")
                        )
                    ));

        Account newAccount = this.accountRepository.findByEmail(account.getEmail()).orElseThrow();

        assertEquals(newAccount.getEmail(), accountForm.getEmail());
        assertNotEquals(newAccount.getUsername(), accountForm.getUsername());
    }

    @Test
    @DisplayName("Account 갱신 실패_400 error(Validator_duplicate_username)")
    public void updateAccount_error_validator_duplicate_username() throws Exception {
        AccountForm accountForm_1 = createAccountForm();
        accountForm_1.setUsername("Test Method User Create");
        saveAccount(accountForm_1);
        AccountForm accountForm_2 = createAccountForm();
        accountForm_2.setUsername("Test Method User Create2");
        accountForm_2.setEmail("test2@gmail.com");
        Account account_2 = saveAccount(accountForm_2);
        accountForm_2.setUsername("Test Method User Create");

        this.mockMvc.perform(put("/accounts/" + account_2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm_2))
                .with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("Account 갱신 실패_400 error(Validator_password)")
    public void updateAccount_error_validator_password() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        accountForm.setCheckingPassword("123456999");

        this.mockMvc.perform(put("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("Account 갱신 실패_404 error(notFound)")
    public void updateAccount_error_notFoundId() throws Exception {
        AccountForm accountForm = createAccountForm();
        saveAccount(accountForm);
        accountForm.setUsername("테스트2");

        this.mockMvc.perform(put("/accounts/231829")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account 삭제 성공")
    public void deleteAccount() throws Exception{
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        this.mockMvc.perform(delete("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-Account",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON을 지원한다.")
                        ),
                        requestFields(
                                fieldWithPath("username").description("닉네임"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("생성할 계정의 비밀번호 8-12자, 문자 규칙은 없다."),
                                fieldWithPath("checkingPassword").description("생성할 계정의 비밀번호를 확인 할 비밀번호.")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("index page URL")
                        )
                ));
    }

    @Test
    @DisplayName("Account 삭제 실패_400 error(validator password)")
    public void deleteAccount_error_validator_password() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        AccountPasswordForm accountPasswordForm = new AccountPasswordForm();
        accountPasswordForm.setPassword("12341234");
        accountPasswordForm.setCheckingPassword("12345678900");

        this.mockMvc.perform(delete("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString((accountPasswordForm)))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("Account 삭제 실패_404 error(notFound)")
    public void deleteAccount_error_notFoundId() throws Exception {
        AccountForm accountForm = createAccountForm();
        saveAccount(accountForm);

        this.mockMvc.perform(delete("/accounts/123155123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString((accountForm)))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}