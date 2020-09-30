package org.kuroneko.restapiproject.account;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    @AfterEach
    private void deleteAccountRepository_After(){
        this.notificationRepository.deleteAll();
        this.commentsRepository.deleteAll();
        this.articleRepository.deleteAll();
        this.accountRepository.deleteAll();
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

        this.mockMvc.perform(get("/accounts/{id}/notification", account.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Location"))
                .andDo(document("get-Account-Article",
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON-HAL 지원한다."),
                                headerWithName(HttpHeaders.LOCATION).description("이 계정의 프로필 URL")
                        ),
                        responseFields(beneathPath("content"),
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
                                fieldWithPath("sort.sorted").description("정렬의 여부"),
                                fieldWithPath("first").description("첫 페이지 여부"),
                                fieldWithPath("empty").description("리스트가 비어있는지의 여부")
                        )

                ));
    }

    @Test
    @DisplayName("Account의 notification를 조회 실패(존재하지 않는 account 조회)")
    @Transactional
    @WithAccount("test1@test.com")
    public void findAccountsNotification_fail_1() throws Exception {
        this.mockMvc.perform(get("/accounts/12345/notification"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 notification 조회 실패(nonPrincipal)")
    @Transactional
    public void findAccountsNotification_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        this.mockMvc.perform(get("/accounts/{id}/notification", account.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account의 notification 조회 실패(principal과 조회 하려는 Account의 Id가 다를 경우)")
    @Transactional
    @WithAccount("test1@test.com")
    public void findAccountsNotification_fail_unMatch() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        this.mockMvc.perform(get("/accounts/{id}/notification", account.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
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

        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/notification", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andDo(document("delete-notification",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Json 타입의 숫자 + ','의 값을 보낸다. ex) 1, 3, 5")
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
                                fieldWithPath("_links.self.href").description("Account 개인 설정화면으로 이동 할 수 있는 Link"),
                                fieldWithPath("_links.getNotification.href").description("Account의 알림들을 보여주는 get Link")
                        )
                ));
    }

    @Test
    @DisplayName("Account의 notification를 삭제 실패(Principal과 Login Account 다름)")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountNotification_fail_accountMiss() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account newAccount = saveAccount(accountForm);

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(newAccount, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, newAccount);
        }

        List<Notification> all = this.notificationRepository.findAll();

        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", newAccount.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 notification를 삭제 실패(이상한 URL 요청)")
    @WithAccount("test@naver.com")
    @Transactional
    public void deleteAccountNotification_fail_unMatch() throws Exception {
        Account account = this.accountRepository.findByEmail("test@naver.com").orElseThrow();
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, account);
        }

        List<Notification> all = this.notificationRepository.findAll();

        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", 1982739548)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 notification를 삭제 실패(non principal)")
    @Transactional
    public void deleteAccountNotification_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account newAccount = saveAccount(accountForm);

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(newAccount, articleForm);

        for (int i = 0; i < 10; i++) {
            this.saveNotification(article, newAccount);
        }

        List<Notification> all = this.notificationRepository.findAll();

        String str = all.get(0).getNumber() + ", " + all.get(3).getNumber() + ", " + all.get(8).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", newAccount.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
