package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.article.domain.ArticleThema;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import javax.swing.plaf.ComponentUI;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class CommunityService {

    @Autowired CommunityRepository communityRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired ArticleRepository articleRepository;
    @Autowired ModelMapper modelMapper;

    public Community createCommunity(CommunityForm communityForm, Account account) {
        Community community = new Community();
        community.setTitle(communityForm.getTitle());
        community.setManager(account);
        community.setCreateTime(LocalDateTime.now());
        Community newCommunity = communityRepository.save(community);
        account.getCommunities().add(newCommunity);
        return newCommunity;
    }

    public void deleteCommunity(Community community) {
        this.communityRepository.delete(community);
    }

    public void updateCommunity(Community community, CommunityForm communityForm, Account account) {
        community.setManager(account);
        community.setTitle(communityForm.getTitle());
        this.communityRepository.save(community);
    }

    public Article createCommunityInArticle(ArticleForm articleForm, Community community, Account account) {
        Account newAccount = this.accountRepository.findById(account.getId()).orElseThrow();
        Article article = new Article();
        article.setTitle(articleForm.getTitle());
        article.setDescription(articleForm.getDescription());
        article.setSource(articleForm.getSource());
        article.setCreateTime(LocalDateTime.now());
        article.setAccount(newAccount);
        article.setCommunity(community);
        switch (articleForm.getDivision()) {
            case 0:
                article.setDivision(ArticleThema.HUMOR);
                break;
            case 2:
                article.setDivision(ArticleThema.QUESTION);
                break;
            default:
                article.setDivision(ArticleThema.CHAT);
        }
        long count = this.articleRepository.count();
        article.setNumber(count);
        Article newArticle = this.articleRepository.save(article);
        community.getArticle().add(newArticle);
        this.communityRepository.save(community);

        return newArticle;
    }

    public Page<ArticleDTO> createPageableArticleWithAccount(Pageable pageable, Account account, Community community) {
        Page<Article> articles = this.articleRepository.findByAccountIdAndCommunityId(account.getId(), community.getId(), pageable);
        return articles.map(article -> {
            ArticleDTO map = this.modelMapper.map(article, ArticleDTO.class);
            map.setAccountId(account.getId());
            map.setUserName(account.getUsername());
            map.setUserEmail(account.getEmail());
            map.setAuthority(account.getAuthority() + "");
            return map;
        });
    }

    public Page<ArticleDTO> wrappingByArticle(Page<Article> articles) {
        return articles.map(article -> {
            ArticleDTO map = this.modelMapper.map(article, ArticleDTO.class);
            Account account = article.getAccount();
            map.setAccountId(account.getId());
            map.setUserName(account.getUsername());
            map.setUserEmail(account.getEmail());
            map.setAuthority(account.getAuthority() + "");
            return map;
        });
    }
}
