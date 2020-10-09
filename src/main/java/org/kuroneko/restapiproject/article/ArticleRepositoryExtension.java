package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.community.domain.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ArticleRepositoryExtension {
    List<Article> findByNumber(List<Long> list);

    Page<Article> findByCommunityWithPageable(Community community, Pageable pageable);
}
