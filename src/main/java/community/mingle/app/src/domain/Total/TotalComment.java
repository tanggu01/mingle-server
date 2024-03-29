package community.mingle.app.src.domain.Total;

import community.mingle.app.src.domain.Member;
import community.mingle.app.src.domain.PostStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="total_comment")

public class TotalComment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "totalcomment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "totalpost_id")
    private TotalPost totalPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String content;


    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "mention_id")
    private Long mentionId;

    /**
     * 양방향
     */
    @OneToMany(mappedBy = "totalComment")
    private List<TotalCommentLike> totalCommentLikes = new ArrayList<>();

    /**알림 */
    @OneToMany(mappedBy = "totalComment")
    private List<TotalNotification> totalNotifications = new ArrayList<>();


    /** 익명방법? */
    @Column(name = "is_anonymous")
    private boolean isAnonymous;

    @Column(name = "anonymous_id")
    private Long anonymousId;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum")
    private PostStatus status;


    //== 생성 메서드 ==// -> constructor 역할.
    public static TotalComment createComment(TotalPost totalPost, Member member, String content, Long parentCommentId, Long mentionId, boolean isAnonymous, Long anonymousId) {
        TotalComment totalComment = new TotalComment();
        totalComment.setTotalPost(totalPost);
        totalComment.setMember(member);
        totalComment.setContent(content);
        totalComment.setParentCommentId(parentCommentId);
        totalComment.setMentionId(mentionId);
        totalComment.setAnonymous(isAnonymous);
        totalComment.setAnonymousId(anonymousId);
        totalComment.createdAt = LocalDateTime.now();
        totalComment.updatedAt = LocalDateTime.now();
        totalComment.status = PostStatus.ACTIVE;

        return totalComment;
    }


    public void deleteTotalComment (){
        this.deletedAt = LocalDateTime.now();
        this.status = PostStatus.INACTIVE;
    }

    public void modifyStatusAsReported() {
        this.status = PostStatus.REPORTED;
    }

    public void modifyStatusAsNotified() {
        this.status = PostStatus.NOTIFIED;
    }

    public void modifyStatusAsDeleted() {
        this.status = PostStatus.DELETED;
    }

    public void modifyInactiveStatus() {
        this.status = PostStatus.INACTIVE;
    }



}
