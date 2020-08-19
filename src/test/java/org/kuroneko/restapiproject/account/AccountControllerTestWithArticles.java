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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
public class AccountControllerTestWithArticles {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ArticleRepository articleRepository;

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
                .andExpect(status().is3xxRedirection())
                .andDo(document("delete-articles",
                        links(
                                linkWithRel("self").description("해당 Account Profile로 이동"),
                                linkWithRel("getArticles").description("해당 Account의 get_artile로 이동")
                        ),
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
                                fieldWithPath("_links.getArticles.href").description("Account의 게시글들을 보여주는 get Link")
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
