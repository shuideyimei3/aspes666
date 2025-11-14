package cn.aspes.agri.trade.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssConfig {
    
    private String endpoint;        // OSS地域节点，如：https://oss-cn-hangzhou.aliyuncs.com
    private String accessKeyId;     // 阿里云账户AccessKeyId
    private String accessKeySecret; // 阿里云账户AccessKeySecret
    private String bucketName;      // 默认Bucket名称
    private String basePath;        // 基础路径前缀，如：agri-trade
    
    @Bean
    @ConditionalOnProperty(prefix = "aliyun.oss", name = "access-key-id")
    public OSS ossClient() {
        return new OSSClientBuilder()
                .build(endpoint, accessKeyId, accessKeySecret);
    }
}
