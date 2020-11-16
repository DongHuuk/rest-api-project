package org.kuroneko.restapiproject;

import org.hibernate.boot.archive.internal.JarFileBasedArchiveDescriptor;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.AccountService;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.ArticleService;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.comments.CommentsService;
import org.kuroneko.restapiproject.comments.domain.CommentForm;
import org.kuroneko.restapiproject.community.CommunityRepository;
import org.kuroneko.restapiproject.community.CommunityService;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.kuroneko.restapiproject.token.AccountVORepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

/*
    H2 DB 환경에서 기본적인 세팅값들(유저, 커뮤니티 사이트, 사이트 내의 게시글, 게시글의 댓글 등을 세팅해주는 객체
 */

@RestController
public class AppInit {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountVORepository accountVORepository;
    @Autowired
    private CommunityService communityService;
    @Autowired
    private CommunityRepository communityRepository;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private CommentsRepository commentsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;

    private void multySaveArticleInCommunity(int count, Community community, Account account){
        int division = 0;
        ArticleForm articleForm = new ArticleForm();
        for (int i = 0; i<count; i++){
            articleForm.setTitle("Test Article No." + i);
            articleForm.setDivision(division);
            articleForm.setDescription("This is Init Articles Number." + i);
            if (division >=3){
                division = 0;
                articleForm.setSource("Init Source No." + i);
            }
            division++;
            Article article = this.communityService.createArticleInCommunity(articleForm, community, account);
            this.multyCreateComment(5, article, account);
        }
    }

    private void multyCreateComment(int count, Article article, Account account){
        for (int i =0; i<count; i++){
            CommentForm commentForm = new CommentForm();
            commentForm.setDescription("Init Comment No." + i);
            if(i % 3 == 1){
                commentForm.setOriginNo(true);
            }
            this.commentsService.createComments(commentForm, account, article);
        }
    }

    @GetMapping("/init")
    @Transactional
    public ResponseEntity initDataByH2DB(){
        AccountForm accountForm = new AccountForm();
        accountForm.setEmail("initAccount@gmail.com");
        accountForm.setUsername("Root");
        accountForm.setPassword("1234567890-=");
        accountForm.setCheckingPassword("1234567890-=");
        Account account = this.accountService.createNewAccount(this.modelMapper.map(accountForm, Account.class));
        account.setAuthority(UserAuthority.ROOT);
        CommunityForm communityForm = new CommunityForm();
        communityForm.setTitle("Init Community No.1");
        communityForm.setManager(account.getUsername());
        Community community1 = this.communityService.createCommunity(communityForm, account);
        communityForm.setTitle("Init Community No.2");
        Community community2 = this.communityService.createCommunity(communityForm, account);
        this.multySaveArticleInCommunity(195, community1, account);
        this.multySaveArticleInCommunity(195, community2, account);

        return new ResponseEntity<>( HttpStatus.CREATED);
    }

}
