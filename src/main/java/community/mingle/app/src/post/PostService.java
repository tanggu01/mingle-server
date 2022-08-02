package community.mingle.app.src.post;

import community.mingle.app.src.domain.Banner;
import community.mingle.app.src.domain.Category;
import community.mingle.app.src.domain.Univ.UnivComment;
import community.mingle.app.src.domain.Univ.UnivPost;
import community.mingle.app.src.post.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import community.mingle.app.config.BaseException;
import community.mingle.app.src.domain.Member;
import community.mingle.app.src.domain.Total.TotalPost;
import community.mingle.app.utils.JwtService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static community.mingle.app.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final JwtService jwtService;
    private final PostRepository postRepository;


    /**
     * 3.1 광고 배너 API
     */
    public List<Banner> findBanner() throws BaseException{
        try{
            List<Banner> banner = postRepository.findBanner();
            return banner;
        }catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 3.2 홍콩 배스트 게시판 API
     */
    public List<TotalPost> findTotalPostWithMemberLikeComment() throws BaseException{
        List<TotalPost> totalPosts = postRepository.findTotalPostWithMemberLikeComment();
        if (totalPosts.size() == 0) {
            throw new BaseException(EMPTY_BEST_POSTS);
        }
        return totalPosts;
    }

    /**
     * 3.3 학교 베스트 게시판 API --> try-catch 설명
     */
    public List<UnivPost> findAllWithMemberLikeCommentCount() throws BaseException {
//       try { //null 이 나오기 전에 쿼리문에서 에러가 남.
        Long memberIdByJwt = jwtService.getUserIdx();  // jwtService 의 메소드 안에서 throw 해줌 -> controller 로 넘어감
//        } catch (Exception e) {
//            throw new BaseException(EMPTY_JWT);
//        }
        Member member;
//        try {
        member = postRepository.findMemberbyId(memberIdByJwt);
        if (member == null) {
            throw new BaseException(DATABASE_ERROR); //무조건 찾아야하는데 못찾을경우 (이미 jwt 에서 검증이 되기때문)
        }
//        } catch (Exception e) {
//            throw new BaseException(DATABASE_ERROR); //무조건 찾아야하는데 못찾을경우 (이미 jwt 에서 검증이 되기때문)
//        }

//        try {
        List<UnivPost> univPosts = postRepository.findAllWithMemberLikeCommentCount(member);
        if (univPosts.size() == 0) {
            throw new BaseException(EMPTY_BEST_POSTS);
        }
        return univPosts;
//        } catch (Exception e) {
//            throw new BaseException(EMPTY_BEST_POSTS);
//        }
    }

    /**
     * 3.4 광장 게시판 리스트 API
     */
    public List<TotalPost> findTotalPost(int category) throws BaseException{
        List<TotalPost> getAll = postRepository.findTotalPost(category);
        if (getAll.size() == 0) {
            throw new BaseException(EMPTY_POSTS_LIST);
        }
        return getAll;
    }


    /**
     * 3.5 게시물 작성 API
     */
    @Transactional
    public PostCreateResponse createPost (PostCreateRequest postCreateRequest) throws BaseException{
        Member member;
        Category category;
        Long memberIdByJwt = jwtService.getUserIdx();
        member = postRepository.findMemberbyId(memberIdByJwt);
        if (member == null) {
            throw new BaseException(USER_NOT_EXIST);
        }
        try {
            category = postRepository.findCategoryById(postCreateRequest.getCategoryId());
        } catch(Exception exception){
            throw new BaseException(INVALID_POST_CATEGORY);
        }
        try {
            UnivPost univPost = UnivPost.createPost(member, category, postCreateRequest);
            Long id = postRepository.save(univPost);
            return new PostCreateResponse(id);
        } catch (Exception e) {
            throw new BaseException(CREATE_FAIL_POST);
        }
    }



    /**
     * 3.10.1 학교 게시물 상세 - 게시물 API
     */
    @Transactional(readOnly = true)
    public UnivPost getUnivPost(Long postId) throws BaseException {
        Long memberIdByJwt = jwtService.getUserIdx();  // jwtService 의 메소드 안에서 throw 해줌 -> controller 로 넘어감
        Member member;
        member = postRepository.findMemberbyId(memberIdByJwt);

        try {
            UnivPost univPost = postRepository.getUnivPostById(postId);
            boolean checkLiked = postRepository.checkIsLiked(postId, memberIdByJwt);
            boolean checkScraped = postRepository.checkIsScraped(postId, memberIdByJwt);
            return univPost;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }

    }

    /**
     * 3.10.2 학교 게시물 상세 - 댓글 API
     */
    @Transactional(readOnly = true)
    public List<UnivCommentDTO> getUnivComments(Long postId) throws BaseException {

        Long memberIdByJwt = jwtService.getUserIdx();  // jwtService 의 메소드 안에서 throw 해줌 -> controller 로 넘어감
        Member member;
        member = postRepository.findMemberbyId(memberIdByJwt);

        try {
            //1. postId 의 댓글, 대댓글 리스트 각각 가져오기
            List<UnivComment> univComments = postRepository.getUnivComments(postId); //댓글
            List<UnivComment> univCoComments = postRepository.getUnivCoComments(postId); //대댓글

            //2. 댓글 + 대댓글 DTO 생성
            List<UnivCommentDTO> univCommentDTOList = new ArrayList<>();

            //3. 댓글 리스트 돌면서 댓글 하나당 대댓글 리스트 넣어서 합쳐주기
            for (UnivComment c : univComments) {
                //parentComment 하나당 해당하는 UnivComment 타입의 대댓글 찾아서 리스트 만들기
                List<UnivComment> CoCommentList = univCoComments.stream()
                        .filter(cc -> c.getId().equals(cc.getParentCommentId()))
                        .collect(Collectors.toList());

//                postRepository.checkCoCommentLiked(CoCommentList); 성능 저하. for문 돌때마다 쿼리문 나감

                //댓글 하나당 만들어진 대댓글 리스트를 대댓글 DTO 형태로 변환
                List<UnivCoCommentDTO> coCommentDTO = CoCommentList.stream()
                        .map(cc -> new UnivCoCommentDTO(c, cc, memberIdByJwt))
                        .collect(Collectors.toList());

                /** 쿼리문 나감. 결론: for 문 안에서 쿼리문 대신 DTO 안에서 해결 */
                //boolean isLiked = postRepository.checkCommentIsLiked(c.getId(), memberIdByJwt);

                //4. 댓글 DTO 생성 후 최종 DTOList 에 넣어주기
                UnivCommentDTO univCommentDTO = new UnivCommentDTO(c, coCommentDTO, memberIdByJwt);
                univCommentDTOList.add(univCommentDTO);
            }
            return univCommentDTOList;

        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 3.10 학교 게시물 상세 (Old)
     */
//    @Transactional(readOnly = true)
//    public UnivPost findUnivPost(Long univPostId) throws BaseException {
//
//        Long memberIdByJwt = jwtService.getUserIdx();  // jwtService 의 메소드 안에서 throw 해줌 -> controller 로 넘어감
//        Member member;
//        member = postRepository.findMemberbyId(memberIdByJwt);
//        if (member == null) {
//            throw new BaseException(DATABASE_ERROR); //무조건 찾아야하는데 못찾을경우 (이미 jwt 에서 검증이 되기때문)
//        }
//        UnivPost univPost = postRepository.findUnivPost(univPostId);
//
////        List<UnivComment> univComments = univPost.getComments();
////
////        List<GetUnivCommentsRes> comments = univComments.stream()
////                .map(p -> new GetUnivCommentsRes(p))
////                .collect(Collectors.toList());
//        return univPost;
//    }
//
//    @Transactional(readOnly = true)
//    public List<GetUnivCommentsRes> findUnivCoComment(UnivPost univPost) {
//
//        List<GetUnivCommentsRes> commentsList = new ArrayList<>();
////        List<UnivComment> nullCommentsList = new ArrayList<>(); // 1번
////        List<UnivComment> comments = univPost.getComments();
//        List<UnivComment> comments = postRepository.findUnivComment(univPost.getId()); //null 도 포함
//
////        for (UnivComment comment : comments) {
////            if (comment.getParentCommentId() == null) {
////                nullCommentsList.add(comment);
////            }
////        }
////        List<UnivComment> nullCommentList = comments.stream()
////                .filter(obj -> obj.getParentCommentId()==null)
////                .collect(Collectors.toList());
//
////        List<UnivComment> nullComments = postRepository.findNullComments(comments); //2번
//
//        List<UnivComment> coComments; // 대댓글 리스트 : null 일수도 있지.
//        for (UnivComment univComment : comments) {
//            coComments = postRepository.findUnivCoComment(univPost, univComment); //댓글 하나마다 대댓글 리스트 찾음.
//            List<GetCoCommentsRes> coCommentsRes = coComments.stream()
//                    .map(cc -> new GetCoCommentsRes(cc))
//                    .collect(Collectors.toList());
//            GetUnivCommentsRes commentsRes = new GetUnivCommentsRes(univComment, coCommentsRes);
//            commentsList.add(commentsRes);
//        }
//        return commentsList;
////        List<GetCoCommentsRes> coCommentsRes = coComments.stream()
////        .map(cc -> new GetCoCommentsRes(cc))
////        .collect(Collectors.toList());
////        return commentsRes;
////        UnivComment univComment = postRepository.findUnivCoComment(univPost.getComments());
//    }
}
