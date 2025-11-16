package cn.aspes.agri.trade.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    
    /**
     * 上传文件到MinIO
     * @param file 上传的文件
     * @param bucketName 桶名称
     * @return 文件URL
     */
    String uploadFile(MultipartFile file, String bucketName);
    
    /**
     * 上传产品图片
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadProductImage(MultipartFile file);
    
    /**
     * 上传合同签字文件
     * @param file 签字文件
     * @return 文件URL
     */
    String uploadContractSign(MultipartFile file);
    
    /**
     * 上传支付凭证
     * @param file 凭证文件
     * @return 文件URL
     */
    String uploadPaymentVoucher(MultipartFile file);
    
    /**
     * 上传身份证正面照
     * @param file 身份证正面照文件
     * @return 图片URL
     */
    String uploadIdCardFront(MultipartFile file);
    
    /**
     * 上传身份证反面照
     * @param file 身份证反面照文件
     * @return 图片URL
     */
    String uploadIdCardBack(MultipartFile file);
    
    /**
     * 上传营业执照
     * @param file 营业执照文件
     * @return 文件URL
     */
    String uploadBusinessLicense(MultipartFile file);
    
    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
}