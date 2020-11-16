package org.kuroneko.restapiproject.article.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTOByMainPage {

//    article - Number, division, title, writer(account), date, 해당 article을 호출 하기 위한 Id 값
//    comment - 해당 article에 속해있는 comment의 총 갯수

    private Long id;

    private Long number;

    private ArticleThema division;

    private String title;

    private Long accountId;

    private String accountUsername;

    private LocalDateTime createTime;

    private int commentCnt;

    private String communityName;

}
