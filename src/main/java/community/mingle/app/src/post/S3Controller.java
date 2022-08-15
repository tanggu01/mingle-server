package community.mingle.app.src.post;

import community.mingle.app.config.BaseException;
import community.mingle.app.config.BaseResponse;
import community.mingle.app.utils.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3Controller {
    private final S3Service s3Service;
    /**
     * S3에 파일 업로드
     * @return 성공 시 200 Success와 함께 업로드 된 파일의 파일명 리스트 반환
     */
    @Operation(summary = "9.9 uploadImageTest API", description = "9.9 이미지 업로드 테스트 API")
    @PostMapping("/file")
    public BaseResponse<List<String>> uploadFile(@RequestPart List<MultipartFile> multipartFile) {
        try {
            return new BaseResponse<>(s3Service.uploadFile(multipartFile, "test"));
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }




    /**
     * Amazon S3에 이미지 업로드 된 파일을 삭제
     * @return 성공 시 200 Success
     */

    /*
    @ApiOperation(value = "Amazon S3에 업로드 된 파일을 삭제", notes = "Amazon S3에 업로드된 이미지 삭제")
    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteImage(@ApiParam(value="img 파일 하나 삭제", required = true) @RequestParam String fileName) {
        awsS3Service.deleteImage(fileName);
        return ApiResponse.success(null);
    } */


}
