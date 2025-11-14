package cn.aspes.agri.trade.service.impl;

import cn.aspes.agri.trade.config.AliyunOssConfig;
import cn.aspes.agri.trade.exception.BusinessException;
import cn.aspes.agri.trade.service.FileUploadService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件上传服务实现（使用阿里云OSS）
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    private final AliyunOssConfig aliyunOssConfig;
    private final OSS ossClient;
    
    public FileUploadServiceImpl(AliyunOssConfig aliyunOssConfig, @Autowired(required = false) OSS ossClient) {
        this.aliyunOssConfig = aliyunOssConfig;
        this.ossClient = ossClient;
    }
    
    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        try {
            if (ossClient == null) {
                throw new BusinessException("OSS客户端未配置，请检查阿里云配置");
            }
            
            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = aliyunOssConfig.getBasePath() + "/" + bucketName + "/" + 
                              LocalDate.now() + "/" + fileName;
            
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        aliyunOssConfig.getBucketName(),
                        objectName,
                        inputStream
                );
                putObjectRequest.setMetadata(null);
                ossClient.putObject(putObjectRequest);
            }
            
            // 拼接完整的文件访问URL
            String url = aliyunOssConfig.getEndpoint().replace("https://", "https://" + 
                        aliyunOssConfig.getBucketName() + ".");
            return url + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String uploadProductImage(MultipartFile file) {
        return uploadFile(file, "product-images");
    }
    
    @Override
    public String uploadContractSign(MultipartFile file) {
        return uploadFile(file, "contract-signs");
    }
    
    @Override
    public String uploadPaymentVoucher(MultipartFile file) {
        return uploadFile(file, "payment-vouchers");
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL解析出objectName
            // URL格式: https://bucket.endpoint/basePath/bucketName/date/fileName
            String objectName = extractObjectName(fileUrl);
            if (objectName != null && !objectName.isEmpty()) {
                ossClient.deleteObject(aliyunOssConfig.getBucketName(), objectName);
                log.info("文件删除成功: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从URL中提取objectName
     */
    private String extractObjectName(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return null;
            }
            // 从最后一个/之后开始提取path部分
            String endpoint = aliyunOssConfig.getEndpoint();
            int endpointIndex = fileUrl.indexOf(endpoint);
            if (endpointIndex != -1) {
                int startIndex = endpointIndex + endpoint.length();
                return fileUrl.substring(startIndex).replaceFirst("^/", "");
            }
            return null;
        } catch (Exception e) {
            log.error("提取objectName失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 生成唯一的文件名
     */
    private String generateFileName(String originalFilename) {
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID() + suffix;
    }
}
