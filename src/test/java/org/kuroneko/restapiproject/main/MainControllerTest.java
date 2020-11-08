package org.kuroneko.restapiproject.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.LoginForm;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.comments.domain.CommentForm;
import org.kuroneko.restapiproject.community.CommunityMethods;
import org.kuroneko.restapiproject.community.CommunityService;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.config.WithAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.kuroneko.restapiproject.account.AccountMethods.EMAIL;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest extends CommunityMethods {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CommunityService communityService;
    @Autowired
    private ArticleRepository articleRepository;

    //TODO Controller 작성 후 Test code 작성해야 할 것
    @Test
    @WithAccount(EMAIL)
    @Transactional
    public void indexCommunityTest() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        CommunityForm communityForm1 = createCommunityForm(EMAIL);
        Community community1 = this.communityService.createCommunity(communityForm1, account);
        CommunityForm communityForm2 = createCommunityForm(EMAIL);
        Community community2 = this.communityService.createCommunity(communityForm2, account);
        CommunityForm communityForm3 = createCommunityForm(EMAIL);
        Community community3 = this.communityService.createCommunity(communityForm3, account);
        CommentForm commentForm = createCommentForm("test1");

        this.createArticleWithCommunity(community1, account);
        this.createArticleWithCommunity(community2, account);
        this.createArticleWithCommunity(community3, account);

        List<Article> byCommunity = this.articleRepository.findByCommunity(community1);
        Article article = byCommunity.get(0);
        this.saveComments(commentForm, article, account, 2);

        this.mockMvc.perform(get("/community"))
                .andDo(print())
                .andExpect(status().isOk());

        assertEquals(article.getComments().size(), 1);
    }
}