package org.kuroneko.restapiproject.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.account.AccountMethods;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class CommunityControllerTest extends AccountMethods{

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CommunityService communityService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private ArticleRepository articleRepository;

    private CommunityForm createCommunityForm(String userName) {
        CommunityForm communityForm = new CommunityForm();
        communityForm.setTitle("테스트 커뮤니티");
        communityForm.setManager(userName);
        return communityForm;
    }

    @Test
    public void indexCommunityTest() throws Exception{
        this.mockMvc.perform(get("/community"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("커뮤니티 생성 성공 - 201")
    @WithAccount("Test@test.com")
    @Transactional
    public void createCommunity_success() throws Exception{
        Account account = this.accountRepository.findByEmail("Test@test.com").orElseThrow();
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());

        this.mockMvc.perform(post("/community")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(communityForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated());

        Community community = this.communityRepository.findByTitle("테스트 커뮤니티");
        assertNotNull(community);
    }
    @Test
    @DisplayName("커뮤니티 생성 실패 - 403_FORBIDDEN")
    @WithAccount("Test@test.com")
    @Transactional
    public void createCommunity_403_FORBIDDEN() throws Exception {
        Account account = this.accountRepository.findByEmail("Test@test.com").orElseThrow();
        account.setAuthority(UserAuthority.USER);
        CommunityForm communityForm = this.createCommunityForm(account.getUsername());

        this.mockMvc.perform(post("/community")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaTypes.HAL_JSON)
                    .content(this.objectMapper.writeValueAsString(communityForm))
                    .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());

        List<Community> all = this.communityRepository.findAll();
        assertTrue(all.isEmpty());
    }
    @Test
    @DisplayName("커뮤니티 생성 실패 - 403_FORBIDDEN(Non Principal)")
    @Transactional
    public void createCommunity_403_FORBIDDEN_NON_Account() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());

        this.mockMvc.perform(post("/community")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());

        List<Community> all = this.communityRepository.findAll();
        assertTrue(all.isEmpty());
    }
    @Test
    @DisplayName("커뮤니티 생성 실패 - 400_Bad_Request")
    @WithAccount("Test@test.com")
    @Transactional
    public void createCommunity_400_Bad_Request() throws Exception {
        Account account = this.accountRepository.findByEmail("Test@test.com").orElseThrow();
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        communityForm.setManager("");

        this.mockMvc.perform(post("/community")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors").exists());

        List<Community> all = this.communityRepository.findAll();
        assertTrue(all.isEmpty());
    }

    private void createArticleWithCommunity(int division, Community community, Account account) {
        for (int i = 0; i < 21; i++) {
            ArticleForm articleForm = createArticleForm(division);
            this.communityService.createCommunityInArticle(articleForm, community, account);
        }
    }

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 성공 (전체) - 200")
    public void findCommunityWithArticles_ALL() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(1, community, account);
        this.createArticleWithCommunity(2, community, account);
        this.createArticleWithCommunity(0, community, account);

        this.mockMvc.perform(get("/community/{id}", community.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 성공 (HUMOR) - 200")
    public void findCommunityWithArticles_HUMOR() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(0, community, account);
        this.createArticleWithCommunity(1, community, account);
        this.createArticleWithCommunity(2, community, account);

        this.mockMvc.perform(get("/community/{id}", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("1000"))
                .andDo(print())
                .andExpect(status().isOk());
    }

}