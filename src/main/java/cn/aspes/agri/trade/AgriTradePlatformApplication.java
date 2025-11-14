package cn.aspes.agri.trade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 农副产品交易对接平台 - 主启动类
 */
@SpringBootApplication
@MapperScan("cn.aspes.agri.trade.mapper")
public class AgriTradePlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgriTradePlatformApplication.class, args);
    }
}
