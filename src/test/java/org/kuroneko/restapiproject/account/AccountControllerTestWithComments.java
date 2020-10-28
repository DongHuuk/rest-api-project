package org.kuroneko.restapiproject.account;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.comments.domain.CommentForm;
import org.kuroneko.restapiproject.comments.domain.Comments;
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
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
public class AccountControllerTestWithComments extends AccountMethods{

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
    @DisplayName("Account의 Comments를 조회 성공 - 200")
    @WithAccount(EMAIL)
    @Transactional
    public void findAccountsComments() throws Exception{
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        ArticleForm articleForm_1 = createArticleForm(1);
        Article article_1 = saveArticle(account, articleForm_1);
        ArticleForm articleForm_2 = createArticleForm(1);
        Article article_2 = saveArticle(account, articleForm_2);
        ArticleForm articleForm_3 = createArticleForm(1);
        Article article_3 = saveArticle(account, articleForm_3);

        for(int i=0; i<22; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article_1, account, i);
        }
        for(int i=0; i<15; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article_2, account, i);
        }
        for(int i=0; i<15; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article_3, account, i);
        }

        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/{id}/comments", account.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Account-Comments",
                        links(
                                linkWithRel("first").description("첫 페이지"),
                                linkWithRel("next").description("다음 페이지"),
                                linkWithRel("last").description("마지막 페이지"),
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
                        responseFields(beneathPath("_embedded.commentsDTOList"),
                                fieldWithPath("number").description("댓글의 순번"),
                                fieldWithPath("description").description("댓글의 내용"),
                                fieldWithPath("createTime").description("댓글이 생성된 시간"),
                                fieldWithPath("agree").description("댓글의 추천 수"),
                                fieldWithPath("disagree").description("댓글의 비추천 수"),
                                fieldWithPath("originNo").description("댓글 위치 값(순서)"),
                                fieldWithPath("groupOrd").description("댓글과 답글의 구분"),
                                fieldWithPath("articleId").description("댓글이 속해있는 게시글의 identification"),
                                fieldWithPath("articleNumber").description("댓글이 속해있는 게시글의 순번")
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
    @DisplayName("Account의 comments를 조회 실패 (JWT error) - 304")
    @WithAccount(EMAIL)
    @Transactional
    public void findAccountsComments_fail_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        this.mockMvc.perform(get("/accounts/{id}/comments", account.getId()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Account의 comments를 조회 실패 (Principal) - 403")
    @Transactional
    public void findAccountsComments_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/{id}/comments", account.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Account의 comments를 조회 실패 (Not Found Account Id) - 404")
    @Transactional
    @WithAccount(EMAIL)
    public void findAccountsComments_fail_AccountId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/12345/comments")
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Account의 comments를 조회 실패(principal과 조회 하려는 Account의 Id가 다를 경우) - 400")
    @Transactional
    @WithAccount(EMAIL)
    public void findAccountsComments_fail_unMatch() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        AccountForm accountForm = createAccountForm();
        accountForm.setEmail(SEC_EMAIL);
        accountForm.setUsername("test Username");
        Account saveAccount = saveAccount(accountForm);
        ArticleForm articleForm = createArticleForm(1);
        saveArticle(saveAccount, articleForm);

        String token = createToken(account);

        this.mockMvc.perform(get("/accounts/{id}/comments", saveAccount.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account의 comments를 삭제 성공")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountComments_success() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<17; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article, account, i);
        }

        String token = createToken(account);
        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber()+ ", " + all.get(4).getNumber()+ ", " + all.get(6).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-Account-Comments",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("AJAX로 Json 타입의 숫자 + ','의 값을 보낸다. ex) 1, 3, 5"),
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Redirect URL")
                        )
                ));

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s ->{
            assertThrows(
                    IdNotFoundException.class,
                    () -> this.commentsRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("Number " + s + " not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 comments를 삭제 실패(JWT error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountComments_fail_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<5; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article, account, i);
        }

        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber() + ", " + all.get(all.size() - 1).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s ->{
            assertDoesNotThrow(
                    () -> this.commentsRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("Number " + s + " not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 comments를 삭제 실패(Principal) - 403")
    @Transactional
    public void deleteAccountComments_fail_principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<5; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article, account, i);
        }

        String token = createToken(account);
        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber() + ", " + all.get(all.size() - 1).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isForbidden());

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s ->{
            assertDoesNotThrow(
                    () -> this.commentsRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("Number " + s + " not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 comments를 삭제 실패 (not found Account Id) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountComments_fail_AccountId() throws Exception {
        Account account = accountRepository.findByEmail(EMAIL).orElseThrow();

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<5; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article, account, i);
        }

        String token = createToken(account);
        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber() + ", " + all.get(all.size() - 1).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", 1982739548)
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isNotFound());

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s ->{
            assertDoesNotThrow(
                    () -> this.commentsRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("Number " + s + " not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 comments를 삭제 실패(unMatch Account and Principal) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountComments_fail_unMatchAccountAndPrincipal() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setEmail(SEC_EMAIL);
        accountForm.setUsername("test Username");
        Account newAccount = saveAccount(accountForm);

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(newAccount, articleForm);

        for(int i=0; i<15; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article, newAccount, i);
        }
        String token = createToken(account);
        List<Comments> all = commentsRepository.findByAccountId(newAccount.getId());
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber() + ", " + all.get(all.size() - 1).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", newAccount.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isBadRequest());

        String[] split = str.split(", ");
        Arrays.stream(split).forEach(s ->{
            assertDoesNotThrow(
                    () -> this.commentsRepository.findByNumber(Long.parseLong(s))
                            .orElseThrow(() -> new IdNotFoundException("Number " + s + " not found"))
            );
        });
    }

    @Test
    @DisplayName("Account의 comments를 삭제 실패(errorResource check) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteAccountComments_fail_errorResource() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        ArticleForm articleForm = createArticleForm(1);
        Article article = saveArticle(account, articleForm);

        for(int i=0; i<15; i++){
            CommentForm CommentForm = createCommentForm("Test Comment Number." + i);
            saveComments(CommentForm, article, account, i);
        }
        String token = createToken(account);
        String str = "6, 122313, 6237";

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(str))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..errors").exists());
//        조건부 검증을 할 필요가 없다.
//        String[] split = str.split(", ");
//        Arrays.stream(split).forEach(s ->{
//            assertThrows(
//                    IdNotFoundException.class,
//                    () -> this.commentsRepository.findByNumber(Long.parseLong(s))
//                            .orElseThrow(() -> new IdNotFoundException("Number " + s + " not found"))
//            );
//        });
    }

}
