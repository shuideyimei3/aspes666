package cn.aspes.agri.trade.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * JSON配置类 - 将所有大整数ID字段以字符串形式返回
 */
@Configuration
public class JsonConfig {

    /**
     * 自定义Long类型序列化器，将Long类型转换为字符串
     */
    public static class LongToStringSerializer extends StdSerializer<Long> {
        
        public LongToStringSerializer() {
            super(Long.class);
        }

        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.toString());
            }
        }
    }

    /**
     * 配置全局ObjectMapper，将所有Long类型字段序列化为字符串
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 注册自定义序列化器
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, new LongToStringSerializer());
        module.addSerializer(Long.TYPE, new LongToStringSerializer());
        objectMapper.registerModule(module);
        
        // 其他配置
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        return objectMapper;
    }
}