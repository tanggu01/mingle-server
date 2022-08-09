package community.mingle.app.src.post.model;

import community.mingle.app.src.domain.Total.TotalPost;
import lombok.Getter;

import java.util.List;

import static community.mingle.app.config.DateTimeConverter.convertLocaldatetimeToTime;

@Getter
public class TotalPostDto {

    private String title;
    private String content;
    private String nickname;
    private int likeCount;
    private int scrapCount;
    private int commentCount;
    private String createdAt;

    private boolean isMyPost;
    private boolean isLiked;
    private boolean isScraped;

    public TotalPostDto(TotalPost totalPost, boolean isMyPost, boolean isLiked, boolean isScraped) {
        this.title = totalPost.getTitle();
        this.content = totalPost.getContent();
        if (totalPost.isAnonymous() == true) {
            this.nickname = "글쓴이";
        } else{
            this.nickname = totalPost.getMember().getNickname();
        }
        this.likeCount = totalPost.getTotalPostLikes().size();
        this.scrapCount = totalPost.getTotalPostScraps().size();
        this.createdAt = convertLocaldatetimeToTime(totalPost.getCreatedAt());
        this.commentCount = totalPost.getTotalPostComments().size();
        this.isMyPost = isMyPost;
        this.isLiked = isLiked;
        this.isScraped = isScraped;
    }
}