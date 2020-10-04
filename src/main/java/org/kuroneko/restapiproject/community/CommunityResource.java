package org.kuroneko.restapiproject.community;

import lombok.NoArgsConstructor;
import org.kuroneko.restapiproject.community.domain.Community;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

@NoArgsConstructor
public class CommunityResource extends EntityModel<Community> {
    public CommunityResource(Community content, Link... link) {
        super(content, link);
    }
}
