package org.kuroneko.restapiproject.community;

import lombok.NoArgsConstructor;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.Set;

@NoArgsConstructor
public class CommunityDTOResource extends EntityModel<CommunityDTO> {
    public CommunityDTOResource(CommunityDTO content, Link... link) {
        super(content, link);
    }
}
