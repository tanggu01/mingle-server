package community.mingle.app.src.comment.model;

import community.mingle.app.src.domain.Univ.UnivComment;
import lombok.Getter;

import static community.mingle.app.config.DateTimeConverter.convertToDateAndTime;

@Getter
public class PostUnivCommentResponse {

    Long commentId;
    String nickname;
    String createdAt;

    public PostUnivCommentResponse(Long anonymousId, UnivComment univComment) {
        this.commentId = univComment.getId();
        if (anonymousId == 0) {
            this.nickname = univComment.getMember().getNickname();
        } else{
            this.nickname = "익명 " + anonymousId;
        }
        createdAt = convertToDateAndTime(univComment.getCreatedAt());


    }
}
