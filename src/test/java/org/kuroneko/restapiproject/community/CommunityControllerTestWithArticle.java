package org.kuroneko.restapiproject.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.config.WithAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @AfterEach
    private void deleteAll() {
        this.articleRepository.deleteAll();
        this.communityRepository.deleteAll();
        this.accountRepository.deleteAll();
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

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 - 201")
    @WithAccount("test@testT.com")
    @Transactional
    public void createCommunityInArticle() throws Exception {
        Account account = this.accountRepository.findByEmail("test@testT.com").orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("_links").exists());

        List<Article> articleList = this.articleRepository.findAll();
        assertFalse(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (Form Title(Empty) Error) - 400")
    @WithAccount("test@testT.com")
    @Transactional
    public void createCommunityInArticle_titleEmpty() throws Exception {
        Account account = this.accountRepository.findByEmail("test@testT.com").orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setTitle("");

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (Form Title Maximum Error) - 400")
    @WithAccount("test@testT.com")
    @Transactional
    public void createCommunityInArticle_titleMaximum() throws Exception {
        Account account = this.accountRepository.findByEmail("test@testT.com").orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setTitle("qwertyuiopqwertyuiopqwertyuiopqwertyuiopqwertyuiopqwertyuiopq");

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (Form description Empty Error) - 400")
    @WithAccount("test@testT.com")
    @Transactional
    public void createCommunityInArticle_descriptionEmpty() throws Exception {
        Account account = this.accountRepository.findByEmail("test@testT.com").orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);
        articleForm.setDescription("");

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

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

        this.mockMvc.perform(post("/community/{id}/article", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 작성 (URL BadRequest) - 404")
    @WithAccount("test@testT.com")
    @Transactional
    public void createCommunityInArticle_URL() throws Exception {
        Account account = this.accountRepository.findByEmail("test@testT.com").orElseThrow();
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        ArticleForm articleForm = createArticleForm(0);

        this.mockMvc.perform(post("/community/{id}/article", 1283671)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(articleForm))
                .accept(MediaTypes.HAL_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        List<Article> articleList = this.articleRepository.findAll();
        assertTrue(articleList.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 조회 (With Account) - 200")
    @WithAccount("test@testT.com")
    @Transactional
    public void findArticleInCommunity() throws Exception {
        Community community = createCommunityAndArticles("test@testT.com");
        List<Article> articleList = this.articleRepository.findAll();

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                community.getId(), articleList.get(5).getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links").exists());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 조회 - 200")
    @Transactional
    public void findArticleInCommunityWithAccount() throws Exception {
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
    @DisplayName("커뮤니티 내 게시글 조회 (unMatch Community) - 400")
    @WithAccount("test@testT.com")
    @Transactional
    public void findArticleInCommunity_unMatch_Community() throws Exception {
        Community community = createCommunityAndArticles("test@testT.com");
        List<Article> articleList = this.articleRepository.findAll();

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                19283754, articleList.get(5).getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 조회 (unMatch Article) - 400")
    @WithAccount("test@testT.com")
    @Transactional
    public void findArticleInCommunity_unMatch_Article() throws Exception {
        Community community = createCommunityAndArticles("test@testT.com");
        List<Article> articleList = this.articleRepository.findAll();

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                community.getId(), 1927364))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private Community createCommunityAndArticles(String email) {
        Account account = this.accountRepository.findByEmail(email).orElseThrow();
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = this.communityService.createCommunity(communityForm, account);
        createArticleWithCommunity(community, account);
        return community;
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 조회 (unMatch Article's Community) - 400")
    @WithAccount("test@testT.com")
    @Transactional
    public void findArticleInCommunity_unMatch_ArticleWithCommunity() throws Exception {
        Community community = createCommunityAndArticles("test@testT.com");
        Community community_2 = createCommunityAndArticles("test@testT.com");

        List<Article> articleList = this.articleRepository.findByCommunity(community_2);

        this.mockMvc.perform(get("/community/{id}/article/{articleId}",
                community.getId(), articleList.get(5).getId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("커뮤니티 내 게시글 수정 - 200")
    @WithAccount("test@testT.com")
    @Transactional
    public void updateArticleWithCommunity() throws Exception {
        Community community = createCommunityAndArticles("test@testT.com");

//        this.mockMvc.perform(put("/community/{id}/article/{articleId}/modify", ))
    }
}