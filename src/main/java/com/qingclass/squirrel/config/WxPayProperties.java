package com.qingclass.squirrel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <pre>
 *  微信支付属性配置类
 * Created by Binary Wang on 2019/4/17.
 * </pre>
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Data
@ConfigurationProperties(prefix = "wx.pay")
public class WxPayProperties {
  /**
   * 设置微信公众号或者小程序等的appid.
   */
  private String appId;

  /**
   * 微信支付商户号.
   */
  private String mchId;

  /**
   * 微信支付商户密钥.
   */
  private String mchKey;

  /**
   * apiclient_cert.p12文件的绝对路径，或者如果放在项目中，请以classpath:开头指定.
   */
  private String keyPath;
}
