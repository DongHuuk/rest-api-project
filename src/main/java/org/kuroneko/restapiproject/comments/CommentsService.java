package org.kuroneko.restapiproject.comments;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.ArticleRepository;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.comments.domain.CommentForm;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.token.AccountVO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentsService {
    @Autowired private CommentsRepository commentsRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private ModelMapper modelMapper;

    public Comments createComments(CommentForm commentForm, Account secAccount, Article article) {
        Account account = this.accountRepository.findById(secAccount.getId()).orElseThrow();
        long count = this.commentsRepository.count();
        Comments comments = new Comments();
        comments.setDescription(commentForm.getDescription());
        comments.setNumber(count+1);
        comments.setCreateTime(LocalDateTime.now());
        comments.setAgree(0);
        comments.setDisagree(0);
        comments.setReport(0);
        comments.setOriginNo(false);
        comments.setGroupOrd(0);
        comments.setArticle(article);
        comments.setAccount(account);
        Comments save = this.commentsRepository.save(comments);
        article.getComments().add(comments);
        account.getComments().add(comments);
        return save;
    }

    public void updateComments(CommentForm commentForm, Comments comments) {
        comments.setDescription(commentForm.getDescription());
    }

    public void deleteComments(Article article, Account account, Comments comments) {
        article.getComments().remove(comments);
        account.getComments().remove(comments);
        this.commentsRepository.delete(comments);
    }

    public CommentsDTO wrappingComments(Comments comments, Article article) {
        CommentsDTO commentsDTO = this.modelMapper.map(comments, CommentsDTO.class);
        commentsDTO.setArticleId(article.getId());
        commentsDTO.setArticleNumber(article.getNumber());
        return commentsDTO;
    }

    @Transactional
    public void updateOpinionCommentVal(Boolean b, Comments comments) {
        if(b) comments.setAgree(comments.getAgree() + 1);
        else comments.setDisagree(comments.getDisagree() + 1);
    }

    @Transactional
    public Comments updateOpinionVal(boolean tf, Comments comments, AccountVO accountVO) {
        if(tf){
            comments.getAgreeList().add(accountVO);
            comments.setAgree(comments.getAgree() + 1);
        }else{
            comments.getDisagreeList().add(accountVO);
            comments.setDisagree(comments.getDisagree() + 1);
        }

        return comments;
    }
}
