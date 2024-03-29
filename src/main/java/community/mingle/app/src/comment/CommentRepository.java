package community.mingle.app.src.comment;

import community.mingle.app.config.BaseException;
import community.mingle.app.config.BaseResponseStatus;
import community.mingle.app.src.domain.ItemNotification;
import community.mingle.app.src.domain.Member;
import community.mingle.app.src.domain.Total.*;
import community.mingle.app.src.domain.Univ.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.*;

import static community.mingle.app.config.BaseResponseStatus.DATABASE_ERROR;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final EntityManager em;


    public Member findMemberbyId(Long id) {
        return em.find(Member.class, id);
    }


    public TotalPost findTotalPostbyId(Long postId) {
        return em.find(TotalPost.class, postId);
    }


    public UnivPost findUnivPostById(Long postId) {
        return em.find(UnivPost.class , postId);
    }



    /**
     * 4.1 익명 몇 인지 찾기 (anonymousId)
     */
    public Long findTotalAnonymousId(TotalPost post, Long memberIdByJwt ) throws BaseException {
        Long newAnonymousId;


        /**
         * case 1: 해당 게시글 커멘츠가 멤버가 있는지 없는지 확인하고 있으면 그 전 id 부여
         */
        List<TotalComment> totalCommentsByMember = em.createQuery("select tc from TotalComment tc join fetch tc.totalPost as tp join fetch tc.member as m where tp.id = :postId and m.id = :memberId and tc.isAnonymous = true", TotalComment.class)
                .setParameter("postId", post.getId())
                .setParameter("memberId", memberIdByJwt)
                .getResultList();
        if (totalCommentsByMember.size() != 0) { //있으면 list 첫번째 element 반환. 중복은 없을테니
             return totalCommentsByMember.get(0).getAnonymousId();
        }

        /**
         * case 2: 댓글 단 이력이 없고 익명 댓글을 달고싶을때: anonymousId 부여받음
         */
        List<TotalComment> totalComments = post.getTotalPostComments();
        TotalComment totalCommentWithMaxAnonymousId;

        try {  //게시물에서 제일 큰 id를 찾은 후 +1 한 id 를 내 댓글에 새로운 anonymousId 로 부여
            // 닉네임으로 단 사람만 있을 경우에도 그냥 0+1 을 해줘서 1을 부여해줌
            totalCommentWithMaxAnonymousId = totalComments.stream()
                    .max(Comparator.comparingLong(TotalComment::getAnonymousId))//nullPointerException
                    .get();
            newAnonymousId = totalCommentWithMaxAnonymousId.getAnonymousId() + 1;
            return newAnonymousId;

        } catch (NoSuchElementException e) {  //게시물에 기존 익명 id 가 아예 없을때: id 로 1 부여 --> 댓글이 아예 없을때
            newAnonymousId = Long.valueOf(1);
        }
        return newAnonymousId;
    }


    public void saveTotalComment (TotalComment comment) {
        em.persist(comment);
    }



    /**
     * 4.2
     */
    public Long findUnivAnonymousId(UnivPost univPost, Long memberIdByJwt) {
        Long newAnonymousId;

        List<UnivComment> univCommentsByMember = em.createQuery("select uc from UnivComment uc " +
                        "join fetch uc.univPost as p join fetch uc.member as m " +
                        "where p.id = :postId and m.id = :memberId and uc.isAnonymous = true", UnivComment.class)
                .setParameter("postId", univPost.getId())
                .setParameter("memberId", memberIdByJwt)
                .getResultList();
        if (univCommentsByMember.size() != 0) {
           newAnonymousId = univCommentsByMember.get(0).getAnonymousId();
           return newAnonymousId;
        }

        List<UnivComment> univComments = univPost.getUnivComments();
        UnivComment univCommentWithMaxAnonymousId;

        try {
            univCommentWithMaxAnonymousId = univComments.stream()
                    .max(Comparator.comparingLong(UnivComment::getAnonymousId))//NullPointerException
                    .get(); //NoSuchElementException
            newAnonymousId = univCommentWithMaxAnonymousId.getAnonymousId() + 1;
            return newAnonymousId;

//        } catch (NullPointerException e) {
//            newAnonymousId = Long.valueOf(1);
//            return newAnonymousId;
        } catch (NoSuchElementException e) {
            newAnonymousId = Long.valueOf(1);
        }
        return newAnonymousId;
    }

    public void saveUnivComment(UnivComment univComment) {
        em.persist(univComment);
//        return univComment;
    }


    public TotalComment findTotalCommentById(Long id) {
        return em.find(TotalComment.class, id);
    }

    public UnivComment findUnivCommentById(Long id) {
        return em.find(UnivComment.class, id);
    }

    public Long save(TotalCommentLike totalCommentLike) {
        em.persist(totalCommentLike);
        return totalCommentLike.getId();
    }

    public Long save(UnivCommentLike univCommentLike) {
        em.persist(univCommentLike);
        return univCommentLike.getId();
    }


    public void  deleteLikeTotal(Long commentId, Long memberId) {
//        TotalCommentLike findComment = em.find(TotalCommentLike.class, commentIdx);
        TotalCommentLike findComment = em.createQuery("select tcp from TotalCommentLike tcp where tcp.totalComment.id =:commentId and tcp.member.id =:memberId", TotalCommentLike.class)
                        .setParameter("commentId", commentId)
                        .setParameter("memberId", memberId)
                        .getSingleResult();
        em.remove(findComment);

    }

    public void deleteLikeUniv(Long commentId, Long memberId) {
//        UnivCommentLike findComment = em.find(UnivCommentLike.class, commentIdx);
        UnivCommentLike findComment = em.createQuery("select ucp from UnivCommentLike ucp where ucp.univComment.id =:commentId and ucp.member.id =:memberId", UnivCommentLike.class)
                    .setParameter("commentId", commentId)
                    .setParameter("memberId", memberId)
                    .getSingleResult();
        em.remove(findComment);

    }

    public void deleteUnivNotification(Long univNotificationId) {
        UnivNotification univNotification = em.find(UnivNotification.class, univNotificationId);
        em.remove(univNotification);
    }


    public void deleteTotalNotification(Long totalNotificationId) {
        TotalNotification totalNotification = em.find(TotalNotification.class, totalNotificationId);
        em.remove(totalNotification);
    }
}
