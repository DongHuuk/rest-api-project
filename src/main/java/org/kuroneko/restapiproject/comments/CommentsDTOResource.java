package org.kuroneko.restapiproject.comments;

import lombok.NoArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

@NoArgsConstructor
public class CommentsDTOResource extends EntityModel<CommentsDTO> {
    public CommentsDTOResource(CommentsDTO commentsDTO, Link... links) {
        super(commentsDTO, links);
    }
}
