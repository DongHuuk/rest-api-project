package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.exception.IdNotFoundException;
import org.kuroneko.restapiproject.token.AccountVO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ArticleService {
    @Autowired private ModelMapper modelMapper;

    public ArticleDTO wrappingArticleByArticleDTO(Article article) {
        ArticleDTO articleDTO = this.modelMapper.map(article, ArticleDTO.class);
        articleDTO.setAccountId(article.getAccount().getId());
        articleDTO.setUserName(article.getAccount().getUsername());
        articleDTO.setUserEmail(article.getAccount().getEmail());
        articleDTO.setAuthority(article.getAccount().getAuthority() + "");
        articleDTO.setCommunityTitle(article.getCommunity().getTitle());
        return articleDTO;
    }
}
