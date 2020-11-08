package org.kuroneko.restapiproject.community;

import com.querydsl.jpa.JPQLQuery;
import org.kuroneko.restapiproject.article.domain.QArticle;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.QCommunity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.function.Predicate;

public class CommunityRepositoryExtensionImpl extends QuerydslRepositorySupport implements CommunityRepositoryExtension {
    public CommunityRepositoryExtensionImpl() {
        super(Community.class);
    }


}
