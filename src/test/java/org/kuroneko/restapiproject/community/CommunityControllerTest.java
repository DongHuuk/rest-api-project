package org.kuroneko.restapiproject.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.RestDocsConfiguration;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class CommunityControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CommunityService communityService;
    @Autowired private ObjectMapper objectMapper;

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
                .andExpect(status().isCreated());
    }

}