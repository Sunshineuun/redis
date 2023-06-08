package com.qiusm.redis.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Encrypted;

import java.io.Serializable;
import java.time.LocalDateTime;

import static org.springframework.data.mongodb.core.EncryptionAlgorithms.AEAD_AES_256_CBC_HMAC_SHA_512_Deterministic;

/**
 * <code>@Document</code>指定要对应的文档名(表名）
 *
 * @author qiushengming
 * @since 2022-04-17
 */
@Data
@Document(collection = "user")
@Encrypted(keyId = "zhouziyi", algorithm = AEAD_AES_256_CBC_HMAC_SHA_512_Deterministic)
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // @MongoId(value = FieldType.INT64)
    @Id
    private Long id;

    private Long maxId;

    private LocalDateTime createTime;

    @Encrypted
    private String password;

    private String username;

    @Version
    private Long version;

}
