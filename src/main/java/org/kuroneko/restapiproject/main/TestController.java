package org.kuroneko.restapiproject.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kuroneko.restapiproject.account.AccountService;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.Article;
import org.kuroneko.restapiproject.domain.ArticleThema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class TestController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/checkbox/test")
    public String checkBox(Model model) throws JsonProcessingException {
        int count = 0;

        Account account = new Account();
        account.setEmail("test@naver.com");
        account.setPassword(this.passwordEncoder.encode("1234567890"));
        account.setUsername("테스트1");
        Account newAccount = accountService.createNewAccount(account);

        while (true) {
            if (count >= 5) {
                break;
            }
            newAccount.getArticle().add(createArticle(count));

            count++;
        }

        model.addAttribute("account", this.objectMapper.writeValueAsString(newAccount));

        return "checkboxTest";
    }

    private Article createArticle(int count) {
        Article article = new Article();
        article.setDivision(ArticleThema.CHAT);
        article.setCreateTime(LocalDateTime.now());
        article.setDescription("테스트" + UUID.randomUUID());
        article.setNumber(count);
        article.setTitle("테스트" + UUID.randomUUID());
        return articleRepository.save(article);
    }

}
