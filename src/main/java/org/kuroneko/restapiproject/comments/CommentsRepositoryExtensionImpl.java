package org.kuroneko.restapiproject.comments;

import com.querydsl.jpa.JPQLQuery;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.domain.QComments;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class CommentsRepositoryExtensionImpl extends QuerydslRepositorySupport implements CommentsRepositoryExtension {

    public CommentsRepositoryExtensionImpl() {
        super(Comments.class);
    }

    @Override
    public List<Comments> findByNumber(List<Long> list) {
        JPQLQuery<Comments> commentsQuery = getCommentsQuery(list.size(), list);

        if (commentsQuery == null) {
            throw new NullPointerException("QueryDSL find Comments is Null");
        }

        return commentsQuery.fetch();
    }

    private JPQLQuery<Comments> getCommentsQuery(int size, List<Long> list){
        QComments comments = QComments.comments;
        switch (size){
            case 1:
                return from(comments).where(comments.number.eq(list.get(0)));
            case 2:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1))));
            case 3:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1)))
                        .or(comments.number.eq(list.get(2))));
            case 4:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1)))
                        .or(comments.number.eq(list.get(2)))
                        .or(comments.number.eq(list.get(3))));
            case 5:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1)))
                        .or(comments.number.eq(list.get(2)))
                        .or(comments.number.eq(list.get(3)))
                        .or(comments.number.eq(list.get(4))));
            case 6:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1)))
                        .or(comments.number.eq(list.get(2)))
                        .or(comments.number.eq(list.get(3)))
                        .or(comments.number.eq(list.get(4)))
                        .or(comments.number.eq(list.get(5))));
            case 7:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1)))
                        .or(comments.number.eq(list.get(2)))
                        .or(comments.number.eq(list.get(3)))
                        .or(comments.number.eq(list.get(4)))
                        .or(comments.number.eq(list.get(5)))
                        .or(comments.number.eq(list.get(6))));
            case 8:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1)))
                        .or(comments.number.eq(list.get(2)))
                        .or(comments.number.eq(list.get(3)))
                        .or(comments.number.eq(list.get(4)))
                        .or(comments.number.eq(list.get(5)))
                        .or(comments.number.eq(list.get(6)))
                        .or(comments.number.eq(list.get(7))));
            case 9:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1)))
                        .or(comments.number.eq(list.get(2)))
                        .or(comments.number.eq(list.get(3)))
                        .or(comments.number.eq(list.get(4)))
                        .or(comments.number.eq(list.get(5)))
                        .or(comments.number.eq(list.get(6)))
                        .or(comments.number.eq(list.get(7)))
                        .or(comments.number.eq(list.get(8))));
            case 10:
                return from(comments).where(comments.number.eq(list.get(0))
                        .or(comments.number.eq(list.get(1)))
                        .or(comments.number.eq(list.get(2)))
                        .or(comments.number.eq(list.get(3)))
                        .or(comments.number.eq(list.get(4)))
                        .or(comments.number.eq(list.get(5)))
                        .or(comments.number.eq(list.get(6)))
                        .or(comments.number.eq(list.get(7)))
                        .or(comments.number.eq(list.get(8)))
                        .or(comments.number.eq(list.get(9)))).fetchJoin();
        }
        return null;
    }

}
