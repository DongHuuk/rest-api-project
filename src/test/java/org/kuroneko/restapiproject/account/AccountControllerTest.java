package org.kuroneko.restapiproject.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.domain.UserAuthority;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class )
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ModelMapper modelMapper;

    private AccountForm createAccountForm(){
        AccountForm accountForm = new AccountForm();
        accountForm.setEmail("Test@email.com");
        accountForm.setPassword("12341234");
        accountForm.setCheckingPassword("12341234");
        accountForm.setUsername("흑우냥이");
        return accountForm;
    }

    private Account saveAccount(AccountForm accountForm) {
        Account account = modelMapper.map(accountForm, Account.class);
        account.setAuthority(UserAuthority.USER);
        account.setCreateTime(LocalDateTime.now());

        return accountRepository.save(account);
    }

    @BeforeEach
    private void deleteAccountRepository(){
        this.accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Account 생성 - 201")
    public void createAccount_201() throws Exception {
        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createAccountForm())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("create-Account",
                    links(
                            linkWithRel("self").description("생성한 Account 개인 설정화면으로 이동 할 수 있는 Link")
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
                    ),
                    relaxedResponseFields(
                            fieldWithPath("id").description("계정의 identification"),
                            fieldWithPath("username").description("계정의 닉네임"),
                            fieldWithPath("email").description("계정의 아이디(로그인에 사용)"),
                            fieldWithPath("createTime").description("계정의 생성 일자"),
                            fieldWithPath("authority").description("계정의 접근 권한"),
                            fieldWithPath("article").description("계정이 작성한 게시글 목록들"),
                            fieldWithPath("comments").description("계정이 작성한 댓글 목록들"),
                            fieldWithPath("notification").description("계정의 알림들"),
                            fieldWithPath("_links.self.href").description("생성한 Account 개인 설정화면으로 이동 할 수 있는 Link")
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
                .param("email", "test@gmail.com"))
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
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andDo(document("create-Account-Errors"));
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
                .content(objectMapper.writeValueAsString(accountForm)))
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
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andDo(document("create-Account-Errors"));
    }

    @Test
    @DisplayName("Account 갱신 - 200")
    public void updateAccount_200() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        accountForm.setUsername("테스트2");

        this.mockMvc.perform(put("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("update-Account"));
    }

}