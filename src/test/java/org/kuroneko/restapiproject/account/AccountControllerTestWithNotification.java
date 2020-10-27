package org.kuroneko.restapiproject.account;

import lombok.With;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.config.WithAccount;
import org.kuroneko.restapiproject.exception.IdNotFoundException;
import org.kuroneko.restapiproject.notification.NotificationRepository;
import org.kuroneko.restapiproject.notification.domain.Notification;
import org.kuroneko.restapiproject.token.AccountVORepository;
import org.kuroneko.restapiproject.token.AuthConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
public class AccountControllerTestWithNotification extends AccountMethods{

    @Autowired
    private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private CommentsRepository commentsRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private AccountVORepository accountVORepository;

    @AfterEach
    private void deleteAccountRepository_After(){
        this.notificationRepository.deleteAll();
        this.commentsRepository.deleteAll();
        this.articleRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.accountVORepository.deleteAll();
    }

    @Test
    @DisplayName("Account의 notfication을 조회 성공 - 200")
    @WithAccount(EMAIL)
    @Transactional
    public void getAccountNotification_success() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/{id}/notification", account.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Account-Notification",
                        links(
                            linkWithRel("self").description("Account Profile"),
                            linkWithRel("get Articles").description("Account's get Articles"),
                            linkWithRel("get Comments").description("Account's get Comments"),
                            linkWithRel("get Notification").description("Account's get Notification"),
                            linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON-HAL 지원한다.")
                        ),
                        responseFields(beneathPath("_embedded.notificationDTOList"),
                                fieldWithPath("id").description("알림의 ID"),
                                fieldWithPath("number").description("일림의 순번"),
                                fieldWithPath("createTime").description("알림이 생성된 시간"),
                                fieldWithPath("checked").description("알림의 확인 여부"),
                                fieldWithPath("accountId").description("알림을 가지고 있는 Account Id"),
                                fieldWithPath("accountUsername").description("알림을 가지고 있는 Account 유저명"),
                                fieldWithPath("userEmail").description("알림을 가지고 있는 Account 이메일"),
                                fieldWithPath("articleId").description("알림이 가리키고 있는 Article ID"),
                                fieldWithPath("articleNumber").description("알림이 가리키고 있는 Article 순번"),
                                fieldWithPath("commentsId").description("알림이 가리키고 있는 Comments 순번"),
                                fieldWithPath("commentsNumber").description("알림이 가리키고 있는 Comments 순번")
                        ),
                        responseFields(beneathPath("page"),
                                fieldWithPath("size").description("한 페이지의 최대 갯수"),
                                fieldWithPath("totalElements").description("총 게시글 수"),
                                fieldWithPath("totalPages").description("총 page 수"),
                                fieldWithPath("number").description("현재 페이지")
                        )

                ));
    }

    @Test
    @DisplayName("Account의 notification 조회 실패 (JWT error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void findAccountsNotification_fail_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        this.mockMvc.perform(get("/accounts/{id}/notification", account.getId()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Account의 notification 조회 실패 (Principal)- 403")
    @Transactional
    public void findAccountsNotification_fail_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/{id}/notification", account.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Account의 notification를 조회 실패(not Found Account Id) - 404")
    @Transactional
    @WithAccount(EMAIL)
    public void findAccountsNotification_fail_AccountId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/{id}/notification", 132132189)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account의 notification 조회 실패 (unMatch Account and Principal) - 400")
    @Transactional
    @WithAccount(EMAIL)
    public void findAccountsNotification_fail_unMatch() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setEmail(SEC_EMAIL);
        accountForm.setUsername("test2 Username");
        Account saveAccount = saveAccount(accountForm);
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(saveAccount, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, saveAccount);
        }

        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/{id}/notification", saveAccount.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 notfication을 삭제 성공 - 204")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountNotification_success() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        List<Notification> all = this.notificationRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();
        String token = createToken(account);

        this.mockMvc.perform(delete("/accounts/{id}/notification", account.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-Account-Notification",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Json 타입의 숫자 + ','의 값을 보낸다. ex) 1, 3, 5"),
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Redirect URL")
                        )
                ));

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s -> {
            assertThrows(
                    IdNotFoundException.class,
                    () -> this.notificationRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("number " + s + " is not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 notification를 삭제 실패(Principal) - 403")
    @Transactional
    public void deleteAccountNotification_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        List<Notification> all = this.notificationRepository.findAll();
        String token = createToken(account);
        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isForbidden());

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s -> {
            assertDoesNotThrow(
                    () -> this.notificationRepository.findByNumber(Long.parseLong(s))
                    .orElseThrow(() -> new IdNotFoundException("number " + s + " is not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 notification를 삭제 실패(not found AccountId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountNotification_fail_unMatch() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        List<Notification> all = this.notificationRepository.findAll();
        String token = createToken(account);
        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", 1982739548)
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isNotFound());

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s -> {
            assertDoesNotThrow(
                    () -> this.notificationRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("number " + s + " is not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 notification를 삭제 실패(unMatch Principal and Account) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountNotification_fail_accountMiss() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setEmail(SEC_EMAIL);
        accountForm.setUsername("test2 Username");
        Account newAccount = saveAccount(accountForm);

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(newAccount, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, newAccount);
        }

        List<Notification> all = this.notificationRepository.findAll();
        String token = createToken(account);
        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", newAccount.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isBadRequest());

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s -> {
            assertDoesNotThrow(
                    () -> this.notificationRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("number " + s + " is not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 notification를 삭제 실패 (JWT error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountNotification_fail_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        List<Notification> all = this.notificationRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s -> {
            assertDoesNotThrow(
                    () -> this.notificationRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("number " + s + " is not found"))
            );
        });
    }
}
