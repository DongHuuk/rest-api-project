package org.kuroneko.restapiproject.article;

import com.querydsl.jpa.JPQLQuery;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.domain.QArticle;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class ArticleRepositoryExtensionImpl extends QuerydslRepositorySupport implements ArticleRepositoryExtension {

    public ArticleRepositoryExtensionImpl() {
        super(Article.class);
    }

    @Override
    public List<Article> findByNumber(List<Long> list) {
        JPQLQuery<Article> articleJPQLQuery = getCommentsQuery(list.size(), list);

        if (articleJPQLQuery == null) {
            throw new NullPointerException("QueryDSL find Article is Null");
        }

        return articleJPQLQuery.fetch();
    }

    private JPQLQuery<Article> getCommentsQuery(int size, List<Long> list){
        QArticle article = QArticle.article;
        switch (size){
            case 1:
                return from(article).where(article.number.eq(list.get(0)));
            case 2:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1))));
            case 3:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1)))
                        .or(article.number.eq(list.get(2))));
            case 4:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1)))
                        .or(article.number.eq(list.get(2)))
                        .or(article.number.eq(list.get(3))));
            case 5:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1)))
                        .or(article.number.eq(list.get(2)))
                        .or(article.number.eq(list.get(3)))
                        .or(article.number.eq(list.get(4))));
            case 6:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1)))
                        .or(article.number.eq(list.get(2)))
                        .or(article.number.eq(list.get(3)))
                        .or(article.number.eq(list.get(4)))
                        .or(article.number.eq(list.get(5))));
            case 7:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1)))
                        .or(article.number.eq(list.get(2)))
                        .or(article.number.eq(list.get(3)))
                        .or(article.number.eq(list.get(4)))
                        .or(article.number.eq(list.get(5)))
                        .or(article.number.eq(list.get(6))));
            case 8:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1)))
                        .or(article.number.eq(list.get(2)))
                        .or(article.number.eq(list.get(3)))
                        .or(article.number.eq(list.get(4)))
                        .or(article.number.eq(list.get(5)))
                        .or(article.number.eq(list.get(6)))
                        .or(article.number.eq(list.get(7))));
            case 9:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1)))
                        .or(article.number.eq(list.get(2)))
                        .or(article.number.eq(list.get(3)))
                        .or(article.number.eq(list.get(4)))
                        .or(article.number.eq(list.get(5)))
                        .or(article.number.eq(list.get(6)))
                        .or(article.number.eq(list.get(7)))
                        .or(article.number.eq(list.get(8))));
            case 10:
                return from(article).where(article.number.eq(list.get(0))
                        .or(article.number.eq(list.get(1)))
                        .or(article.number.eq(list.get(2)))
                        .or(article.number.eq(list.get(3)))
                        .or(article.number.eq(list.get(4)))
                        .or(article.number.eq(list.get(5)))
                        .or(article.number.eq(list.get(6)))
                        .or(article.number.eq(list.get(7)))
                        .or(article.number.eq(list.get(8)))
                        .or(article.number.eq(list.get(9)))).fetchJoin();
        }
        return null;
    }
}
