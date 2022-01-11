package com.ant.ipush.domain;

import lombok.Getter;

public enum BehaviorEvent {
    favorites("收藏"), unfavorites("取消收藏"), follow("关注"), unfollow("取消关注"), like("点赞"), cancelLike("取消点赞"), comments("评论"),
    purchase("交易待支付"), dailyAttendance("每日签到"), share("分享"),
    glance("浏览"), exchange("兑换"), paid("已付"),
    birthday("生日"), payRefund("退款"), Festivals("节假日"),
    login("登录"), cancelGoods("商品下架"),
    New("生成订单"), Shipped("已经发货"), Receive("收货"), Accounted("入账/可提现"), Cancel("取消订单"),
    shoppingCart("加入购物车"), cancelShoppingCart("移除购物车"), coupon("优惠卷"), inviteMember("邀请注册会员"), inviteLoginUser("邀请用户登陆"), register("会员注册");

    @Getter
    String desc;

    BehaviorEvent(String desc) {
        this.desc = desc;
    }


}
