package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.comments.CommentsDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleService {
    @Autowired private ModelMapper modelMapper;

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
}
