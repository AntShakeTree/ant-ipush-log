package com.ant.ipush.domain;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * app_id varchar not null comment '应用id',
 * app_name varchar not null comment '应用名称',
 * version varchar not null comment '埋点方案版本' ,
 * ip varchar not null comment '用户访问时的ip',
 * time timestamp not null comment '用户访问数据的时间',
 * timezone varchar not null comment '客户端本地时区',
 * mac varchar not null comment 'MAC地址',
 * resolving_powercr varchar not null comment '分辨率',
 * agent varchar not null comment '客户端信息',
 * browser varchar not null comment '浏览器名称',
 * os varchar not null comment '操作系统名称',
 * os_language varchar not null comment '操作系统语言',
 * net varchar not null comment '网络类型',
 * carrier varchar not null comment '运营商',
 * model varchar not null comment '设备型号',
 * province varchar not null comment '省',
 * city varchar not null comment '市',
 * area varchar not null comment '区',
 * lat varchar not null comment '维度',
 * lon varchar not null comment '经度',
 * sid varchar not null comment '会话id',
 * cookie varchar not null comment '客户端cookie唯一标识',
 * uid varchar not null comment '用户id',
 * ute varchar not null comment '客户系统用户类型',
 * seller_id bigint not null comment '商户id',
 * channel_id bigint not null comment '渠道id',
 * channel_name varchar not null comment '渠道名称',
 * current_url varchar not null comment '当前页面地址',
 * guest_url varchar not null comment '访问的页面地址',
 * source_url varchar not null comment '跳转来源地址',
 * source_id varchar comment '页面来源id',
 * source_name varchar comment '页面来源名称',
 * page_id varchar not null comment '页面id',
 * page_name varchar not null comment '页面名称',
 * flash_version varchar not null comment 'flash版本',
 * action_id varchar not null comment '事件id',
 * action_name varchar not null comment '事件描述',
 * <p>
 * certificate_id varchar comment '券id',
 * certificate_name varchar comment'券名称',
 * shop_id varchar comment '门店id',
 * shop_name varchar comment '门店名称',
 * assembly_id varchar comment '组件id',
 * assembly_name varchar comment '组件名称',
 * jump_page_id varchar comment '要跳转的页面id',
 * jump_page_name varchar comment '要跳转的页面名称',
 * <p>
 * activity_id bigint comment '活动id',
 * activity_name varchar comment '活动名称',
 * <p>
 * a_type varchar comment '事件类型,浏览 :view 点击 :click 页面进入:pageEnter 页面离开:pageLeave 分享:share',
 * p_type int comment '组件化：1 互动管理：2',
 * a_source varchar comment '事件来源 页面事件:page 组件事件：component（如：Banner组件，菜单列表组件）单元事件：unit（如Banner上的图片，菜单列表中的某一个item，或者页面上的一个按钮',
 * <p>
 * unit_id varchar comment '单元id',
 * unit_n varchar comment '单元名称',
 * <p>
 * prize_id varchar comment '奖品id',
 * prize_name varchar comment '奖品名称',
 * reward varchar comment '领奖',
 * prize int comment '中奖',
 * prizes_count int comment '奖品总数',
 * share_uid varchar comment '分享者id',
 * o_share_uid varchar comment '原始分享者id',
 * <p>
 * sex int comment '性别 0:未知 1:男 2:女',
 * vid varchar comment '会话id，开始到结束是同一个id',
 * vtime int comment '停留时长',
 * member_name varchar comment '会员姓名',
 * baby_sex int comment '宝宝性别 0:未知 1:男 2:女',
 * baby_birthday varchar comment '宝宝生日',
 * mobile varchar comment '手机号',
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogAnalytics {
    long ts;
    String ds;
    String appId;

    String appName;
    String version;
    String sourceOpenId;
    String ip;
    String timezone;
    String mac;
    String resolvingPowercr;
    String agent;
    String browser;
    String os;
    String osLanguage;
    String net;
    String carrier;
    String model;
    String province;
    String city;
    String area;
    String lat;
    String lon;
    String sid;
    String cookie;
    int uid;
    int invitee;
    //商品信息
    Long goodsId;
    Long skuId;
    Long spuId;
    String skuCode;
    String spuCode;
    //商品名称
    String goodsName;
    String ute;
    Long sellerId;
    Long channelId;
    String channelName;
    String currentUrl;
    String guestUrl;
    String sourceUrl;
    String sourceId;
    String sourceName;
    String pageId;
    String pageName;
    String flashVersion;
    String actionId;
    String actionName;
    String certificateId;
    String certificateName;
    String certificateCode;
    String shopId;
    String shopName;
    String assemblyId;
    String assemblyName;
    String jumpPageId;
    String jumpPageName;
    Long activityId;
    String activityName;
    String aType;
    String payStatus;
    int pType;
    String aSource;
    String unitId;
    String unitN;
    Long prizeId;
    String prizeName;
    String reward;
    int prize;
    int integral;
    int prizesCount;
    int goodsCount;
    String shareUid;
    String oShareUid;
    int sex;
    String vid;

    /**
     *
     */
    int vtime;
    String memberName;
    int babySex;
    String babyBirthday;
    String mobile;
    int memberId;
    int productId;
    /**
     * 请求路径
     */
    private String path;
    /**
     * head埋点自定义内容
     */
    private String params;
    String orderId;
    String orderCode;
    /**
     * 请求参数?=
     */
    private String query;
    Long storeId;
    Long placeTime;
    Long payTime;
    Long shipmentTime;
    Long receivingTime;
    Long accountTime;
    Long cancelTime;
    int purchaseChannelId;
    String carrierName;
    String waybillNo;
    Integer deliveryMode;
    Integer receiverAddressId;
    String receiverProvince;
    String receiverCity;
    String receiverDistrict;
    String receiverAddress;
    String receiverTel;
    String receiverName;
    String payMethodId;
    int amount;
    int deliveryAmount;
    int discount;
    String channel;
    String openId;
    /**
     * 登陆状态
     */
    private String loginStatus;
    private String storeCode;
    private Boolean subscribe;
    private String nickname;
    private String sexDesc;
    private String language;
    private String country;
    private String headImgUrl;
    private Long subscribeTime;
    private String unionId;
    private String remark;
    private Integer groupId;
    private String subscribeScene;
    private String qrScene;
    private String qrSceneStr;
    private String sellerName;
    private String memberLevel;
    private String behaviorLevel;
    private String realName;
    private String avatar;
    private String county;
    private String channelUnionId;
    private String admissionStoreName;
    private Long admissionStoreId;
    private LocalDate birthday;
    private String idCard;
    private String address;
    private Long inviteUserId;
    private String inviteUserName;
    private String lastLoginIp;
    private Long emId;
    private String emName;
    private String orderStatus;
    private Map<String, Object> ps;


    //    double latitude;
//    double longitude;
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}