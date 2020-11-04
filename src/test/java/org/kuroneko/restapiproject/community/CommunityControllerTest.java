package org.kuroneko.restapiproject.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import org.kuroneko.restapiproject.token.AccountVO;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class CommunityControllerTest extends CommunityMethods {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CommunityService communityService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private AccountVORepository accountVORepository;

    @AfterEach
    private void deleteAll() {
        this.articleRepository.deleteAll();
        this.communityRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.accountVORepository.deleteAll();
    }

    //TODO Controller 작성 후 Test code 작성해야 할 것
    @Test
    public void indexCommunityTest() throws Exception {
        this.mockMvc.perform(get("/community"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("커뮤니티 생성 성공 - 201")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunity_success() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.MASTER);
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());

        String token = createToken(account);

        this.mockMvc.perform(post("/community")
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("create-Community",
                        links(
                                linkWithRel("move Community").description("move Community")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("이 API에서는 JSON을 지원한다."),
                                headerWithName(HttpHeaders.ACCEPT).description("이 API에서는 HAL을 지원한다."),
                                headerWithName(AuthConstants.AUTH_HEADER).description("JWT")
                        )
                ));

        Community community = this.communityRepository.findByTitle("테스트 커뮤니티");
        assertNotNull(community);
    }

    @Test
    @DisplayName("커뮤니티 생성 실패 (Authority Error) - 403")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunity_403_FORBIDDEN() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.USER);
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = this.createCommunityForm(account.getUsername());
        String token = createToken(account);

        this.mockMvc.perform(post("/community")
                .header(AuthConstants.AUTH_HEADER, token)
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
    @DisplayName("커뮤니티 생성 실패 (Principal error) - 403")
    @Transactional
    public void createCommunity_403_FORBIDDEN_NON_Account() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());

        String token = createToken(account);

        this.mockMvc.perform(post("/community")
                .header(AuthConstants.AUTH_HEADER, token)
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
    @DisplayName("커뮤니티 생성 실패 (not Found Form Manager Field) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunity_404_Bad_Request() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.MASTER);
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        communityForm.setManager(account.getUsername() + "281321");

        String token = createToken(account);

        this.mockMvc.perform(post("/community")
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        List<Community> all = this.communityRepository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("커뮤니티 생성 실패 (Validation Error) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void createCommunity_400_Bad_Request() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.MASTER);
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        communityForm.setTitle("");

        String token = createToken(account);

        this.mockMvc.perform(post("/community")
                .header(AuthConstants.AUTH_HEADER, token)
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

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 성공 (전체) With Community - 200")
    public void findArticleWithCommunity_ALL() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(community, account);

        this.mockMvc.perform(get("/community/{id}", community.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 성공 (전체) - 200")
    public void findArticle_ALL() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(community, account);

        this.mockMvc.perform(get("/community/{id}", community.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Community-Article",
                        links(
                                linkWithRel("first").description("첫 페이지"),
                                linkWithRel("next").description("다음 페이지"),
                                linkWithRel("last").description("마지막 페이지"),
                                linkWithRel("self").description("Account Profile")
//                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("게시글의 유형과는 관계없이 모든글을 순차적으로 리턴한다. Application/JSON Type")
                        ),
                        responseFields(beneathPath("page"),
                                fieldWithPath("size").description("한 페이지의 최대 갯수"),
                                fieldWithPath("totalElements").description("총 게시글 수"),
                                fieldWithPath("totalPages").description("총 page 수"),
                                fieldWithPath("number").description("현재 페이지")
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
                        )
                ));
    }

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 성공 (HUMOR) - 200")
    public void findArticleWithCommunity_HUMOR() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(community, account);

        this.mockMvc.perform(get("/community/{id}", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("1000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-Community-Article",
                        links(
                                linkWithRel("first").description("첫 페이지"),
                                linkWithRel("next").description("다음 페이지"),
                                linkWithRel("last").description("마지막 페이지"),
                                linkWithRel("self").description("Account Profile")
//                                linkWithRel("DOCS").description("REST API DOCS")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("1000 - HUMOR, 2000 - CHAT, 3000 - QUESTION 타입의 게시글을 리턴한다. Application/JSON Type")
                        ),
                        responseFields(beneathPath("page"),
                                fieldWithPath("size").description("한 페이지의 최대 갯수"),
                                fieldWithPath("totalElements").description("총 게시글 수"),
                                fieldWithPath("totalPages").description("총 page 수"),
                                fieldWithPath("number").description("현재 페이지")
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
                        )
                ));
    }

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 성공 (CHAT) - 200")
    public void findArticleWithCommunity_CHAT() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(community, account);

        this.mockMvc.perform(get("/community/{id}", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("2000"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 성공 (QUESTION) - 200")
    public void findArticleWithCommunity_QUESTION() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(community, account);

        this.mockMvc.perform(get("/community/{id}", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("3000"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 실패 (잘못된 Community Id 요청) - 400")
    public void findArticleWithCommunity_fail() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(community, account);

        this.mockMvc.perform(get("/community/158231"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("특정 커뮤니티의 게시글 요청 실패 (Cate 범위 초과) - 400")
    public void findArticleWithCommunity_fail_cate() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);
        this.createArticleWithCommunity(community, account);

        this.mockMvc.perform(get("/community/{id}", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("5000"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    //TODO 여기부
    @Test
    @DisplayName("특정 커뮤니티 수정 - 200")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.ROOT);
        Account currentAccount = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        currentAccount.setAuthority(UserAuthority.ROOT);
        CommunityForm communityForm = createCommunityForm(currentAccount.getUsername());
        Community community = communityService.createCommunity(communityForm, currentAccount);

        AccountForm accountForm = createAccountForm();
        accountForm.setEmail("test2@test.com");
        accountForm.setUsername("test2 username");
        Account account = saveAccount(accountForm);
        account.setAuthority(UserAuthority.MASTER);
        communityForm.setManager(account.getUsername());
        

        String token = createToken(currentAccount);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isOk());

        Community updateCommunity = this.communityRepository.findById(community.getId()).orElseThrow();
        assertNotEquals(updateCommunity.getManager().getUsername(), currentAccount.getUsername());
        assertEquals(updateCommunity.getManager(), account);
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (Valid Form field by Title) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity_400_Form_Title() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.ROOT);

        Account currentAccount = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        currentAccount.setAuthority(UserAuthority.ROOT);

        CommunityForm communityForm = createCommunityForm(currentAccount.getUsername());
        Community community = communityService.createCommunity(communityForm, currentAccount);

        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("test2 username");
        accountForm.setEmail("test2@testT.com");
        Account account = saveAccount(accountForm);
        account.setAuthority(UserAuthority.MASTER);
        communityForm.setTitle("");
        communityForm.setManager(account.getUsername());

        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());

        Community updateCommunity = this.communityRepository.findById(community.getId()).orElseThrow();
        assertNotEquals(updateCommunity.getManager().getUsername(), account.getUsername());
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (Valid Form field by Manager) - 400")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity_400_Form_Username() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.ROOT);

        Account currentAccount = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        currentAccount.setAuthority(UserAuthority.ROOT);

        CommunityForm communityForm = createCommunityForm(currentAccount.getUsername());
        Community community = communityService.createCommunity(communityForm, currentAccount);

        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("test2 username");
        accountForm.setEmail("test2@testT.com");
        Account account = saveAccount(accountForm);
        account.setAuthority(UserAuthority.MASTER);

        String title = "업데이트 타이틀";
        communityForm.setTitle(title);
        communityForm.setManager(null);

        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Community updateCommunity = this.communityRepository.findById(community.getId()).orElseThrow();
        assertNotEquals(updateCommunity.getTitle(), title);
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (Not Found CommunityId) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity_400_CommunityId() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.ROOT);

        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        account.setAuthority(UserAuthority.ROOT);

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);

        String token = createToken(account);
        String title = "update community title";
        communityForm.setTitle(title);

        this.mockMvc.perform(put("/community/{id}", 21362916)
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        Community updateCommunity = this.communityRepository.findById(community.getId()).orElseThrow();
        assertNotEquals(updateCommunity.getTitle(), title);
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (Not Found Username) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity_400_Username() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.ROOT);

        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        account.setAuthority(UserAuthority.ROOT);

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);

        String token = createToken(account);
        String title = "update community Title";
        communityForm.setManager("Not Found Account Username");
        communityForm.setTitle(title);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isNotFound());

        Community updateCommunity = this.communityRepository.findById(community.getId()).orElseThrow();
        assertNotEquals(updateCommunity.getTitle(), title);
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (Principal is Null) - 403")
    @Transactional
    public void updateCommunity_403_Principal() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account currentAccount = saveAccount(accountForm);
        currentAccount.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(currentAccount.getUsername());
        Community community = communityService.createCommunity(communityForm, currentAccount);

        String token = createToken(currentAccount);
        String title = "update community title";
        communityForm.setTitle(title);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isForbidden());

        Community updateCommunity = this.communityRepository.findById(community.getId()).orElseThrow();
        assertNotEquals(updateCommunity.getTitle(), title);
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (Principal Authentication Error) - 403")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity_403_CurrentAccount() throws Exception {
        Account currentAccount = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.USER);
        CommunityForm communityForm = createCommunityForm(currentAccount.getUsername());
        Community community = communityService.createCommunity(communityForm, currentAccount);

        String token = createToken(currentAccount);
        String title = "update community title";
        communityForm.setTitle(title);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isForbidden());

        Community updateCommunity = this.communityRepository.findById(community.getId()).orElseThrow();
        assertNotEquals(updateCommunity.getTitle(), title);
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (JWT Error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity_3xx_JWT() throws Exception {
        Account currentAccount = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        CommunityForm communityForm = createCommunityForm(currentAccount.getUsername());
        Community community = communityService.createCommunity(communityForm, currentAccount);

        String title = "update community title";
        communityForm.setTitle(title);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        Community updateCommunity = this.communityRepository.findById(community.getId()).orElseThrow();
        assertNotEquals(updateCommunity.getTitle(), title);
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (Not Matching Manager and Account) - 403")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity_403_notMatching() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.ROOT);
        Account dbAccount = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        dbAccount.setAuthority(UserAuthority.ROOT);

        AccountForm accountForm = createAccountForm();
        accountForm.setUsername("test2 username");
        accountForm.setEmail("test2@testT.com");
        Account account = saveAccount(accountForm);
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);

        String token = createToken(account);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("특정 커뮤니티 수정 (Manager Authority Error) - 403")
    @WithAccount(EMAIL)
    @Transactional
    public void updateCommunity_403_ManagerAuthority() throws Exception {
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        accountVO.setAuthority(UserAuthority.ROOT);

        Account currentAccount = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        currentAccount.setAuthority(UserAuthority.ROOT);

        CommunityForm communityForm = createCommunityForm(currentAccount.getUsername());
        Community community = communityService.createCommunity(communityForm, currentAccount);
        community.getManager().setAuthority(UserAuthority.USER);

        String token = createToken(currentAccount);

        this.mockMvc.perform(put("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(communityForm)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("특정 커뮤니티 삭제 - 204")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteCommunity() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        account.setAuthority(UserAuthority.MASTER);
        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);

        createArticleWithCommunity(community, account);
        String token = createToken(account);

        List<Article> before = this.articleRepository.findAll();
        assertFalse(before.isEmpty());

        this.mockMvc.perform(delete("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNoContent());

        List<Article> after = this.articleRepository.findAll();
        Optional<Community> after_community = this.communityRepository.findById(community.getId());
        assertTrue(after.isEmpty());
        assertTrue(after_community.isEmpty());
    }

    @Test
    @DisplayName("특정 커뮤니티 삭제 (JWT Error) - 3xx")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteCommunity_3xx_JWT() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);

        createArticleWithCommunity(community, account);

        List<Article> before = this.articleRepository.findAll();
        assertFalse(before.isEmpty());

        this.mockMvc.perform(delete("/community/{id}", community.getId()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        List<Article> after = this.articleRepository.findAll();
        Optional<Community> after_community = this.communityRepository.findById(community.getId());
        assertFalse(after.isEmpty());
        assertFalse(after_community.isEmpty());
    }

    @Test
    @DisplayName("특정 커뮤니티 삭제 (Principal Null) - 403")
    @Transactional
    public void deleteCommunity_FORBIDDEN_NULL() throws Exception {
        AccountForm accountForm = createAccountForm();
        Account account = saveAccount(accountForm);
        account.setAuthority(UserAuthority.USER);
        AccountVO accountVO = this.accountVORepository.findByEmail(account.getEmail()).orElseThrow();
        accountVO.setAuthority(UserAuthority.USER);

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);

        String token = createToken(account);
        createArticleWithCommunity(community, account);

        List<Article> before = this.articleRepository.findAll();
        assertFalse(before.isEmpty());

        this.mockMvc.perform(delete("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isForbidden());

        List<Article> after = this.articleRepository.findAll();
        Optional<Community> after_community = this.communityRepository.findById(community.getId());
        assertFalse(after.isEmpty());
        assertFalse(after_community.isEmpty());
    }

    @Test
    @DisplayName("특정 커뮤니티 삭제 (Principal Authority Error) - 403")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteCommunity_FORBIDDEN() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();
        AccountVO accountVO = this.accountVORepository.findByEmail(EMAIL).orElseThrow();
        account.setAuthority(UserAuthority.USER);
        accountVO.setAuthority(UserAuthority.USER);

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);

        createArticleWithCommunity(community, account);
        String token = createToken(account);

        List<Article> before = this.articleRepository.findAll();
        assertFalse(before.isEmpty());

        this.mockMvc.perform(delete("/community/{id}", community.getId())
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isForbidden());

        List<Article> after = this.articleRepository.findAll();
        Optional<Community> after_community = this.communityRepository.findById(community.getId());
        assertFalse(after.isEmpty());
        assertFalse(after_community.isEmpty());
    }

    @Test
    @DisplayName("특정 커뮤니티 삭제 (Not Found Community) - 404")
    @WithAccount(EMAIL)
    @Transactional
    public void deleteCommunity_CommunityId() throws Exception {
        Account account = this.accountRepository.findByEmail(EMAIL).orElseThrow();

        CommunityForm communityForm = createCommunityForm(account.getUsername());
        Community community = communityService.createCommunity(communityForm, account);

        createArticleWithCommunity(community, account);
        String token = createToken(account);

        List<Article> before = this.articleRepository.findAll();
        assertFalse(before.isEmpty());

        this.mockMvc.perform(delete("/community/{id}", 1927391)
                .header(AuthConstants.AUTH_HEADER, token))
                .andDo(print())
                .andExpect(status().isNotFound());

        List<Article> after = this.articleRepository.findAll();
        Optional<Community> after_community = this.communityRepository.findById(community.getId());
        assertFalse(after.isEmpty());
        assertFalse(after_community.isEmpty());
    }
}