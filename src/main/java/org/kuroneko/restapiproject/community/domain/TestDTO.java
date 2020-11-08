package org.kuroneko.restapiproject.community.domain;

import lombok.Data;
import org.kuroneko.restapiproject.article.domain.ArticleMiniDTO;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestDTO {

    private List<CommunityMiniDTO> communityMiniDTO = new ArrayList<>();
    private List<ArticleMiniDTO> articleMiniDTO = new ArrayList<>();
}
