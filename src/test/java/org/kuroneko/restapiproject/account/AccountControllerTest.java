package org.kuroneko.restapiproject.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.article.ArticleForm;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.comments.CommentsForm;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.config.WithAccount;
import org.kuroneko.restapiproject.domain.*;
import org.kuroneko.restapiproject.notification.NotificationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CommentsRepository commentsRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private AccountForm createAccountForm(){
        AccountForm accountForm = new AccountForm();
        accountForm.setEmail("Test@email.com");
        accountForm.setPassword("12341234");
        accountForm.setCheckingPassword("12341234");
        accountForm.setUsername("테스트1");
        return accountForm;
    }

    private Account saveAccount(AccountForm accountForm) {
        Account account = modelMapper.map(accountForm, Account.class);
        account.setAuthority(UserAuthority.USER);
        account.setCreateTime(LocalDateTime.now());
        account.setPassword(this.passwordEncoder.encode(accountForm.getPassword()));

        return accountRepository.save(account);
    }

    private ArticleForm createArticleForm(int division){
        ArticleForm articleForm = new ArticleForm();
        articleForm.setTitle("Test title number 1");
        articleForm.setDescription("This is Test Article description");
        articleForm.setSource("source @nullable");
        articleForm.setDivision(division);
        return articleForm;
    }

    private Article saveArticle(Account account, ArticleForm articleForm) {
        Article article = modelMapper.map(articleForm, Article.class);
        article.setCreateTime(LocalDateTime.now());

        switch (articleForm.getDivision()) {
            case 1:
                article.setDivision(ArticleThema.HUMOR);
            case 2:
                article.setDivision(ArticleThema.CHAT);
            case 3:
                article.setDivision(ArticleThema.QUESTION);
            default:
                article.setDivision(ArticleThema.CHAT);
        }

        Article newArticle = articleRepository.save(article);
        newArticle.setNumber(newArticle.getId() + 1);
        account.setArticle(article);
        accountRepository.save(account);
        return newArticle;
    }

    private Article saveArticle(Account account, ArticleForm articleForm, int i) {
        Article article = modelMapper.map(articleForm, Article.class);
        article.setCreateTime(LocalDateTime.now().plusHours(i));

        switch (articleForm.getDivision()) {
            case 1:
                article.setDivision(ArticleThema.HUMOR);
            case 2:
                article.setDivision(ArticleThema.CHAT);
            case 3:
                article.setDivision(ArticleThema.QUESTION);
            default:
                article.setDivision(ArticleThema.CHAT);
        }

        Article newArticle = articleRepository.save(article);
        newArticle.setNumber(newArticle.getId() + 1);
        account.setArticle(article);
        accountRepository.save(account);
        return newArticle;
    }

    private CommentsForm createCommentsForm(String message){
        CommentsForm commentsForm = new CommentsForm();
        commentsForm.setDescription(message);
        return commentsForm;
    }

    private Comments saveComments(CommentsForm commentsForm, Article article, Account account){
        Comments comments = new Comments();
        comments.setDescription(commentsForm.getDescription());
        comments.setCreateTime(LocalDateTime.now());
        this.commentsRepository.save(comments);
        comments.setNumber(comments.getId() + 1);
        comments.setArticle(article);
        account.getComments().add(comments);
        return comments;
    }

    private void saveNotification(Article article, Account account){
        Notification notification = new Notification();
        notification.setChecked(false);
        notification.setArticle(article);
        notification.setAccount(account);
        notification.setCreateTime(LocalDateTime.now());
        notificationRepository.save(notification);
        account.getNotification().add(notification);
    }

    @AfterEach
    private void deleteAccountRepository_After(){
        this.articleRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.commentsRepository.deleteAll();
        this.notificationRepository.deleteAll();
    }

    @Test
    @DisplayName("Account 생성 - 201")
    public void createAccount_201() throws Exception {
        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createAccountForm()))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
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
                    responseHeaders(
                            headerWithName(HttpHeaders.LOCATION).description("생성한 계정을 Profile 화면으로 갈 수 있는 링크"),
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("현 REST API가 지원하고 있는 ContentType")
                    ),
                    relaxedResponseFields(
                            fieldWithPath("id").description("계정의 identification"),
                            fieldWithPath("username").description("계정의 닉네임"),
                            fieldWithPath("email").description("계정의 아이디(로그인에 사용)"),
                            fieldWithPath("createTime").description("계정의 생성 일자"),
                            fieldWithPath("updateTime").description("계정의 갱신 일자"),
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
    @DisplayName("Account 갱신 - 200")
    public void updateAccount_200() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        accountForm.setUsername("테스트2");

        this.mockMvc.perform(put("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(accountForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andDo(document("update-Account",
                        links(
                                linkWithRel("self").description("자기 Profile을 볼 수 있는 화면으로 이동")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON을 지원한다."),
                                headerWithName(HttpHeaders.ACCEPT).description("이 API에서는 HAL을 지원한다.")
                        ),
                        requestFields(
                                fieldWithPath("username").description("닉네임"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("생성할 계정의 비밀번호 8-12자, 문자 규칙은 없다."),
                                fieldWithPath("checkingPassword").description("생성할 계정의 비밀번호를 확인 할 비밀번호.")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON과 HAL을 지원한다.")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("계정의 identification"),
                                fieldWithPath("username").description("계정의 닉네임"),
                                fieldWithPath("email").description("계정의 아이디(로그인에 사용)"),
                                fieldWithPath("createTime").description("계정의 생성 일자"),
                                fieldWithPath("updateTime").description("계정의 갱신 일자"),
                                fieldWithPath("authority").description("계정의 접근 권한"),
                                fieldWithPath("article").description("계정이 작성한 게시글 목록들"),
                                fieldWithPath("comments").description("계정이 작성한 댓글 목록들"),
                                fieldWithPath("notification").description("계정의 알림들"),
                                fieldWithPath("_links.self.href").description("생성한 Account 개인 설정화면으로 이동 할 수 있는 Link")
                        )
                ));
    }

    @Test
    @DisplayName("Account 갱신 실패_400 error(Validator_duplicate_username)")
    public void updateAccount_error_validator_duplicate_username() throws Exception {
        AccountForm accountForm_1 = createAccountForm();
        saveAccount(accountForm_1);
        AccountForm accountForm_2 = createAccountForm();
        accountForm_2.setUsername("테스트2");
        accountForm_2.setEmail("test2@gmail.com");
        Account account_2 = saveAccount(accountForm_2);
        accountForm_2.setUsername("테스트1");

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
                .andExpect(status().isOk())
                .andDo(document("delete-Account",
                        links(
                                linkWithRel("index").description("메인 화면으로 이동")
                        ),
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
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON과 HAL을 지원한다.")
                        ),
                        responseFields(
                                fieldWithPath("_links.index.href").description("계정 삭제 후 메인 화면으로 이동한다.")
                        )
                ));
    }

    @Test
    @DisplayName("Account 삭제 실패_400 error(validator password)")
    public void deleteAccount_error_validator_password() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        accountForm.setCheckingPassword("12345678900");

        this.mockMvc.perform(delete("/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString((accountForm)))
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

    @Test
    @DisplayName("Account의 Comments를 조회 성공")
    @WithAccount("test@naver.com")
    @Transactional
    public void findAccountsComments() throws Exception{
        Account account = this.accountRepository.findByEmail("test@naver.com").orElseThrow();

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<5; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article, account);
        }

        this.mockMvc.perform(get("/accounts/{id}/comments", account.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.article").exists())
                .andDo(document("get-Account-Comments",
                        links(
                                linkWithRel("self").description("해당 Account Profile로 이동")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON-HAL 지원한다.")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("계정의 identification"),
                                fieldWithPath("username").description("계정의 닉네임"),
                                fieldWithPath("email").description("계정의 아이디(로그인에 사용)"),
                                fieldWithPath("createTime").description("계정의 생성 일자"),
                                fieldWithPath("updateTime").description("계정의 갱신 일자"),
                                fieldWithPath("authority").description("계정의 접근 권한"),
                                fieldWithPath("article").description("계정이 작성한 게시글 목록들"),
                                fieldWithPath("comments").description("계정이 작성한 댓글 목록들"),
                                fieldWithPath("notification").description("계정의 알림들"),
                                fieldWithPath("_links.self.href").description("생성한 Account 개인 설정화면으로 이동 할 수 있는 Link")
                        ),
                        relaxedResponseFields(beneathPath("comments"),
                                fieldWithPath("id").description("댓글의 identification"),
                                fieldWithPath("number").description("댓글의 순번"),
                                fieldWithPath("description").description("댓글의 내용"),
                                fieldWithPath("createTime").description("댓글이 생성된 시간"),
                                fieldWithPath("agree").description("댓글의 추천 수"),
                                fieldWithPath("disagree").description("댓글의 비추천 수"),
                                fieldWithPath("report").description("댓글의 신고 수"),
                                fieldWithPath("originNo").description("댓글 위치 값(순서)"),
                                fieldWithPath("groupOrd").description("댓글과 답글의 구분"),
                                fieldWithPath("article").description("속해있는 게시글"),
                                fieldWithPath("article.id").description("댓글이 속해있는 게시글의 identification"),
                                fieldWithPath("article.number").description("댓글이 속해있는 게시글의 순번")
                            )
                    ));
    }

    @Test
    @DisplayName("Account의 comments를 조회 실패(존재하지 않는 account 조회)")
    @Transactional
    @WithAccount("test1@test.com")
    public void findAccountsComments_fail_1() throws Exception {
        this.mockMvc.perform(get("/accounts/12345/comments"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 comments를 조회 실패(nonPrincipal)")
    @Transactional
    public void findAccountsComments_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        this.mockMvc.perform(get("/accounts/{id}/comments", account.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account의 comments를 조회 실패(principal과 조회 하려는 Account의 Id가 다를 경우)")
    @Transactional
    @WithAccount("test1@test.com")
    public void findAccountsComments_fail_unMatch() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        ArticleForm articleForm = createArticleForm(1);
        saveArticle(account, articleForm);

        this.mockMvc.perform(get("/accounts/{id}/comments", account.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 comments를 삭제 성공")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountComments_success() throws Exception {
        Account account = this.accountRepository.findByEmail("test@naver.com").orElseThrow();
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<5; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article, account);
        }

        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber() + ", " + all.get(all.size() - 1).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"))
                .andDo(document("delete-comments",
                        links(
                                linkWithRel("self").description("해당 Account Profile로 이동")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("redirect url"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON-HAL 지원한다.")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("계정의 identification"),
                                fieldWithPath("username").description("계정의 닉네임"),
                                fieldWithPath("email").description("계정의 아이디(로그인에 사용)"),
                                fieldWithPath("createTime").description("계정의 생성 일자"),
                                fieldWithPath("updateTime").description("계정의 갱신 일자"),
                                fieldWithPath("authority").description("계정의 접근 권한"),
                                fieldWithPath("article").description("계정이 작성한 게시글 목록들"),
                                fieldWithPath("comments").description("계정이 작성한 댓글 목록들"),
                                fieldWithPath("notification").description("계정의 알림들"),
                                fieldWithPath("_links.self.href").description("생성한 Account 개인 설정화면으로 이동 할 수 있는 Link")
                        ),
                        relaxedResponseFields(beneathPath("comments"),
                                fieldWithPath("id").description("댓글의 identification"),
                                fieldWithPath("number").description("댓글의 순번"),
                                fieldWithPath("description").description("댓글의 내용"),
                                fieldWithPath("createTime").description("댓글이 생성된 시간"),
                                fieldWithPath("agree").description("댓글의 추천 수"),
                                fieldWithPath("disagree").description("댓글의 비추천 수"),
                                fieldWithPath("report").description("댓글의 신고 수"),
                                fieldWithPath("originNo").description("댓글 위치 값(순서)"),
                                fieldWithPath("groupOrd").description("댓글과 답글의 구분"),
                                fieldWithPath("article").description("속해있는 게시글"),
                                fieldWithPath("article.id").description("댓글이 속해있는 게시글의 identification"),
                                fieldWithPath("article.number").description("댓글이 속해있는 게시글의 순번")
                        )
                ));
    }

    @Test
    @DisplayName("Account의 comments를 삭제 실패(Principal과 Login Account 다름)")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountComments_fail_accountMiss() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account newAccount = saveAccount(accountForm);

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(newAccount, articleForm);

        for(int i=0; i<5; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article, newAccount);
        }

        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber() + ", " + all.get(all.size() - 1).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", newAccount.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 comments를 삭제 실패(이상한 URL 요청)")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountComments_fail_unMatch() throws Exception {
        Account account = accountRepository.findByEmail("test@naver.com").orElseThrow();

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<5; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article, account);
        }

        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber() + ", " + all.get(all.size() - 1).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", 1982739548)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 comments를 삭제 실패(non principal)")
    @Transactional
    public void deleteAccountComments_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<5; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article, account);
        }

        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber() + ", " + all.get(all.size() - 1).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account의 articles를 조회 성공")
    @WithAccount("test@naver.com")
    @Transactional
    public void findAccountsArticles() throws Exception{
        Account account = this.accountRepository.findByEmail("test@naver.com").orElseThrow();

        for(int i = 0; i<50; i++){
            ArticleForm articleForm = createArticleForm(1);
            saveArticle(account, articleForm, i);
        }

        this.mockMvc.perform(get("/accounts/{id}/articles", account.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Account-Article",
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON-HAL 지원한다."),
                                headerWithName(HttpHeaders.LOCATION).description("이 계정의 프로필 URL")
                        ),
                        responseFields(beneathPath("content"),
                                fieldWithPath("number").description("게시글의 순번"),
                                fieldWithPath("title").description("게시글의 제목"),
                                fieldWithPath("description").description("게시글의 내용"),
                                fieldWithPath("source").description("게시글에 첨부파일 등이 있다면 그에 대한 출처 정보"),
                                fieldWithPath("division").description("게시글의 글 유형"),
                                fieldWithPath("createTime").description("게시글이 생성된 시간"),
                                fieldWithPath("updateTime").description("게시글이 수정된 시간"),
                                fieldWithPath("comments").description("게시글의 댓글들"),
                                fieldWithPath("accountId").description("게시글을 가지고 있는 유저의 Id"),
                                fieldWithPath("userName").description("게시글을 가지고 있는 유저의 이름"),
                                fieldWithPath("userEmail").description("게시글을 가지고 있는 유저의 이메일"),
                                fieldWithPath("authority").description("게시글을 가지고 있는 유저의 접근권한")
                        ),
                        relaxedResponseFields(beneathPath("pageable"),
                                fieldWithPath("sort").description("페이징의 정렬"),
                                fieldWithPath("offset").description("페이지 출발 값"),
                                fieldWithPath("pageNumber").description("현재 페이지 번호"),
                                fieldWithPath("pageSize").description("한 페이지에서 표시 가능한 숫자")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("last").description("마지막 페이지인지에 대한 여부"),
                                fieldWithPath("totalPages").description("총 페이지 수"),
                                fieldWithPath("totalElements").description("총 게시글의 갯수"),
                                fieldWithPath("size").description("한 페이지에 보여줄 수 있는 게시글의 수"),
                                fieldWithPath("number").description("현재 페이지 번호"),
                                fieldWithPath("sort").description("정렬 속성"),
                                fieldWithPath("sort.sorted").description("정렬의 여부"),
                                fieldWithPath("first").description("첫 페이지 여부"),
                                fieldWithPath("empty").description("리스트가 비어있는지의 여부")
                        )
                ));
    }

    @Test
    @DisplayName("Account의 articles를 조회 실패(존재하지 않는 account 조회)")
    @Transactional
    @WithAccount("test1@test.com")
    public void findAccountsArticles_fail_1() throws Exception {
        this.mockMvc.perform(get("/accounts/12345/articles"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 articles를 조회 실패(nonPrincipal)")
    @Transactional
    public void findAccountsArticles_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        this.mockMvc.perform(get("/accounts/{id}/articles", account.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account의 articles를 조회 실패(principal과 조회 하려는 Account의 Id가 다를 경우)")
    @Transactional
    @WithAccount("test1@test.com")
    public void findAccountsArticles_fail_unMatch() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        ArticleForm articleForm = createArticleForm(1);
        saveArticle(account, articleForm);

        this.mockMvc.perform(get("/accounts/{id}/articles", account.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 articles를 삭제 성공")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountArticles_success() throws Exception {
        Account account = this.accountRepository.findByEmail("test@naver.com").orElseThrow();
        ArticleForm articleForm1 = createArticleForm(1);
        saveArticle(account, articleForm1);
        ArticleForm articleForm2 = createArticleForm(1);
        saveArticle(account, articleForm2);
        ArticleForm articleForm3 = createArticleForm(1);
        saveArticle(account, articleForm3);

        List<Article> all = articleRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/articles", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"))
                .andDo(document("delete-articles",
                        links(
                                linkWithRel("self").description("해당 Account Profile로 이동")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("redirect url"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON-HAL 지원한다.")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("계정의 identification"),
                                fieldWithPath("username").description("계정의 닉네임"),
                                fieldWithPath("email").description("계정의 아이디(로그인에 사용)"),
                                fieldWithPath("createTime").description("계정의 생성 일자"),
                                fieldWithPath("updateTime").description("계정의 갱신 일자"),
                                fieldWithPath("authority").description("계정의 접근 권한"),
                                fieldWithPath("article").description("계정이 작성한 게시글 목록들"),
                                fieldWithPath("comments").description("계정이 작성한 댓글 목록들"),
                                fieldWithPath("notification").description("계정의 알림들"),
                                fieldWithPath("_links.self.href").description("생성한 Account 개인 설정화면으로 이동 할 수 있는 Link")
                        ),
                        responseFields(beneathPath("article"),
                                fieldWithPath("id").description("게시글의 identification"),
                                fieldWithPath("number").description("게시글의 순번"),
                                fieldWithPath("title").description("게시글의 제목"),
                                fieldWithPath("description").description("게시글의 내용"),
                                fieldWithPath("source").description("게시글에 첨부파일 등이 있다면 그에 대한 출처 정보"),
                                fieldWithPath("division").description("게시글의 글 유형"),
                                fieldWithPath("createTime").description("게시글이 생성된 시간"),
                                fieldWithPath("updateTime").description("게시글이 수정된 시간"),
                                fieldWithPath("comments").description("게시글의 댓글들"),
                                fieldWithPath("report").description("게시글의 신고 횟수")
                        )
                ));
    }

    @Test
    @DisplayName("Account의 articles를 삭제 실패(Principal과 Login Account 다름)")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountArticles_fail_accountMiss() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account newAccount = saveAccount(accountForm);

        ArticleForm articleForm1 = createArticleForm(1);
        saveArticle(newAccount, articleForm1);
        ArticleForm articleForm2 = createArticleForm(1);
        saveArticle(newAccount, articleForm2);
        ArticleForm articleForm3 = createArticleForm(1);
        saveArticle(newAccount, articleForm3);

        List<Article> all = articleRepository.findAll();
        String str = all.get(0).getId() + ", " + all.get(2).getId();

        this.mockMvc.perform(delete("/accounts/{id}/articles", newAccount.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 articles를 삭제 실패(이상한 URL 요청)")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountArticles_fail_unMatch() throws Exception {
        Account account = accountRepository.findByEmail("test@naver.com").orElseThrow();
        ArticleForm articleForm1 = createArticleForm(1);
        saveArticle(account, articleForm1);
        ArticleForm articleForm2 = createArticleForm(1);
        saveArticle(account, articleForm2);
        ArticleForm articleForm3 = createArticleForm(1);
        saveArticle(account, articleForm3);

        List<Article> all = articleRepository.findAll();
        String str = all.get(0).getId() + ", " + all.get(2).getId();

        this.mockMvc.perform(delete("/accounts/{id}/articles", 1982739548)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 articles를 삭제 실패(non principal)")
    @Transactional
    public void deleteAccountArticles_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        ArticleForm articleForm1 = createArticleForm(1);
        saveArticle(account, articleForm1);
        ArticleForm articleForm2 = createArticleForm(1);
        saveArticle(account, articleForm2);
        ArticleForm articleForm3 = createArticleForm(1);
        saveArticle(account, articleForm3);

        List<Article> all = articleRepository.findAll();
        String str = all.get(0).getId() + ", " + all.get(2).getId();

        this.mockMvc.perform(delete("/accounts/{id}/articles", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account의 notfication을 조회 성공")
    @WithAccount("test@naver.com")
    @Transactional
    public void getAccountNotification_success() throws Exception {
        Account account = this.accountRepository.findByEmail("test@naver.com").orElseThrow();
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        List<Notification> all = this.notificationRepository.findAll();
        assertEquals(all.size(), 10);

        this.mockMvc.perform(get("/accounts/{id}/notification", account.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Location"))
                .andDo(document("get-Account-Article"));
    }

    @Test
    @DisplayName("Account의 notfication을 삭제 성공")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountNotification_success() throws Exception {
        Account account = this.accountRepository.findByEmail("test@naver.com").orElseThrow();
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        List<Notification> all = this.notificationRepository.findAll();
        assertEquals(all.size(), 10);

        this.mockMvc.perform(delete("/accounts/{id}/notification", account.getId())
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"))
                .andDo(document("delete-notification"));

        all = this.notificationRepository.findAll();
        assertEquals(all.size(), 0);
    }

}