package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.*;
import org.kuroneko.restapiproject.comments.CommentsRepository;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.community.domain.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommunityService {

    @Autowired CommunityRepository communityRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired ArticleRepository articleRepository;
    @Autowired ModelMapper modelMapper;
    @Autowired CommentsRepository commentsRepository;

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
        Account manager = community.getManager();
        manager.getCommunities().remove(community);
        List<Article> articleList = this.articleRepository.findByCommunity(community);
        this.articleRepository.deleteInBatch(articleList);
        this.communityRepository.delete(community);
    }

    public void updateCommunity(Community community, CommunityForm communityForm, Account account) {
        community.setManager(account);
        community.setTitle(communityForm.getTitle());
        this.communityRepository.save(community);
    }

    public Article createArticleInCommunity(ArticleForm articleForm, Community community, Account account) {
        Account newAccount = this.accountRepository.findById(account.getId()).orElseThrow();
        Article article = new Article();
        article.setTitle(articleForm.getTitle());
        article.setDescription(articleForm.getDescription());
        article.setSource(articleForm.getSource());
        article.setCreateTime(LocalDateTime.now());
        article.setAccount(newAccount);
        article.setCommunity(community);
        article.setAgree((int)(Math.random() * 50));
        article.setDisagree((int) (Math.random() * 30));
        article.setReport((int) (Math.random() * 30));
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
        account.getArticle().add(newArticle);
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

        /*
            Page<ArticleDTO> resultList = this.communityService
                    .createPageableArticleWithAccount(pageable, account, communityRepositoryById.get());
            PagedModel<EntityModel<ArticleDTO>> getArticles = assembler.toModel(resultList,
                    linkTo(CommunityController.class).slash(id + "/article").withRel("get Articles"));
            return new ResponseEntity(getArticles, HttpStatus.OK);
     */

    }

    public List<ArticleDTOByMainPage> articleWrappingByArticleDTOByMainPage(List<Article> articles) {
        List<ArticleDTOByMainPage> list = new ArrayList<>();
        articles.stream().forEach(article -> {
            ArticleDTOByMainPage articleDTOByMainPage = new ArticleDTOByMainPage();
            articleDTOByMainPage.setId(article.getId());
            articleDTOByMainPage.setNumber(article.getNumber());
            articleDTOByMainPage.setDivision(article.getDivision());
            articleDTOByMainPage.setTitle(article.getTitle());
            Account account = article.getAccount();
            articleDTOByMainPage.setAccountId(account.getId());
            articleDTOByMainPage.setAccountUsername(account.getUsername());
            articleDTOByMainPage.setCreateTime(article.getCreateTime());
            articleDTOByMainPage.setCommentCnt(article.getComments().size());
            articleDTOByMainPage.setCommunityName(article.getCommunity().getTitle());

            list.add(articleDTOByMainPage);
        });

        return list;
    }

    public Page<ArticleDTOByMainPage> articleWrappingByArticleDTOByMainPage(Page<Article> articles) {
        return articles.map(article -> {
            ArticleDTOByMainPage articleDTOByMainPage = new ArticleDTOByMainPage();
            articleDTOByMainPage.setId(article.getId());
            articleDTOByMainPage.setNumber(article.getNumber());
            articleDTOByMainPage.setDivision(article.getDivision());
            articleDTOByMainPage.setTitle(article.getTitle());
            Account account = article.getAccount();
            articleDTOByMainPage.setAccountId(account.getId());
            articleDTOByMainPage.setAccountUsername(account.getUsername());
            articleDTOByMainPage.setCreateTime(article.getCreateTime());
            articleDTOByMainPage.setCommentCnt(article.getComments().size());
            articleDTOByMainPage.setCommunityName(article.getCommunity().getTitle());
            return articleDTOByMainPage;
        });
    }

    //TODO 중복인지 아닌지 모르겟음 최적화 작업하면서 수정
    public Page<ArticleDTO> wrappingByArticle(Page<Article> articles) {
        return articles.map(article -> {
            ArticleDTO map = this.modelMapper.map(article, ArticleDTO.class);
            Account account = article.getAccount();
            map.setCommunityTitle(article.getCommunity().getTitle());
            map.setAccountId(account.getId());
            map.setUserName(account.getUsername());
            map.setUserEmail(account.getEmail());
            map.setAuthority(account.getAuthority() + "");
            return map;
        });
    }

    public Article updateArticleInCommunity(ArticleForm articleForm, Community community, Article article) {
        article.setTitle(articleForm.getTitle());
        article.setDescription(articleForm.getDescription());
        article.setSource(articleForm.getSource());
        article.setUpdateTime(LocalDateTime.now());
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
        Article newArticle = this.articleRepository.save(article);
        community.getArticle().add(newArticle);
        this.communityRepository.save(community);

        return newArticle;
    }

    public void deleteArticleInCommunity(Article article, Community community, Account account) {
        community.getArticle().remove(article);
        account.getArticle().remove(article);
        List<Comments> byArticle = this.commentsRepository.findByArticle(article);
        this.commentsRepository.deleteAllByIdInQuery(byArticle.stream().map(Comments::getId).collect(Collectors.toList()));
        this.articleRepository.delete(article);

    }

    public CommunityDTO findCommunityAndArticles() {
        List<Community> communities = this.communityRepository.findAll();
        CommunityDTO communityDTO = new CommunityDTO();
        communities.forEach(community -> {
            TestDTO testDTO = new TestDTO();
            CommunityMiniDTO communityMiniDTO = new CommunityMiniDTO();
            communityMiniDTO.setCommunityId(community.getId());
            communityMiniDTO.setCommunityTitle(community.getTitle());
            List<Article> articles = this.articleRepository.findTop4ByCommunityOrderByCreateTimeDesc(community);
            articles.forEach(article -> {
                ArticleMiniDTO articleMiniDTO = new ArticleMiniDTO();
                articleMiniDTO.setArticleId(article.getId());
                articleMiniDTO.setArticleTitle(article.getTitle());
                articleMiniDTO.setCommentsCount(article.getComments().size());
                testDTO.getArticleMiniDTO().add(articleMiniDTO);
            });
            testDTO.getCommunityMiniDTO().add(communityMiniDTO);
            communityDTO.getTestDTO().add(testDTO);
        });

        return communityDTO;
    }
}
