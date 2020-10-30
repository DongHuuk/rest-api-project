package org.kuroneko.restapiproject.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.article.domain.ArticleThema;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.comments.CommentsService;
import org.kuroneko.restapiproject.comments.domain.CommentForm;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.config.WithAccount;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class CommunityControllerTestWithArticle extends CommunityMethods {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CommunityService communityService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CommunityRepository communityRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CommentsRepository commentsRepository;
    @Autowired
    private AccountVORepository accountVORepository;
    @Autowired
    private CommentsService commentsService;

    @AfterEach
    private void deleteAll() {
        this.commentsRepository.deleteAll();
        this.articleRepository.deleteAll();
        this.communityRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.accountVORepository.deleteAll();
    }

    private void multyUsers() {
        AccountForm accountForm_2 = createAccountForm();
        accountForm_2.setUsername("테스트2");
        accountForm_2.setEmail("test@test2.com");
        AccountForm accountForm_3 = createAccountForm();
        accountForm_3.setUsername("테스트3");
        accountForm_3.setEmail("test@test3.com");
        saveAccount(accountForm_2);
        saveAccount(accountForm_3);
    }

    private Community createCommunityAndArticles(String email) {
        Account account = this.accountRepository.findByEmail(email).orElseThrow();
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        createArticleWithCommunity(community, account);
        return community;
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 - 201")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunityInArticle() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("_links").exists())
                .andDo(document("create-Community-Article",
                    links(
                            linkWithRel("self").description("계정의 프로필"),
                            linkWithRel("Community Site").description("생성한 커뮤니티로 이동"),
                            linkWithRel("get Article By Community").description("생성한 게시글로 이동"),
                            linkWithRel("DOCS").description("REST API DOCS")
                    ),
                    requestHeaders(
                            headerWithName(AuthConstants.AUTH_HEADER).description("JWT"),
                            headerWithName(org.springframework.http.HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON을 지원한다."),
                            headerWithName(org.springframework.http.HttpHeaders.ACCEPT).description("이 API에서는 HAL을 지원한다.")
                    ),
                    requestFields(
                            fieldWithPath("title").description("게시글의 제목"),
                            fieldWithPath("description").description("게시글의 내용"),
                            fieldWithPath("source").description("게시글 내용의 출처"),
                            fieldWithPath("division").description("게시글의 유형")
                    )
                ));

        List<Article> articleList = this.articleRepository.findAll();
        assertFalse(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (JWT Error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunityInArticle_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (Principal Error) - 403")
    @Transactional
    public void createCommunityInArticle_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (Form Title(Empty) Error) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunityInArticle_titleEmpty() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setTitle("");
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (Form Title Maximum Error) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunityInArticle_titleMaximum() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setTitle("qwertyuiopqwertyuiopqwertyuiopqwertyuiopqwertyuiopqwertyuiopq");
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (Form description Empty Error) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunityInArticle_descriptionEmpty() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDescription("");
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (not Found Community Id) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunityInArticle_CommunityId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{id}/article", 1283671)
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 조회 (With Account) - 200")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleInCommunity() throws Exception {
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                community.getId(), articleList.get(5).getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links").exists())
                .andDo(document("get-Community-Article",
                        links(
                                linkWithRel("Community Site").description("생성한 커뮤니티로 이동"),
                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("number").description("게시글의 순번"),
                                fieldWithPath("title").description("게시글의 제목"),
                                fieldWithPath("description").description("게시글의 내용"),
                                fieldWithPath("source").description("게시글에 첨부파일 등이 있다면 그에 대한 출처 정보"),
                                fieldWithPath("division").description("게시글의 글 유형"),
                                fieldWithPath("createTime").description("게시글이 생성된 시간"),
                                fieldWithPath("updateTime").description("게시글이 수정된 시간"),
                                fieldWithPath("comments").description("게시글의 댓글들"),
                                fieldWithPath("report").description("게시글의 신고 횟수"),
                                fieldWithPath("accountId").description("게시글을 가지고 있는 유저의 Id"),
                                fieldWithPath("userName").description("게시글을 가지고 있는 유저의 이름"),
                                fieldWithPath("userEmail").description("게시글을 가지고 있는 유저의 이메일"),
                                fieldWithPath("authority").description("게시글을 가지고 있는 유저의 접근권한")
                        )
                ));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 조회 - 200")
    @Transactional
    public void findArticleInCommunityWithAccount() throws Exception {
        //TODO 로그인 여부에 따라 동작의 차이가 백엔드에서 작업이 이루어져야 할 경우 DOCS 생성
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> articleList = this.articleRepository.findAll();

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                community.getId(), articleList.get(5).getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links").exists());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 조회 (not Found CommunityId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleInCommunity_CommunityId() throws Exception {
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                19283754, articleList.get(5).getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 조회 (not Found ArticleId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleInCommunity_ArticleId() throws Exception {
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                community.getId(), 1927364))
                .andDo(print())
                .andExpect(status().isNotFound());
    }



    @Test
    @DisplayName("커뮤니티 내 게시글 조회 (unMatch Article's Community) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleInCommunity_unMatch_ArticleWithCommunity() throws Exception {
        Community community = createCommunityAndArticles(EMAIL);

        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        communityForm.setTitle("second Community Site");
        Community community_2 = this.communityService.createCommunity(communityForm, account);
        createArticleWithCommunity(community_2, account);

        List<Article> articleList = this.articleRepository.findByCommunity(community_2);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                community.getId(), articleList.get(5).getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 삭제 - 200")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteArticleInCommunity() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = this.createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(12);

        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-Community-Article",
                        requestHeaders(
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Redirect URL")
                        )
                ));

        Optional<Article> result = this.articleRepository.findById(article.getId());
        assertTrue(result.isEmpty());
        assertFalse(community.getArticle().contains(article));
        assertFalse(account.getArticle().contains(article));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 삭제(Principal) - 403")
    @Transactional
    public void deleteArticleInCommunity_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        Community community = this.createCommunityAndArticles(account.getEmail());
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(12);
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isForbidden());

        Optional<Article> result = this.articleRepository.findById(article.getId());
        assertFalse(result.isEmpty());
        assertTrue(community.getArticle().contains(article));
        assertTrue(account.getArticle().contains(article));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 삭제(not Found CommunityId) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteArticleInCommunity_CommunityId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = this.createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(12);
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}",
                1234567890, article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());

        Optional<Article> result = this.articleRepository.findById(article.getId());
        assertFalse(result.isEmpty());
        assertTrue(community.getArticle().contains(article));
        assertTrue(account.getArticle().contains(article));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 삭제(not Fount Article Id) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteArticleInCommunity_ArticleId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = this.createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(12);
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}",
                community.getId(), 1234567890)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());

        Optional<Article> result = this.articleRepository.findById(article.getId());
        assertFalse(result.isEmpty());
        assertTrue(community.getArticle().contains(article));
        assertTrue(account.getArticle().contains(article));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 삭제(Article unMatch By Community) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteArticleInCommunity_unMatchCommunity() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = this.createCommunityAndArticles(EMAIL);

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        communityForm.setTitle("second Community Site");
        Community community_2 = this.communityService.createCommunity(communityForm, account);
        createArticleWithCommunity(community_2, account);

        List<Article> articleList = this.articleRepository.findByCommunity(community);
        Article article = articleList.get(12);
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}",
                community_2.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Optional<Article> result = this.articleRepository.findById(article.getId());
        assertFalse(result.isEmpty());
        assertTrue(community.getArticle().contains(article));
        assertTrue(account.getArticle().contains(article));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 화면 이동 (수정 화면으로 이동) - 200")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleWithCommunity() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        String token = createToken(account);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Community-Article-modify",
                        links(
                                linkWithRel("self").description("계정의 프로필"),
                                linkWithRel("Community Site").description("생성한 커뮤니티로 이동"),
                                linkWithRel("get Article By Community").description("생성한 게시글로 이동"),
                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("number").description("게시글의 순번"),
                                fieldWithPath("title").description("게시글의 제목"),
                                fieldWithPath("description").description("게시글의 내용"),
                                fieldWithPath("source").description("게시글에 첨부파일 등이 있다면 그에 대한 출처 정보"),
                                fieldWithPath("division").description("게시글의 글 유형"),
                                fieldWithPath("createTime").description("게시글이 생성된 시간"),
                                fieldWithPath("updateTime").description("게시글이 수정된 시간"),
                                fieldWithPath("comments").description("게시글의 댓글들"),
                                fieldWithPath("report").description("게시글의 신고 횟수"),
                                fieldWithPath("accountId").description("게시글을 가지고 있는 유저의 Id"),
                                fieldWithPath("userName").description("게시글을 가지고 있는 유저의 이름"),
                                fieldWithPath("userEmail").description("게시글을 가지고 있는 유저의 이메일"),
                                fieldWithPath("authority").description("게시글을 가지고 있는 유저의 접근권한")
                        )
                ));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 화면 이동 (JWT error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleWithCommunity_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 화면 이동 (Principal error) - 403")
    @Transactional
    public void findArticleWithCommunity_Principal() throws Exception {
        AccountForm accountForm = this.createAccountForm();
        Account account = saveAccount(accountForm);
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        String token = createToken(account);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 화면 이동 (not Found CommunityId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleWithCommunity_CommunityId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        String token = createToken(account);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}/modify",
                123456789, article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 화면 이동 (not Found ArticleId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleWithCommunity_ArticleId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        String token = createToken(account);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}/modify",
                community.getId(), 123456789)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 화면 이동 (unMatch Article's Community) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleWithCommunity_unMatchCommunity() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community2 = this.communityService.createCommunity(communityForm, account);
        String token = createToken(account);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}/modify",
                community2.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 화면 이동 (unMatch Article's Principal) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void findArticleWithCommunity_unMatchAccount() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setEmail(SEC_EMAIL);
        accountForm.setUsername("test2 username");
        Account account2 = saveAccount(accountForm);
        Community community = createCommunityAndArticles(account2.getEmail());
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        String token = createToken(account);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 - 200")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("Update Article Title");
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("update-Community-Article",
                        links(
                                linkWithRel("self").description("계정의 프로필"),
                                linkWithRel("Community Site").description("생성한 커뮤니티로 이동"),
                                linkWithRel("get Article By Community").description("생성한 게시글로 이동"),
                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(org.springframework.http.HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON을 지원한다."),
                                headerWithName(org.springframework.http.HttpHeaders.ACCEPT).description("이 API에서는 HAL을 지원한다."),
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        requestFields(
                                fieldWithPath("title").description("게시글의 제목"),
                                fieldWithPath("description").description("게시글의 내용"),
                                fieldWithPath("source").description("게시글 내용의 출처"),
                                fieldWithPath("division").description("게시글의 유형")
                        )
                ));

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertEquals(newArticle.getTitle(), articleForm.getTitle());
        assertEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (JWT Error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("Update Article Title");

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getTitle(), articleForm.getTitle());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (Principal Error) - 403")
    @Transactional
    public void updateArticleWithCommunity_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("Update Article Title");
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isForbidden());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getTitle(), articleForm.getTitle());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (ArticleForm Title Error) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity_ArticleFormByTitle() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("");
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors").exists());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getTitle(), articleForm.getTitle());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (ArticleForm Maximum Title Error) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity_ArticleFormByMaximumTitle() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("qwertyuiopqwertyuiopqwertyuiopqwertyuiopqwertyuiopq");
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors").exists());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getTitle(), articleForm.getTitle());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (ArticleForm Description Error) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity_ArticleFormByDescription() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setDescription("");
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors").exists());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getDescription(), articleForm.getDescription());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (not Found CommunityId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity_CommunityId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("update Article Title");
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                1928375, article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getTitle(), articleForm.getTitle());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (not Found ArticleId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity_ArticleId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> articleList = this.articleRepository.findAll();
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("update Article Title");
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community.getId(), 1827301)
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getTitle(), articleForm.getTitle());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (Article Match Problem(article and community)) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity_ArticleMatchProblem() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        communityForm.setTitle("second Community Site");
        Community community_2 = this.communityService.createCommunity(communityForm, account);
        createArticleWithCommunity(community, account);

        List<Article> articleList = this.articleRepository.findByCommunity(community);
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("update Article Title");
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community_2.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getTitle(), articleForm.getTitle());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 (Article Match Problem(article and account)) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateArticleWithCommunity_ArticleMatchProblem_2() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountForm accountForm = createAccountForm();
        accountForm.setEmail(SEC_EMAIL);
        accountForm.setUsername("test2 username");
        Account newAccount = saveAccount(accountForm);

        Community community = createCommunityAndArticles(newAccount.getEmail());
        List<Article> articleList = this.articleRepository.findByCommunity(community);
        Article article = articleList.get(5);
        article.setDivision(ArticleThema.HUMOR);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDivision(1);
        articleForm.setTitle("update Article Title");

        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify",
                community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, createToken(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Article newArticle = this.articleRepository.findById(article.getId()).orElseThrow();
        assertNotEquals(newArticle.getTitle(), articleForm.getTitle());
        assertNotEquals(newArticle.getDivision(), ArticleThema.CHAT);
    }
}