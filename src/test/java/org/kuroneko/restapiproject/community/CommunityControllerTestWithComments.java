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
public class CommunityControllerTestWithComments extends CommunityMethods {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CommunityService communityService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private CommentsRepository commentsRepository;
    @Autowired private AccountVORepository accountVORepository;
    @Autowired private CommentsService commentsService;

    @AfterEach
    private void deleteAll() {
        this.commentsRepository.deleteAll();
        this.articleRepository.deleteAll();
        this.communityRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.accountVORepository.deleteAll();
    }

    private Community createCommunityAndArticles(String email) {
        Account account = this.accountRepository.findByEmail(email).orElseThrow();
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        createArticleWithCommunity(community, account);
        return community;
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 생성 - 201")
    @WithAccount(EMAIL)
    @Transactional
    public void createComment() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("Test Description");
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(15);

        this.mockMvc.perform(post("/community/{communityId}/article/{articleId}/comments"
                , community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, createToken(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("create-Article-Comments",
                        links(
                                linkWithRel("self").description("계정의 프로필"),
                                linkWithRel("Community Site").description("생성한 커뮤니티로 이동"),
                                linkWithRel("get Article By Community").description("생성한 게시글로 이동"),
                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON을 지원한다."),
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        requestFields(
                                fieldWithPath("description").description("댓글의 내용"),
                                fieldWithPath("originNo").description("댓글 혹은 댓글의 답글에 대한 여부")
                        )
                ));

        List<Comments> commentsList = this.commentsRepository.findAll();
        assertEquals(commentsList.size(), 1);
        Comments comments = commentsList.get(0);
        assertEquals(comments.getDescription(), commentForm.getDescription());
        assertTrue(article.getComments().contains(comments));
        assertTrue(account.getComments().contains(comments));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 생성 (JWT error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void createComment_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("Test Description");
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(15);

        this.mockMvc.perform(post("/community/{communityId}/article/{articleId}/comments"
                , community.getId(), article.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        List<Comments> commentsList = this.commentsRepository.findAll();
        assertNotEquals(commentsList.size(), 1);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 생성 (Principal) - 403")
    @Transactional
    public void createComment_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        account.setAuthority(UserAuthority.ROOT);
        Community community = createCommunityAndArticles(account.getEmail());
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("Test Description");
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(15);
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{communityId}/article/{articleId}/comments"
                , community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isForbidden());

        List<Comments> commentsList = this.commentsRepository.findAll();
        assertNotEquals(commentsList.size(), 1);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 생성 (Form Description Empty) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void createComment_Description_Empty() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("");
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(15);
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{communityId}/article/{articleId}/comments"
                , community.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());

        List<Comments> commentsList = this.commentsRepository.findAll();
        assertNotEquals(commentsList.size(), 1);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 생성 (not Found CommunityId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void createComment_CommunityId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comment Description");
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(15);
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{communityId}/article/{articleId}/comments"
                , 1237261954, article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        List<Comments> commentsList = this.commentsRepository.findAll();
        assertNotEquals(commentsList.size(), 1);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 생성 (not Found ArticleId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void createComment_ArticleId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comment Description");
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(15);
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{communityId}/article/{articleId}/comments"
                , community.getId(), 1234567890)
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        List<Comments> commentsList = this.commentsRepository.findAll();
        assertNotEquals(commentsList.size(), 1);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 생성 (unMatch Article's Community) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void createComment_unMatch() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> all = this.articleRepository.findByCommunity(community);
        Article article = all.get(15);
        Community newCommunity = createCommunityAndArticles(account.getEmail());

        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comment Description");
        String token = createToken(account);

        this.mockMvc.perform(post("/community/{communityId}/article/{articleId}/comments"
                , newCommunity.getId(), article.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        List<Comments> commentsList = this.commentsRepository.findAll();
        assertNotEquals(commentsList.size(), 1);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 수정 성공 - 201")
    @WithAccount(EMAIL)
    @Transactional
    public void updateComment() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comments");
        Comments comments = this.commentsService.createComments(commentForm, account, article);
        String description = "update test comments";
        commentForm.setDescription(description);
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{communityId}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), comments.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("update-Article-Comments",
                        links(
                                linkWithRel("self").description("계정의 프로필"),
                                linkWithRel("Community Site").description("생성한 커뮤니티로 이동"),
                                linkWithRel("get Article By Community").description("생성한 게시글로 이동"),
                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON을 지원한다."),
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        requestFields(
                                fieldWithPath("description").description("댓글의 내용"),
                                fieldWithPath("originNo").description("댓글 혹은 댓글의 답글에 대한 여부")
                        )
                ));

        Comments newComments = this.commentsRepository.findById(comments.getId()).orElseThrow();
        assertEquals(newComments.getDescription(), description);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 수정 실패 (JWT Error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void updateComment_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comments");
        Comments comments = this.commentsService.createComments(commentForm, account, article);
        String description = "update test comments";
        commentForm.setDescription(description);

        this.mockMvc.perform(put("/community/{communityId}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), comments.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        Comments newComments = this.commentsRepository.findById(comments.getId()).orElseThrow();
        assertNotEquals(newComments.getDescription(), description);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 수정 실패 (Principal Error) - 403")
    @Transactional
    public void updateComment_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        Community community = createCommunityAndArticles(account.getEmail());
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comments");
        Comments comments = this.commentsService.createComments(commentForm, account, article);
        String description = "update test comments";
        commentForm.setDescription(description);
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{communityId}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), comments.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isForbidden());

        Comments newComments = this.commentsRepository.findById(comments.getId()).orElseThrow();
        assertNotEquals(newComments.getDescription(), description);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 수정 실패 (CommentForm Description Empty Error) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateComment_Description_Empty() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comments");
        Comments comments = this.commentsService.createComments(commentForm, account, article);
        String description = "";
        commentForm.setDescription(description);
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{communityId}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), comments.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());

        Comments newComments = this.commentsRepository.findById(comments.getId()).orElseThrow();
        assertNotEquals(newComments.getDescription(), description);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 수정 실패 (not Found CommunityId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void updateComment_CommunityId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comments");
        Comments comments = this.commentsService.createComments(commentForm, account, article);
        String description = "update test comments";
        commentForm.setDescription(description);
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{communityId}/article/{articleId}/comments/{commentsId}",
                123456789, article.getId(), comments.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        Comments newComments = this.commentsRepository.findById(comments.getId()).orElseThrow();
        assertNotEquals(newComments.getDescription(), description);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 수정 실패 (not Found ArticleId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void updateComment_ArticleId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comments");
        Comments comments = this.commentsService.createComments(commentForm, account, article);
        String description = "update test comments";
        commentForm.setDescription(description);
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{communityId}/article/{articleId}/comments/{commentsId}",
                community.getId(), 123456789, comments.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        Comments newComments = this.commentsRepository.findById(comments.getId()).orElseThrow();
        assertNotEquals(newComments.getDescription(), description);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 수정 실패 (not Found CommentsId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void updateComment_CommentsId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);
        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comments");
        Comments comments = this.commentsService.createComments(commentForm, account, article);
        String description = "update test comments";
        commentForm.setDescription(description);
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{communityId}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), 123456789)
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        Comments newComments = this.commentsRepository.findById(comments.getId()).orElseThrow();
        assertNotEquals(newComments.getDescription(), description);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 수정 실패 (unMatch Article's Community) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateComment_unMatch() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        List<Article> all = this.articleRepository.findAll();
        Article article = all.get(7);

        CommunityForm communityForm = createCommunityForm(EMAIL);
        Community newCommunity = this.communityService.createCommunity(communityForm, account);

        CommentForm commentForm = new CommentForm();
        commentForm.setDescription("test comments");
        Comments comments = this.commentsService.createComments(commentForm, account, article);
        String description = "update test comments";
        commentForm.setDescription(description);
        String token = createToken(account);

        this.mockMvc.perform(put("/community/{communityId}/article/{articleId}/comments/{commentsId}",
                newCommunity.getId(), article.getId(), comments.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentForm)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Comments newComments = this.commentsRepository.findById(comments.getId()).orElseThrow();
        assertNotEquals(newComments.getDescription(), description);
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 삭제 성공 - 201")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteComment() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        Article article = this.articleRepository.findAll().get(7);
        this.createComment(article, account);
        Comments comments = this.commentsRepository.findAll().get(9);
        String token = createToken(account);
        Long id = comments.getId();

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), id)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-Article-Comments",
                        requestHeaders(
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Redirect URL")
                        )
                ));

        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(id);
        assertTrue(commentsRepositoryById.isEmpty());
        assertFalse(account.getComments().contains(comments));
        assertFalse(comments.getArticle().getComments().contains(comments));
        assertFalse(article.getComments().contains(comments));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 삭제 실패 (JWT error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteComment_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        Article article = this.articleRepository.findAll().get(7);
        this.createComment(article, account);
        Comments comments = this.commentsRepository.findAll().get(9);
        Long id = comments.getId();

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), id))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(id);
        assertFalse(commentsRepositoryById.isEmpty());
        assertTrue(account.getComments().contains(comments));
        assertTrue(comments.getArticle().getComments().contains(comments));
        assertTrue(article.getComments().contains(comments));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 삭제 실패 (Principal error) - 403")
    @Transactional
    public void deleteComment_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        Community community = createCommunityAndArticles(EMAIL);
        Article article = this.articleRepository.findAll().get(7);
        this.createComment(article, account);
        Comments comments = this.commentsRepository.findAll().get(9);
        Long id = comments.getId();
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), id)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isForbidden());

        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(id);
        assertFalse(commentsRepositoryById.isEmpty());
        assertTrue(account.getComments().contains(comments));
        assertTrue(comments.getArticle().getComments().contains(comments));
        assertTrue(article.getComments().contains(comments));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 삭제 실패 (not Found CommunityId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteComment_CommunityId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        Article article = this.articleRepository.findAll().get(7);
        this.createComment(article, account);
        Comments comments = this.commentsRepository.findAll().get(9);
        Long id = comments.getId();
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}/comments/{commentsId}",
                123456789, article.getId(), id)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());

        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(id);
        assertFalse(commentsRepositoryById.isEmpty());
        assertTrue(account.getComments().contains(comments));
        assertTrue(comments.getArticle().getComments().contains(comments));
        assertTrue(article.getComments().contains(comments));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 삭제 실패 (not Found ArticleId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteComment_ArticleId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        Article article = this.articleRepository.findAll().get(7);
        this.createComment(article, account);
        Comments comments = this.commentsRepository.findAll().get(9);
        Long id = comments.getId();
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}/comments/{commentsId}",
                community.getId(), 123456789, id)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());

        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(id);
        assertFalse(commentsRepositoryById.isEmpty());
        assertTrue(account.getComments().contains(comments));
        assertTrue(comments.getArticle().getComments().contains(comments));
        assertTrue(article.getComments().contains(comments));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 삭제 실패 (not Found CommentsId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteComment_CommentsId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);
        Article article = this.articleRepository.findAll().get(7);
        this.createComment(article, account);
        Comments comments = this.commentsRepository.findAll().get(9);
        Long id = comments.getId();
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}/comments/{commentsId}",
                community.getId(), article.getId(), 123456789)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());

        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(id);
        assertFalse(commentsRepositoryById.isEmpty());
        assertTrue(account.getComments().contains(comments));
        assertTrue(comments.getArticle().getComments().contains(comments));
        assertTrue(article.getComments().contains(comments));
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 내 댓글 삭제 실패 (unMatch Article's Community) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteComment_unMatch() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        Community community = createCommunityAndArticles(EMAIL);

        CommunityForm communityForm = createCommunityForm(EMAIL);
        Community newCommunity = this.communityService.createCommunity(communityForm, account);

        Article article = this.articleRepository.findAll().get(7);
        this.createComment(article, account);
        Comments comments = this.commentsRepository.findAll().get(9);
        Long id = comments.getId();
        String token = createToken(account);

        this.mockMvc.perform(delete("/community/{id}/article/{articleId}/comments/{commentsId}",
                newCommunity, article.getId(), id)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Optional<Comments> commentsRepositoryById = this.commentsRepository.findById(id);
        assertFalse(commentsRepositoryById.isEmpty());
        assertTrue(account.getComments().contains(comments));
        assertTrue(comments.getArticle().getComments().contains(comments));
        assertTrue(article.getComments().contains(comments));
    }

}