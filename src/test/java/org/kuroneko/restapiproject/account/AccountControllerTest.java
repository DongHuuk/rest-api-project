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
import org.kuroneko.restapiproject.account.domain.LoginForm;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.config.WithAccount;
import org.kuroneko.restapiproject.exception.IdNotFoundException;
import org.kuroneko.restapiproject.notification.NotificationRepository;
import org.kuroneko.restapiproject.token.AccountVORepository;
import org.kuroneko.restapiproject.token.AuthConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
    @Autowired private AccountVORepository accountVORepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @AfterEach
    private void deleteAccountRepository_After(){
        this.notificationRepository.deleteAll();
        this.commentsRepository.deleteAll();
        this.articleRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.accountVORepository.deleteAll();
    }

    @Test
    @DisplayName("Account 조회 (email) 성공 - 201")
    public void findAccountByEmail() throws Exception {
        AccountForm accountForm = createAccountForm();
        saveAccount(accountForm);

        this.mockMvc.perform(get("/accounts")
                .content(accountForm.getEmail()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Account 생성 - 201")
    public void createAccount_201() throws Exception {
        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("TestUser");
        String str = "{\"username\":\"TestUser\",\"email\":\"test@testT.com\",\"password\":\"1234567890\",\"checkingPassword\":\"1234567890\"}";

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
//                .accept(MediaTypes.HAL_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("create-Account",
                    links(
                            linkWithRel("Account Profile").description("계정의 프로필"),
                            linkWithRel("get Articles").description("계정이 작성한 게시글"),
                            linkWithRel("get Comments").description("계정이 작성한 댓글"),
                            linkWithRel("get Notification").description("계정의 알림"),
                            linkWithRel("DOCS").description("REST API DOCS")
                    ),
                    requestFields(
                            fieldWithPath("username").description("생성할 계정의 닉네임 특수문자는 허용하지 않는다."),
                            fieldWithPath("email").description("생성할 계정의 아이디(로그인시 사용)"),
                            fieldWithPath("password").description("생성할 계정의 비밀번호 8-12자, 문자 규칙은 없다."),
                            fieldWithPath("checkingPassword").description("생성할 계정의 비밀번호를 확인 할 비밀번호.")
                    )
                ));
        Account account = this.accountRepository.findByEmail(accountForm.getEmail()).orElseThrow();
        assertNotEquals(account, null);
        assertEquals(account.getUsername(), accountForm.getUsername());
        assertEquals(account.getEmail(), accountForm.getEmail());
        assertTrue(this.passwordEncoder.matches(accountForm.getPassword(), account.getPassword()));
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
                .param("email", EMAIL))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());

        assertThrows(IdNotFoundException.class,
                () -> this.accountRepository.findByEmail(EMAIL)
                        .orElseThrow(() -> new IdNotFoundException(EMAIL))
        );
    }

    @Test
    @DisplayName("Account 생성 실패_400 error(Validator_password)")
    public void createAccount_error_validator() throws Exception{
        AccountForm accountForm = createAccountForm();
        accountForm.setPassword("123456");

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());

        assertThrows(IdNotFoundException.class,
                () -> this.accountRepository.findByEmail(EMAIL)
                        .orElseThrow(() -> new IdNotFoundException(EMAIL))
        );
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 생성 실패 (Validator duplicate username) - 400")
    public void createAccount_error_emailDuplicate() throws Exception{
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setUsername(account.getUsername());

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andDo(document("create-Account-Errors"));

        List<Account> all = this.accountRepository.findAll();
        assertEquals(all.size(), 1);
        assertEquals(all.get(0).getEmail(), EMAIL);
    }

    @Test
    @DisplayName("Account 생성 실패 (Validator duplicate email) - 400")
    public void createAccount_error_usernameDuplicate() throws Exception{
        AccountForm accountForm = createAccountForm();
        saveAccount(accountForm);
        accountForm.setEmail(SEC_EMAIL);

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());

        List<Account> all = this.accountRepository.findAll();
        assertEquals(all.size(), 1);
        assertEquals(all.get(0).getEmail(), EMAIL);
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 조회 성공 - 200")
    public void sendAccount() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        String token = createToken(account);
        this.mockMvc.perform(get("/accounts/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Account",
                        links(
                                linkWithRel("self").description("계정의 프로필"),
                                linkWithRel("get Articles").description("계정이 작성한 게시글"),
                                linkWithRel("get Comments").description("계정이 작성한 댓글"),
                                linkWithRel("get Notification").description("계정의 알림"),
                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("계정을 구분할 수 있는 ID 값"),
                                fieldWithPath("username").description("사이트의 닉네임으로 사용될 값"),
                                fieldWithPath("email").description("로그인 시 사용되는 값"),
                                fieldWithPath("createTime").description("생성된 일자"),
                                fieldWithPath("updateTime").description("계정을 수정한 일자"),
                                fieldWithPath("authority").description("계정의 권한"),
                                fieldWithPath("article").description("계정이 작성한 게시글"),
                                fieldWithPath("comments").description("계정이 작성한 댓글"),
                                fieldWithPath("communities").description("계정이 생성한 커뮤니티 게시판"),
                                fieldWithPath("notification").description("계정의 알림"),
                                fieldWithPath("isEnable").description("계정의 JWT의 유효 여부")
                        )
                ));
    }

    @Test
    @DisplayName("Account 조회 실패 (JWT error) - 3xx")
    @WithAccount(EMAIL)
    public void sendAccount_token() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        this.mockMvc.perform(get("/accounts/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Account 조회 실패 (principal) - 403")
    public void sendAccount_noneAuthenticated() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/{id}", account.getId())
                .accept(MediaTypes.HAL_JSON)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Account 조회 실패 (Account Id) - 404")
    @WithAccount(EMAIL)
    public void sendAccount_notFound() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/532151235")
                .accept(MediaTypes.HAL_JSON)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 갱신 성공 - 201")
    public void updateAccount() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("테스트2");
        accountForm.setPassword("0987654321");
        accountForm.setCheckingPassword("0987654321");
        String token = createToken(account);

        this.mockMvc.perform(put("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .header(AuthConstants.AUTH_HEADER, token)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("update-Account",
                        links(
                                linkWithRel("Account Profile").description("계정의 프로필"),
                                linkWithRel("get Articles").description("계정이 작성한 게시글"),
                                linkWithRel("get Comments").description("계정이 작성한 댓글"),
                                linkWithRel("get Notification").description("계정의 알림"),
                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
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
        assertEquals(newAccount.getUsername(), accountForm.getUsername());
        assertFalse(this.passwordEncoder.matches("1234567890", newAccount.getPassword()));
        assertTrue(this.passwordEncoder.matches("0987654321", newAccount.getPassword()));
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 갱신 실패 (JWT error) - 304")
    public void updateAccount_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("테스트2");

        this.mockMvc.perform(put("/accounts/231829")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Account 갱신 실패 (Principal) - 403")
    public void updateAccount_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        accountForm.setEmail(SEC_EMAIL);
        accountForm.setUsername("test2 username");

        String token = createToken(account);

        this.mockMvc.perform(put("/accounts/231829")
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 갱신 실패 (Validator_duplicate_username) - 400")
    @Transactional
    public void updateAccount_error_validator_duplicate_username() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        account.setUsername("Test Method User Create");

        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("Test Method User Create2");
        accountForm.setEmail(SEC_EMAIL);
        Account account2 = saveAccount(accountForm);
        accountForm.setUsername("Test Method User Create");

        String token = createToken(account);

        this.mockMvc.perform(put("/accounts/" + account2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .header(AuthConstants.AUTH_HEADER, token)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(status().isBadRequest());

        assertNotEquals(account.getUsername(), account2.getUsername());
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 갱신 실패 (Validator_password) - 400")
    public void updateAccount_error_validator_password() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("update method error");
        accountForm.setCheckingPassword("0987654321");

        String token = createToken(account);

        this.mockMvc.perform(put("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .header(AuthConstants.AUTH_HEADER, token)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(status().isBadRequest());

        assertNotEquals(account.getUsername(), accountForm.getUsername());
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 갱신 실패 (Account Id) - 404")
    public void updateAccount_error_notFoundId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("테스트2");

        String token = createToken(account);

        this.mockMvc.perform(put("/accounts/231829")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .header(AuthConstants.AUTH_HEADER, token)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 삭제 성공 - 204")
    public void deleteAccount() throws Exception{
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        String token = createToken(account);

        AccountPasswordForm accountPasswordForm = new AccountPasswordForm();
        accountPasswordForm.setPassword("1234567890");
        accountPasswordForm.setCheckingPassword("1234567890");

        this.mockMvc.perform(delete("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstants.AUTH_HEADER, token)
                .content(objectMapper.writeValueAsString(accountPasswordForm)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-Account",
                        requestHeaders(
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        requestFields(
                                fieldWithPath("password").description("생성할 계정의 비밀번호 8-12자, 문자 규칙은 없다."),
                                fieldWithPath("checkingPassword").description("생성할 계정의 비밀번호를 확인 할 비밀번호.")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Redirect URL")
                        )
                ));

        assertThrows(IdNotFoundException.class,
                () -> this.accountRepository.findByEmail(account.getEmail())
                        .orElseThrow(() -> new IdNotFoundException(account.getEmail()))
        );
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 삭제 실패 (JWT error) - 3xx")
    public void deleteAccount_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountPasswordForm accountPasswordForm = new AccountPasswordForm();
        accountPasswordForm.setPassword("12341234");
        accountPasswordForm.setCheckingPassword("12345678900");

        this.mockMvc.perform(delete("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString((accountPasswordForm))))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        assertDoesNotThrow(
                () -> this.accountRepository.findByEmail(account.getEmail())
                        .orElseThrow(() -> new IdNotFoundException(account.getEmail()))
        );
    }

    @Test
    @DisplayName("Account 삭제 실패 (Principal) - 403")
    public void deleteAccount_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        AccountPasswordForm accountPasswordForm = new AccountPasswordForm();
        accountPasswordForm.setPassword("12341234");
        accountPasswordForm.setCheckingPassword("12345678900");

        String token = createToken(account);

        this.mockMvc.perform(delete("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstants.AUTH_HEADER, token)
                .content(this.objectMapper.writeValueAsString((accountPasswordForm))))
                .andDo(print())
                .andExpect(status().isForbidden());

        assertDoesNotThrow(
                () -> this.accountRepository.findByEmail(account.getEmail())
                        .orElseThrow(() -> new IdNotFoundException(account.getEmail()))
        );
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 삭제 실패 (validator accountPasswordForm) - 400")
    public void deleteAccount_error_validator_password() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountPasswordForm accountPasswordForm = new AccountPasswordForm();
        accountPasswordForm.setPassword("12341234");
        accountPasswordForm.setCheckingPassword("12345678900");

        String token = createToken(account);

        this.mockMvc.perform(delete("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstants.AUTH_HEADER, token)
                .content(this.objectMapper.writeValueAsString((accountPasswordForm))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());

        assertDoesNotThrow(
                () -> this.accountRepository.findByEmail(account.getEmail())
                .orElseThrow(() -> new IdNotFoundException(account.getEmail()))
        );
    }

    @Test
    @WithAccount(EMAIL)
    @DisplayName("Account 삭제 실패 (Not Found Account Id) - 404")
    public void deleteAccount_error_notFoundId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        String token = createToken(account);

        AccountPasswordForm accountPasswordForm = new AccountPasswordForm();
        accountPasswordForm.setPassword("1234567890");
        accountPasswordForm.setCheckingPassword("1234567890");

        this.mockMvc.perform(delete("/accounts/123155123")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstants.AUTH_HEADER, token)
                .content(this.objectMapper.writeValueAsString((accountPasswordForm))))
                .andDo(print())
                .andExpect(status().isNotFound());

        assertDoesNotThrow(
                () -> this.accountRepository.findByEmail(account.getEmail())
                        .orElseThrow(() -> new IdNotFoundException(account.getEmail()))
        );
    }

    @Test
    @DisplayName("login token(JWT) 생성 여부 확인")
    public void tokenTest() throws Exception {
        AccountForm accountForm = createAccountForm();
        saveAccount(accountForm);
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail(accountForm.getEmail());
        loginForm.setPassword(accountForm.getPassword());

        this.mockMvc.perform(post("/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content(this.objectMapper.writeValueAsString(loginForm)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
    }
}