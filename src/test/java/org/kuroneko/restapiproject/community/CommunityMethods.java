package org.kuroneko.restapiproject.community;

import org.junit.jupiter.api.extension.ExtendWith;
import org.kuroneko.restapiproject.account.AccountMethods;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.kuroneko.restapiproject.comments.CommentsService;
import org.kuroneko.restapiproject.comments.domain.CommentForm;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CommunityMethods extends AccountMethods {

    @Autowired CommunityService communityService;
    @Autowired CommentsService commentsService;

    protected CommunityForm createCommunityForm(String userName) {
        CommunityForm communityForm = new CommunityForm();
        communityForm.setTitle("테스트 커뮤니티");
        communityForm.setManager(userName);
        return communityForm;
    }

    protected void createArticleWithCommunity(Community community, Account account) {
        int count = 0;
        for (int i = 0; i < 66; i++) {
            ArticleForm articleForm = createArticleForm(count);
            this.communityService.createArticleInCommunity(articleForm, community, account);
            count++;
            if (count >= 3) {
                count = 0;
            }
        }
    }

    protected void createComment(Article article, Account account) {
        for (int i = 0; i < 30; i++) {
            CommentForm commentForm = new CommentForm();
            commentForm.setDescription("No." + i + " - test Description");
            this.commentsService.createComments(commentForm, account, article);
        }
    }
}
