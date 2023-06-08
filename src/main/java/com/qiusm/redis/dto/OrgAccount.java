package com.qiusm.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author qiushengming
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class OrgAccount {
    private String orgCode;
    private String orgAccount;
    private String chainPublicKey;
    private String dataPublicKey;
    private String orgName;
    private String orgType;
    private String province;
    private String city;
    private String ipfs;
    private String createTime;
    private String updateTime;
    private String remarks;
}