package org.kuroneko.restapiproject.account;

import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountForm;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.ArticleThema;
import org.kuroneko.restapiproject.comments.CommentsForm;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.notification.NotificationRepository;
import org.kuroneko.restapiproject.notification.domain.Notification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class AccountMethods {

    @Autowired private AccountRepository accountRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private CommentsRepository commentsRepository;
    @Autowired private NotificationRepository notificationRepository;

    public AccountForm createAccountForm(){
        AccountForm accountForm = new AccountForm();
        accountForm.setEmail("Test@email.com");
        accountForm.setPassword("12341234");
        accountForm.setCheckingPassword("12341234");
        accountForm.setUsername("테스트1");
        return accountForm;
    }

    public Account saveAccount(AccountForm accountForm) {
        Account account = modelMapper.map(accountForm, Account.class);
        account.setAuthority(UserAuthority.USER);
        account.setCreateTime(LocalDateTime.now());
        account.setPassword(this.passwordEncoder.encode(accountForm.getPassword()));

        return accountRepository.save(account);
    }

    public ArticleForm createArticleForm(int division){
        ArticleForm articleForm = new ArticleForm();
        articleForm.setTitle("Test title number 1");
        articleForm.setDescription("This is Test Article description");
        articleForm.setSource("source @nullable");
        articleForm.setDivision(division);
        return articleForm;
    }

    public Article saveArticle(Account account, ArticleForm articleForm) {
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

    public Article saveArticle(Account account, ArticleForm articleForm, int i) {
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

    public CommentsForm createCommentsForm(String message){
        CommentsForm commentsForm = new CommentsForm();
        commentsForm.setDescription(message);
        return commentsForm;
    }

    public Comments saveComments(CommentsForm commentsForm, Article article, Account account, int i){
        Comments comments = new Comments();
        comments.setDescription(commentsForm.getDescription());
        comments.setCreateTime(LocalDateTime.now().plusHours(i));
        this.commentsRepository.save(comments);
        comments.setNumber(comments.getId() + 1);
        article.setComments(comments);
        account.setComments(comments);
        return comments;
    }

    public void saveNotification(Article article, Account account){
        Notification notification = new Notification();
        notification.setChecked(false);
        notification.setArticle(article);
        notification.setAccount(account);
        notification.setCreateTime(LocalDateTime.now());
        notificationRepository.save(notification);
        notification.setNumber(notification.getId() + 1);
        account.getNotification().add(notification);
    }

}
