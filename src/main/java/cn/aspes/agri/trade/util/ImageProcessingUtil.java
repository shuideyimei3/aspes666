package cn.aspes.agri.trade.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 图片处理工具类
 * 提供图片压缩、尺寸调整等功能
 */
@Slf4j
public class ImageProcessingUtil {

    // 默认最大宽度
    private static final int DEFAULT_MAX_WIDTH = 1920;
    // 默认最大高度
    private static final int DEFAULT_MAX_HEIGHT = 1080;
    // 默认压缩质量
    private static final float DEFAULT_QUALITY = 0.8f;
    // 最大文件大小 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    // 目标压缩大小 300KB
    private static final long TARGET_COMPRESSED_SIZE = 300 * 1024;

    /**
     * 检查文件大小是否超出限制
     *
     * @param file 原始图片文件
     * @throws IllegalArgumentException 当文件大小超出限制时抛出异常
     */
    public static void checkFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过5MB");
        }
    }

    /**
     * 压缩图片到指定大小以内
     *
     * @param file 原始图片文件
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressImageToTargetSize(MultipartFile file) {
        try {
            // 首先检查文件大小
            checkFileSize(file);

            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                log.error("无法读取图片，可能是不支持的格式或文件损坏: {}", file.getOriginalFilename());
                throw new IllegalArgumentException("不支持的图片格式或文件已损坏，请上传jpg/png/gif格式");
            }

            // 获取原始图片尺寸
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 初始压缩参数
            int maxWidth = DEFAULT_MAX_WIDTH;
            int maxHeight = DEFAULT_MAX_HEIGHT;
            float quality = DEFAULT_QUALITY;

            // 如果原始图片尺寸小于目标尺寸，则不进行缩放
            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                maxWidth = originalWidth;
                maxHeight = originalHeight;
            } else {
                // 按比例计算新的尺寸
                double widthRatio = (double) maxWidth / originalWidth;
                double heightRatio = (double) maxHeight / originalHeight;
                double ratio = Math.min(widthRatio, heightRatio);

                maxWidth = (int) (originalWidth * ratio);
                maxHeight = (int) (originalHeight * ratio);
            }

            // 循环压缩直到达到目标大小
            byte[] compressedImage = compressImageWithParams(originalImage, file, maxWidth, maxHeight, quality);
            
            // 如果初始压缩后的图片仍然大于目标大小，则进一步降低质量和尺寸
            int attempts = 0;
            while (compressedImage.length > TARGET_COMPRESSED_SIZE && attempts < 10) {
                // 优先降低质量
                if (quality > 0.3) {
                    quality -= 0.1;
                } else if (maxWidth > 800) {
                    // 如果质量已经很低但图片还是太大，则减小尺寸
                    maxWidth = (int) (maxWidth * 0.8);
                    maxHeight = (int) (maxHeight * 0.8);
                    quality = 0.3f; // 保持较低的质量
                } else {
                    // 如果已经很小了但还是太大，则直接使用目标质量
                    break;
                }
                
                compressedImage = compressImageWithParams(originalImage, file, maxWidth, maxHeight, quality);
                attempts++;
            }

            log.debug("图片压缩完成，原始大小: {} bytes, 压缩后大小: {} bytes, 压缩率: {}%", 
                     file.getSize(), compressedImage.length, 
                     String.format("%.2f", (1.0 - (double) compressedImage.length / file.getSize()) * 100));
            
            return compressedImage;
        } catch (IOException e) {
            log.error("图片压缩失败: {}", e.getMessage(), e);
            throw new RuntimeException("图片压缩失败", e);
        }
    }

    /**
     * 使用指定参数压缩图片
     *
     * @param originalImage 原始图片
     * @param file 原始文件（用于获取格式）
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量
     * @return 压缩后的图片字节数组
     * @throws IOException IO异常
     */
    private static byte[] compressImageWithParams(BufferedImage originalImage, MultipartFile file, 
                                                  int maxWidth, int maxHeight, float quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(maxWidth, maxHeight)
                .outputQuality(quality)
                .outputFormat(getImageFormat(file.getOriginalFilename()))
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    /**
     * 压缩图片
     *
     * @param file 原始图片文件
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressImage(MultipartFile file) {
        return compressImage(file, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, DEFAULT_QUALITY);
    }

    /**
     * 压缩图片
     *
     * @param file    原始图片文件
     * @param quality 压缩质量 (0.0 - 1.0)
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressImage(MultipartFile file, float quality) {
        return compressImage(file, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, quality);
    }

    /**
     * 压缩图片
     *
     * @param file      原始图片文件
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @param quality   压缩质量 (0.0 - 1.0)
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressImage(MultipartFile file, int maxWidth, int maxHeight, float quality) {
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                log.error("无法读取图片，可能是不支持的格式或文件损坏: {}", file.getOriginalFilename());
                throw new IllegalArgumentException("不支持的图片格式或文件已损坏，请上传jpg/png/gif格式");
            }

            // 获取原始图片尺寸
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 如果原始图片尺寸小于目标尺寸，则不进行缩放
            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                maxWidth = originalWidth;
                maxHeight = originalHeight;
            } else {
                // 按比例计算新的尺寸
                double widthRatio = (double) maxWidth / originalWidth;
                double heightRatio = (double) maxHeight / originalHeight;
                double ratio = Math.min(widthRatio, heightRatio);

                maxWidth = (int) (originalWidth * ratio);
                maxHeight = (int) (originalHeight * ratio);
            }

            // 使用Thumbnailator进行压缩
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(maxWidth, maxHeight)
                    .outputQuality(quality)
                    .outputFormat(getImageFormat(file.getOriginalFilename()))
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("图片压缩失败: {}", e.getMessage(), e);
            throw new RuntimeException("图片压缩失败", e);
        }
    }

    /**
     * 创建图片缩略图
     *
     * @param file 原始图片文件
     * @param size 缩略图尺寸
     * @return 缩略图字节数组
     */
    public static byte[] createThumbnail(MultipartFile file, int size) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                log.error("无法读取图片，可能是不支持的格式或文件损坏: {}", file.getOriginalFilename());
                throw new IllegalArgumentException("不支持的图片格式或文件已损坏，请上传jpg/png/gif格式");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(size, size)
                    .crop(Positions.CENTER)
                    .outputQuality(DEFAULT_QUALITY)
                    .outputFormat(getImageFormat(file.getOriginalFilename()))
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("创建缩略图失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建缩略图失败", e);
        }
    }

    /**
     * 根据文件名获取图片格式
     *
     * @param fileName 文件名
     * @return 图片格式
     */
    private static String getImageFormat(String fileName) {
        if (fileName == null) {
            return "jpg";
        }

        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".png")) {
            return "png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "gif";
        } else {
            return "jpg";
        }
    }
}