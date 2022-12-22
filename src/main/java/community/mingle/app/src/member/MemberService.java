package community.mingle.app.src.member;

import community.mingle.app.config.BaseException;
import community.mingle.app.src.auth.AuthRepository;
import community.mingle.app.src.auth.RedisUtil;
import community.mingle.app.src.domain.*;
import community.mingle.app.src.domain.Total.TotalNotification;
import community.mingle.app.src.domain.Total.TotalPost;
import community.mingle.app.src.domain.Univ.UnivComment;
import community.mingle.app.src.domain.Univ.UnivNotification;
import community.mingle.app.src.domain.Univ.UnivPost;
import community.mingle.app.src.domain.Total.TotalComment;
import community.mingle.app.src.member.model.NotificationDTO;
import community.mingle.app.src.member.model.NotificationRequest;
import community.mingle.app.src.member.model.ReportDTO;
import community.mingle.app.src.member.model.ReportRequest;
import community.mingle.app.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static community.mingle.app.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final JwtService jwtService;
    private final AuthRepository authRepository;
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;


    /**
     * 토큰에서 대학 추출
     */
    public UnivName findUniv() throws BaseException {
        Member member;
        Long memberIdByJwt = jwtService.getUserIdx();
        member = memberRepository.findMember(memberIdByJwt);
        if (member == null) {
            throw new BaseException(USER_NOT_EXIST);
        }
        return member.getUniv();
    }



    /**
     * 2.1 닉네임 수정
     */
    @Transactional
    public void modifyNickname(String nickname) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        Member member = memberRepository.findMember(userIdByJwt);

        if (authRepository.findNickname(nickname)) {
            throw new BaseException(USER_EXISTS_NICKNAME);
        }
        try {
            member.modifyNickname(nickname);
        } catch (Exception e) {
            throw new BaseException(MODIFY_FAIL_NICKNAME);
        }
    }



    /**
     * 2.2 내가 쓴 글 조회
     */
    public List<TotalPost> getTotalPosts(Long postId) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        try {
            List<TotalPost> posts = memberRepository.findTotalPosts(userIdByJwt, postId);
            return posts;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 2.3
     */
    public List<UnivPost> getUnivPosts(Long postId) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        try {
            List<UnivPost> posts = memberRepository.findUnivPosts(userIdByJwt, postId);
            return posts;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 2.4 내가 쓴 댓글 조회
     */
    public List<TotalPost> getTotalComments(Long postId) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        try {
            List<TotalPost> comments = memberRepository.findTotalComments(userIdByJwt, postId);
            return comments;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 2.5
     */
    public List<UnivPost> getUnivComments(Long postId) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        try {
            List<UnivPost> comments = memberRepository.findUnivComments(userIdByJwt, postId);
            return comments;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 2.6 univ 스크랩
     */
    public List<UnivPost> getUnivScraps(Long postId) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        Member member = memberRepository.findMember(userIdByJwt);

        try {
            List<UnivPost> scraps = memberRepository.findUnivScraps(member.getId(), postId);
            return scraps;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 2.7 전체 스크랩
     */
    public List<TotalPost> getTotalScraps(Long postId) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        Member member = memberRepository.findMember(userIdByJwt);
        try {
            List<TotalPost> scraps = memberRepository.findTotalScraps(member.getId(), postId);
            return scraps;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 2.8 잔체 좋아요 게시물
     */
    public List<TotalPost> getTotalLikes(Long postId) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        Member member = memberRepository.findMember(userIdByJwt);
        try {
            List<TotalPost> likes = memberRepository.findTotalLikes(member.getId(), postId);
            return likes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }



    /**
     * 2.9 학교 좋아요 게시물
     */
    public List<UnivPost> getUnivLikes(Long postId) throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        Member member = memberRepository.findMember(userIdByJwt);
        try {
            List<UnivPost> likes = memberRepository.findUnivLikes(member.getId(), postId);
            return likes;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 2.10 유저 삭제
     */
    @Transactional
    public void  deleteMember() throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        Member member;
        member = memberRepository.findMember(userIdByJwt);
        if (member == null) {
            throw new BaseException(USER_NOT_EXIST);
        }

        try {
            member.deleteMember();

        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 2.11 report API
     */
    @Transactional
    public Member findReportedMember(ReportRequest reportRequest) throws BaseException {
        Member reportedMember = null;
        //나중에 case문으로 바꿀 수 있는지 확인
        try {
            if (reportRequest.getTableId() == TableType.TotalPost) {
                reportedMember = memberRepository.findReportedTotalPostMember(reportRequest.getContentId());
            } else if (reportRequest.getTableId() == TableType.TotalComment) {
                reportedMember = memberRepository.findReportedTotalCommentMember(reportRequest.getContentId());
            } else if (reportRequest.getTableId() == TableType.UnivPost) {
                reportedMember = memberRepository.findReportedUnivPostMember(reportRequest.getContentId());
            } else if (reportRequest.getTableId() == TableType.UnivComment) {
                reportedMember = memberRepository.findReportedUnivCommentMember(reportRequest.getContentId());
            }
            return reportedMember;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    //신고 추가 메소드
    public ReportDTO createReport(ReportRequest reportRequest, Member reportedMember) throws BaseException {
        //신고당한 사람 (신고당한 컨텐츠를 작성한 사람)의 memberId를 가져옴
        Long reportedMemberId = reportedMember.getId();
        //신고한 사람의 memberId를 가져옴 by jwt
        Long reporterMemberId = jwtService.getUserIdx();
        //신고한 사람이 이미 해당 컨텐츠를 한 번 신고한 적 있는지 validation을 해 줌
//        if (memberRepository.isMultipleReport(reportRequest, reporterMemberId) == true) {
//            throw new BaseException(ALREADY_REPORTED);
//        }

        try {
            //신고 엔티티의 createReport를 통해 report생성 후 DB에 저장
            Report report = Report.createReport(reportRequest.getTableId(), reportRequest.getContentId(), reportedMemberId, reporterMemberId);
            Long reportId = memberRepository.reportSave(report);
            //reportDTO에 reportId를 담아서 반환해 줌 (신고가 잘 저장됐다는 뜻)
            ReportDTO reportDTO = new ReportDTO(reportId);
            return reportDTO;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
    @Transactional
    //신고 10회 이상 누적된 멤버 삭제 메소드
    public void checkReportedMember(Member member) throws BaseException{
        try {
            //신고 테이블에서 신고 당한 맴버가 몇 번이 있는지를 count한 후
            Long memberCount = memberRepository.countMemberReport(member.getId());
            //10번일 시 member의 status를 REPORTED로 변환
            if (memberCount % 10 == 0) {
                member.modifyReportStatus();
            }
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }

    }
    @Transactional
    //신고 3회 이상 누적된 멤버 삭제 메소드
    public void checkReportedPost(ReportRequest reportRequest) throws BaseException{
        try {
            //신고 테이블에서 이번에 신고된 컨텐츠와 같은 tableId와 contentId를 가지고 있는 컨텐츠를 count한 후 3번 이상일 시
            Long contentCount = memberRepository.countContentReport(reportRequest);
            if (contentCount == 3) {
                //total post
                if (reportRequest.getTableId() == TableType.TotalPost) {
                    //신고 된 total post 찾음
                    TotalPost reportedTotalPost = memberRepository.findReportedTotalPost(reportRequest.getContentId());
                    //해당 total post에 딸린 total comments들도 찾음
                    int reportedTotalComments = memberRepository.findReportedTotalCommentsByPostId(reportRequest.getContentId());
                    //total post는 REPORTED status로 total comments는 INACTIVE status로 만들어 줌
                    reportedTotalPost.modifyReportStatus();
//                for (TotalComment tc : reportedTotalComments) {
//                    tc.modifyInactiveStatus();
//                }
                }

                //total comment
                else if (reportRequest.getTableId() == TableType.TotalComment) {
                    //신고 된 total comment를 찾음
                    TotalComment reportedTotalComment = memberRepository.findReportedTotalCommentByCommentId(reportRequest.getContentId());
                    //해당 댓글을 REPORTED status로 만들어 줌
                    reportedTotalComment.modifyReportStatus();
                }

                //univ post
                else if (reportRequest.getTableId() == TableType.UnivPost) {
                    //신고 된 univ post를 찾음
                    UnivPost reportedUnivPost = memberRepository.findReportedUnivPost(reportRequest.getContentId());
                    //해당 univ post에 딸린 univ comments들도 찾음
                    int reportedUnivComments = memberRepository.findReportedUnivCommentsByPostId(reportRequest.getContentId());
                    //univ post는 REPORTED status로 univ comments는 INACTIVE status로 만들어 줌
                    reportedUnivPost.modifyReportStatus();
//                for (UnivComment uc : reportedUnivComments) {
//                    uc.modifyInactiveStatus();
//                }
                }

                //univ comment
                else if (reportRequest.getTableId() == TableType.UnivComment) {
                    //신고 된 univ comment를 찾음
                    UnivComment reportedUnivComment = memberRepository.findReportedUnivCommentByCommentId(reportRequest.getContentId());
                    //해당 댓글을 REPORTED status로 만들어 줌
                    reportedUnivComment.modifyReportStatus();
                }
            }
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }

    }


    /**
     * 2.12 알림 리스트 API
     */
    public List<TotalNotification> getTotalNotifications() throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        try {
            List<TotalNotification> notificationDTO = memberRepository.getTotalNotification(userIdByJwt);
            return notificationDTO;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    public List<UnivNotification> getUnivNotifications() throws BaseException {
        Long userIdByJwt = jwtService.getUserIdx();
        try {
            List<UnivNotification> notificationDTO = memberRepository.getUnivNotification(userIdByJwt);
            return notificationDTO;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 2.13 알림 읽기 API
     */
    @Transactional
    public void  readNotification(NotificationRequest notificationRequest) throws BaseException {
        try {
            if (notificationRequest.getBoardType().equals(BoardType.광장)){
                TotalNotification totalNotification;
                totalNotification = memberRepository.findTotalNotification(notificationRequest.getNotificationId());
                totalNotification.readNotification();
            }
            else if (notificationRequest.getBoardType().equals(BoardType.잔디밭)) {
                UnivNotification univNotification;
                univNotification = memberRepository.findUnivNotification(notificationRequest.getNotificationId());
               univNotification.readNotification();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }



    public List<NotificationDTO> sortNotifications(List<NotificationDTO> final_result) {
        Collections.sort(final_result, new NotificationDTOComparator().reversed());
        if (final_result.size() <= 20) {
            return final_result;
        } else {
            final_result.subList(0, 20);
        }
        return final_result;
    }





    /**
     * 2.14 로그아웃 api
     */
    public void logout() throws BaseException {
        Long userIdx = jwtService.getUserIdx();
        Member member = authRepository.findMemberById(userIdx);
        try {
            redisUtil.deleteData(member.getEmail());
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
