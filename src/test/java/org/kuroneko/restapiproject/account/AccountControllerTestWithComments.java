package org.kuroneko.restapiproject.account;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.domain.Article;
import org.kuroneko.restapiproject.domain.Comments;
import org.kuroneko.restapiproject.notification.NotificationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @AfterEach
    private void deleteAccountRepository_After(){
        this.notificationRepository.deleteAll();
        this.commentsRepository.deleteAll();
        this.articleRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Account의 Comments를 조회 성공")
    @WithAccount("test@naver.com")
    @Transactional
    public void findAccountsComments() throws Exception{
        Account account = this.accountRepository.findByEmail("test@naver.com").orElseThrow();

        ArticleForm articleForm_1 = createArticleForm(1);
        Article article_1 = saveArticle(account, articleForm_1);
        ArticleForm articleForm_2 = createArticleForm(1);
        Article article_2 = saveArticle(account, articleForm_2);
        ArticleForm articleForm_3 = createArticleForm(1);
        Article article_3 = saveArticle(account, articleForm_3);

        for(int i=0; i<22; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article_1, account, i);
        }
        for(int i=0; i<15; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article_2, account, i);
        }
        for(int i=0; i<15; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article_3, account, i);
        }

        this.mockMvc.perform(get("/accounts/{id}/comments", account.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Account-Comments",
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Account Profile을 보여주는 Link"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON-HAL 지원한다.")
                        ),
                        responseFields(beneathPath("content"),
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
                        relaxedResponseFields(
                                fieldWithPath("last").description("끝 페이지인가에 대한 여부"),
                                fieldWithPath("totalPages").description("총 페이시 수"),
                                fieldWithPath("totalElements").description("페이지의 요소들의 총 갯수"),
                                fieldWithPath("size").description("페이지 내에 존재하는 최대 요소들의 갯수"),
                                fieldWithPath("first").description("첫 페이지인가에 대한 여부"),
                                fieldWithPath("number").description("현재 페이지 번호"),
                                fieldWithPath("sort.sorted").description("정렬의 적용 여부"),
                                fieldWithPath("empty").description("리스트가 비어있는지의 여부")
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

        for(int i=0; i<17; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article, account, i);
        }

        List<Comments> all = commentsRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(2).getNumber()+ ", " + all.get(4).getNumber()+ ", " + all.get(6).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/comments", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andDo(document("delete-comments",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content Type")
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
                                fieldWithPath("_links.getComments.href").description("Account의 Comments를 받을 수 있는 Link")
                        )
//                        relaxedResponseFields(beneathPath("article"),
//                                fieldWithPath("id").description("게시글의 identification"),
//                                fieldWithPath("number").description("게시글의 순번"),
//                                fieldWithPath("title").description("게시글의 제목"),
//                                fieldWithPath("description").description("게시글의 내용"),
//                                fieldWithPath("source").description("게시글에 첨부파일 등이 있다면 그에 대한 출처 정보"),
//                                fieldWithPath("division").description("게시글의 글 유형"),
//                                fieldWithPath("createTime").description("게시글이 생성된 시간"),
//                                fieldWithPath("updateTime").description("게시글이 수정된 시간"),
//                                fieldWithPath("comments").description("게시글의 댓글들"),
//                                fieldWithPath("report").description("게시글의 신고 횟수")
//                        )
//                        relaxedResponseFields(beneathPath("comments"),
//                                fieldWithPath("id").description("댓글의 identification"),
//                                fieldWithPath("number").description("댓글의 순번"),
//                                fieldWithPath("description").description("댓글의 내용"),
//                                fieldWithPath("createTime").description("댓글이 생성된 시간"),
//                                fieldWithPath("agree").description("댓글의 추천 수"),
//                                fieldWithPath("disagree").description("댓글의 비추천 수"),
//                                fieldWithPath("report").description("댓글의 신고 수"),
//                                fieldWithPath("originNo").description("댓글 위치 값(순서)"),
//                                fieldWithPath("groupOrd").description("댓글과 답글의 구분")
//                        )

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

        for(int i=0; i<15; i++){
            CommentsForm commentsForm = createCommentsForm("Test Comment Number." + i);
            saveComments(commentsForm, article, newAccount, i);
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
            saveComments(commentsForm, article, account, i);
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
            saveComments(commentsForm, article, account, i);
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

}
