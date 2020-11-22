package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.article.domain.ArticleDTOByOpinion;
import org.kuroneko.restapiproject.comments.CommentsDTO;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.token.AccountVO;
import org.kuroneko.restapiproject.token.AccountVORepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleService {
    @Autowired private ModelMapper modelMapper;
    @Autowired private AccountVORepository accountVORepository;

    public ArticleDTO wrappingArticleByArticleDTO(Article article) {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(article.getId());
        articleDTO.setNumber(article.getNumber());
        articleDTO.setTitle(article.getTitle());
        articleDTO.setDescription(article.getDescription());
        articleDTO.setSource(article.getSource());
        articleDTO.setDivision(article.getDivision());
        articleDTO.setCreateTime(article.getCreateTime());
        articleDTO.setUpdateTime(article.getUpdateTime());
        articleDTO.setReport(article.getReport());
        articleDTO.setAccountId(article.getAccount().getId());
        articleDTO.setUserName(article.getAccount().getUsername());
        articleDTO.setUserEmail(article.getAccount().getEmail());
        articleDTO.setAuthority(article.getAccount().getAuthority() + "");
        articleDTO.setCommunityTitle(article.getCommunity().getTitle());
        articleDTO.setAgree(article.getAgree());
        articleDTO.setDisagree(article.getDisagree());
        article.getComments().forEach(comments -> {
            CommentsDTO commentsDTO = this.modelMapper.map(comments, CommentsDTO.class);
            commentsDTO.setCommentNumber(comments.getNumber());
            commentsDTO.setAccountId(comments.getAccount().getId());
            commentsDTO.setAccountUsername(comments.getAccount().getUsername());
            commentsDTO.setArticleId(comments.getId());
            commentsDTO.setArticleNumber(comments.getNumber());
            articleDTO.getComments().add(commentsDTO);
        });

        return articleDTO;
    }

    //t = +, f = -
    @Transactional
    public Article updateOpinionVal(boolean b, Article article, AccountVO accountVO) {
        if (b) {
            article.setAgree(article.getAgree() + 1);
            article.getAgreeList().add(accountVO);
        }else {
            article.setDisagree(article.getDisagree() + 1);
            article.getDisagreeList().add(accountVO);
        }

        return article;
    }


    public ArticleDTOByOpinion wrappingArticleByArticleDTOByOpinion(Article article) {
        return ArticleDTOByOpinion.builder()
                .id(article.getId())
                .number(article.getNumber())
                .agree(article.getAgree())
                .disagree(article.getDisagree())
                .build();
    }

}
