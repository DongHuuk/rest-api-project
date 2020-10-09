package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.QCommunity;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class CommunityRepositoryExtensionImpl extends QuerydslRepositorySupport implements CommunityRepositoryExtension {
    public CommunityRepositoryExtensionImpl() {
        super(Community.class);
    }
}
