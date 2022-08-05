package community.mingle.app.src.post.model;

import community.mingle.app.src.domain.Univ.UnivComment;
import community.mingle.app.src.domain.Univ.UnivCommentLike;
import lombok.Getter;

import static community.mingle.app.config.DateTimeConverter.convertLocaldatetimeToTime;

@Getter
public class UnivCoCommentDTO {

    private Long commentId;
    private Long parentCommentId;
    private String nickname;
    private String mention; //멘션 추가
    private String content;
    private int likeCount;
    private boolean isLiked;
    private boolean isMyComment;
    private String createdTime;

    public UnivCoCommentDTO(UnivComment parent, UnivComment cc, Long memberId) {

//        boolean contains = cc.getUnivCommentLikes()

        this.commentId = cc.getId();
        this.parentCommentId = cc.getParentCommentId();

//        this.nickname = cc.getMember().getNickname(); //jwt userIdx 로 멤버 찾음
        if (cc.isAnonymous() == true) {
            this.nickname = "익명 "+cc.getAnonymousId();
        } else{
            this.nickname = cc.getMember().getNickname();
        }

        if (parent.isAnonymous() == true) {
            this.mention = "익명 "+parent.getAnonymousId();
        }else{
            this.mention = parent.getMember().getNickname();
        }
        this.content = cc.getContent();
        this.likeCount = cc.getUnivCommentLikes().size();

        for (UnivCommentLike ucl : cc.getUnivCommentLikes()) { //영속성
            if (ucl.getMember().getId() == memberId) { //배치사이즈?
                isLiked = true;
                break;
            } else {
                isLiked = false;
            }
        }

        if (cc.getMember().getId() == memberId) {
            isMyComment = true;
        }
        this.createdTime = convertLocaldatetimeToTime(cc.getCreatedAt());

    }

}
