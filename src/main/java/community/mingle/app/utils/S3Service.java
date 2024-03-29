package community.mingle.app.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import community.mingle.app.config.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import community.mingle.app.config.BaseException;
import static community.mingle.app.config.BaseResponseStatus.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static community.mingle.app.config.BaseResponseStatus.DATABASE_ERROR;
import static community.mingle.app.config.BaseResponseStatus.UPLOAD_FAIL_IMAGE;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;  // S3 버킷 이름

    public static final String cloudFrontDomain = "https://d2xbxo9g2f57e.cloudfront.net/";
    public List<String> uploadFile(List<MultipartFile> multipartFile, String dirName) throws BaseException {
        List<String> fileNameList = new ArrayList<>();

        long count = multipartFile.stream().filter(t -> t.getSize() > 0).count();
        if (count > 5) {
            throw new BaseException(INVALID_IMAGE_NUMBER);
        }
        // multipartFile로 넘어온 파일들 fileNameList에 추가
        for (MultipartFile file : multipartFile) {
            String fileName = dirName + "/" + createFileName(file.getOriginalFilename());
            System.out.println("파일을 보자" + file);
            System.out.println("파일파일" + file.getContentType());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType("image");

            try(InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
                //fileNameList.add(amazonS3.getUrl(bucket, fileName).toString());
                fileNameList.add(cloudFrontDomain+fileName);
            } catch(Exception e) {
                e.printStackTrace();
                throw new BaseException(UPLOAD_FAIL_IMAGE);
            }
        }

        return fileNameList;
    }

    public void deleteFile(String fileName, String dirName) throws BaseException {
        String fileRename = dirName + "/" + fileName; //key
        try{
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileRename));
        } catch (AmazonClientException e) {
            e.printStackTrace();
            throw new BaseException(DELETE_FAIL_IMAGE);
        }
    }


    private String createFileName(String fileName) throws BaseException{ // 먼저 파일 업로드 시, 파일명을 난수화
        try{
            return UUID.randomUUID().toString().concat(getFileExtension(fileName));
        }catch(Exception e) {
            e.printStackTrace();
            throw new BaseException(INVALID_IMAGE);
        }

    }

    private String getFileExtension(String fileName) throws BaseException{ // file 형식이 잘못된 경우를 확인하기 위해 만들어진 로직이며, 파일 타입과 상관없이 업로드할 수 있게 하기 위해 .의 존재 유무만 판단하였습니다.
        try {
            if (fileName.length() == 0) {
                throw new BaseException(INVALID_IMAGE_FORMAT);
            }
            ArrayList<String> fileValidate = new ArrayList<>();
            fileValidate.add(".jpg");
            fileValidate.add(".jpeg");
            fileValidate.add(".png");
            fileValidate.add(".JPG");
            fileValidate.add(".JPEG");
            fileValidate.add(".PNG");
            fileValidate.add(".heic");
            fileValidate.add(".HEIC");
            fileValidate.add(".webp");
            String idxFileName = fileName.substring(fileName.lastIndexOf("."));
            if (!fileValidate.contains(idxFileName)) {
                throw new BaseException(INVALID_IMAGE_FORMAT);
            }
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(INVALID_IMAGE_FORMAT);
        }
    }
}
