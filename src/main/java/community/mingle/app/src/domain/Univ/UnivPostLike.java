package community.mingle.app.src.domain.Univ;

import community.mingle.app.src.domain.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="univ_post_like")

public class UnivPostLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "univpost_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "univpost_id")
    private UnivPost univPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static UnivPostLike likesUnivPost(UnivPost univPost, Member member) {
        UnivPostLike univPostLike = new UnivPostLike();
        univPostLike.setMember(member);
        univPostLike.setUnivPost(univPost);
        univPostLike.createdAt = LocalDateTime.now();
        return univPostLike;


    }


}
