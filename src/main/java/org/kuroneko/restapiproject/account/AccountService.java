package org.kuroneko.restapiproject.account;

import javassist.NotFoundException;
import org.kuroneko.restapiproject.article.ArticleDTO;
import org.kuroneko.restapiproject.comments.CommentsDTO;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.domain.*;
import org.kuroneko.restapiproject.notification.NotificationDTO;
import org.kuroneko.restapiproject.notification.NotificationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private org.kuroneko.restapiproject.article.ArticleRepository articleRepository;
    @Autowired
    private CommentsRepository commentsRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ModelMapper modelMapper;

    public Account createNewAccount(Account account) {
        account.setCreateTime(LocalDateTime.now());
        account.setAuthority(UserAuthority.USER);
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        return accountRepository.save(account);
    }

    public Errors checkAccountEmailAndUsername(AccountForm accountForm, Errors errors) {
        Optional<Account> byEmail = accountRepository.findByEmail(accountForm.getEmail());
        String username = accountForm.getUsername();

        List<String> filter = accountRepository.findAll().stream()
                .map(Account::getUsername)
                .filter(a -> a.equals(username))
                .collect(Collectors.toList());

        if (!filter.isEmpty()) {
            errors.rejectValue("username", "wrong.username", "duplicate username. check please");
        }

        if (byEmail.isPresent()) {
            errors.rejectValue("email", "wrong.email", "duplicate Email. check please");
        }
        return errors;
    }

    public Errors checkUpdateAccount(AccountForm accountForm, Errors errors, Account account) {
        List<Account> all = this.accountRepository.findAll();
        List<Long> collect = new ArrayList<>();
        String username = accountForm.getUsername();

        List<String> filter = all.stream()
                .map(Account::getUsername)
                .filter(a -> a.equals(username))
                .collect(Collectors.toList());

        if (!filter.isEmpty()) {
            List<Account> accountList = accountRepository.findAllByUsername(username);
            collect = accountList.stream().map(Account::getId).filter(id -> !account.getId().equals(id)).collect(Collectors.toList());
            // !collect.isEmpty() => "테스트15"는 사용 못한다.
        }

        if (!collect.isEmpty()) {
            errors.rejectValue("username", "wrong.username", "duplicate username. check please");
        }

        if (!this.passwordEncoder.matches(accountForm.getPassword(), account.getPassword())) {
            errors.rejectValue("password", "wrong.password", "unMatch Password. check please");
        }

        return errors;
    }

    public Account updateAccount(Account account) {
        account.setUpdateTime(LocalDateTime.now());
        return accountRepository.save(account);
    }

    public void deleteAccount(Account account) {
        this.accountRepository.delete(account);
    }

    public void findArticlesAndDelete(Account accountWithArticles, String checked) throws NotFoundException {
        String[] split = checked.split(",");
        List<Long> collect = Arrays.stream(split).map(s -> {
            s = s.trim();
            return Long.valueOf(s);
        }).collect(Collectors.toList());

        List<Article> byNumber = this.articleRepository.findByNumber(collect);

        if (byNumber.isEmpty()) {
            throw new NotFoundException("Not Found Articles by Numbers");
        }

        for (Article  article : byNumber) {
            if (accountWithArticles.getArticle().contains(article)) {
                accountWithArticles.getArticle().remove(article);
            }
        }
        this.articleRepository.deleteAllByIdInQuery(byNumber.stream().map(Article::getNumber).collect(Collectors.toList()));
    }

    public void findCommentsAndDelete(Account accountWithComments, String checked) throws NotFoundException {
        String[] split = checked.split(",");
        List<Long> collect = Arrays.stream(split).map(s -> {
            s = s.trim();
            return Long.valueOf(s);
        }).collect(Collectors.toList());

        List<Comments> byNumber = this.commentsRepository.findByNumber(collect);

        if (byNumber.isEmpty()) {
            throw new NotFoundException("Not Found Articles by Numbers");
        }

        for (Comments comments : byNumber) {
            if (accountWithComments.getComments().contains(comments)) {
                accountWithComments.getComments().remove(comments);
                comments.getArticle().getComments().remove(comments);
//                this.commentsRepository.delete(comments);
            }
        }
        this.commentsRepository.deleteAllByIdInQuery(byNumber.stream().map(Comments::getId).collect(Collectors.toList()));
//        this.commentsRepository.deleteInBatch(byNumber);
    }

    public void findNotificationAndDelete(Account accountWithNotification, String checked) throws NotFoundException {
        String[] split = checked.split(",");
        List<Long> collect = Arrays.stream(split).map(s -> {
            s = s.trim();
            return Long.valueOf(s);
        }).collect(Collectors.toList());

        List<Notification> byNumber = this.notificationRepository.findByNumber(collect);

        if (byNumber.isEmpty()) {
            throw new NotFoundException("Not Found Articles by Numbers");
        }

        for (Notification notification : byNumber) {
            if (accountWithNotification.getNotification().contains(notification)) {
                accountWithNotification.getNotification().remove(notification);
            }
        }
        this.notificationRepository.deleteAllByIdInQuery(byNumber.stream().map(Notification::getId).collect(Collectors.toList()));
//        this.notificationRepository.deleteInBatch(byNumber);
    }

    public Page<ArticleDTO> createPageableArticle(Long id, Pageable pageable, Account account) {
        Page<Article> pageableArticle = articleRepository.findByAccountId(id, pageable);
        return pageableArticle.map(article -> {
            ArticleDTO map = modelMapper.map(article, ArticleDTO.class);
            map.setAccountId(id);
            map.setUserName(account.getUsername());
            map.setUserEmail(account.getEmail());
            map.setAuthority(account.getAuthority() + "");
            return map;
        });
    }

    public Page<CommentsDTO> createPageableComments(Long id, Pageable pageable, Account account) {
        Page<Comments> pageableComments = commentsRepository.findByAccountId(id, pageable);
        return pageableComments.map(comments -> {
            CommentsDTO c = modelMapper.map(comments, CommentsDTO.class);
            if (comments.getArticle() != null) {
                c.setArticleId(comments.getArticle().getId());
                c.setArticleNumber(comments.getArticle().getNumber());
            }
            return c;
        });

    }

    public Page<NotificationDTO> createPageableNotification(Long id, Pageable pageable, Account account) {
        Page<Notification> pageableComments = notificationRepository.findByAccountId(id, pageable);
        return pageableComments.map(notification -> {
            NotificationDTO c = modelMapper.map(notification, NotificationDTO.class);
            if (notification.getArticle() != null) {
                c.setArticleId(notification.getArticle().getId());
                c.setArticleNumber(notification.getArticle().getNumber());
            }
            if (notification.getComments() != null) {
                c.setCommentsId(notification.getComments().getId());
                c.setCommentsNumber(notification.getComments().getNumber());
            }
            c.setAccountId(notification.getAccount().getId());
            c.setAccountUsername(notification.getAccount().getUsername());
            return c;
        });
    }


}
