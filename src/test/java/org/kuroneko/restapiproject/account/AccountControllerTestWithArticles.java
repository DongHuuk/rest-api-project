package org.kuroneko.restapiproject.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestBodySnippet;
import org.springframework.security.config.web.servlet.headers.HttpPublicKeyPinningDsl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
public class AccountControllerTestWithArticles extends AccountMethods{

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ArticleRepository articleRepository;

    @AfterEach
    private void deleteAccountRepository_After(){
        this.articleRepository.deleteAll();
        this.accountRepository.deleteAll();
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
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON-HAL 지원한다.")
                        ),
                        responseFields(beneathPath("_embedded.articleDTOList"),
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
                        responseFields(beneathPath("page"),
                                fieldWithPath("size").description("한 페이지의 최대 갯수"),
                                fieldWithPath("totalElements").description("총 게시글 수"),
                                fieldWithPath("totalPages").description("총 page 수"),
                                fieldWithPath("number").description("현재 페이지")
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

        for(int i = 0; i<5; i++){
            ArticleForm articleForm = createArticleForm(1);
            saveArticle(account, articleForm, i);
        }

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

        for(int i = 0; i<15; i++){
            ArticleForm articleForm = createArticleForm(1);
            saveArticle(account, articleForm, i);
        }

        List<Article> all = articleRepository.findAll();
        String str = all.get(0).getNumber() + ", " + all.get(4).getNumber();

        this.mockMvc.perform(delete("/accounts/{id}/articles", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-Account-Article",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("AJAX로 Json 타입의 숫자 + ','의 값을 보낸다. ex) 1, 3, 5")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Redirect URL")
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

        for(int i = 0; i<15; i++){
            ArticleForm articleForm = createArticleForm(1);
            saveArticle(newAccount, articleForm, i);
        }

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

        for(int i = 0; i<15; i++){
            ArticleForm articleForm = createArticleForm(1);
            saveArticle(account, articleForm, i);
        }

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

        for(int i = 0; i<15; i++){
            ArticleForm articleForm = createArticleForm(1);
            saveArticle(account, articleForm, i);
        }

        List<Article> all = articleRepository.findAll();
        String str = all.get(0).getId() + ", " + all.get(2).getId();

        this.mockMvc.perform(delete("/accounts/{id}/articles", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(str)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
