package dev.appkr;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PresignedUrlUploadRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;

public class App {

  static final String AWS_ACCESS_KEY = "FILL VALUE";
  static final String AWS_ACCESS_SECRET_KEY = "FILL VALUE";

  static final String bucket = "meshdev-vroong-private";
  static final String objectKey = "dev1/neogeo/ECAMPRO_II_PLUS_DOCKER.tar.gz";

  static final File fileToUpload = Path.of("/path/to/ECAMPRO_II_PLUS_DOCKER.tar.gz").toFile();

  static final AWSCredentialsProvider awsCredentialsProvider =
      new AWSStaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_ACCESS_SECRET_KEY));
  static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
      .withCredentials(awsCredentialsProvider)
      .withRegion(Regions.AP_NORTHEAST_2)
      .build();

  public static void main(String[] args) {
    // AWSS3 presignedUrl 구하기
    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60;
    expiration.setTime(expTimeMillis);

    final GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest(bucket, objectKey)
          .withMethod(HttpMethod.PUT)
          .withExpiration(expiration);

    final URL presignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    System.out.println("AWSS3 presignedUrl=" + presignedUrl);

    // presignedUrl을 이용하여 파일 업로드 하기
    try {
      final PresignedUrlUploadRequest presignedUrlUploadRequest =
          new PresignedUrlUploadRequest(presignedUrl)
              .withFile(fileToUpload);

      System.out.println("파일 업로드 중...");
      s3Client.upload(presignedUrlUploadRequest);
    } finally {
      System.out.println("파일 업로드 완료");
    }

    // 업로드한 파일 정보 조회
    final S3Object s3Object = s3Client.getObject(bucket, objectKey);
    System.out.println(s3Object);
    System.out.println(s3Object.getObjectMetadata().getRawMetadata());
  }
}
