package com.shaanxi.zhiping.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shaanxi.zhiping.config.WxMiniAppConfig;
import com.shaanxi.zhiping.dto.LoginVO;
import com.shaanxi.zhiping.dto.WxLoginDTO;
import com.shaanxi.zhiping.entity.User;
import com.shaanxi.zhiping.mapper.UserMapper;
import com.shaanxi.zhiping.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务（微信登录）
 */
@Slf4j
@Service
public class AuthService {

    @Resource
    private WxMiniAppConfig wxConfig;

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 微信小程序登录
     * 1. 通过 code 换取 openid + session_key
     * 2. 查询/创建用户
     * 3. 生成 JWT token
     */
    @Transactional
    public LoginVO wxLogin(WxLoginDTO dto) {
        // 1. code2Session 获取 openid
        Map<String, Object> params = new HashMap<>();
        params.put("appid", wxConfig.getAppid());
        params.put("secret", wxConfig.getSecret());
        params.put("js_code", dto.getCode());
        params.put("grant_type", "authorization_code");

        String response = HttpUtil.get(wxConfig.getLoginUrl(), params);
        log.info("微信 code2session 返回: {}", response);

        JSONObject json = JSONUtil.parseObj(response);
        String openid = json.getStr("openid");
        String sessionKey = json.getStr("session_key");

        if (openid == null) {
            String errMsg = json.getStr("errmsg", "微信登录失败");
            Integer errcode = json.getInt("errcode", -1);
            log.error("微信登录失败 errcode={} errmsg={}", errcode, errMsg);
            throw new RuntimeException("微信登录失败: " + errMsg);
        }

        // 2. 查询/创建用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);
        boolean isNewUser = false;

        if (user == null) {
            // 新用户注册
            user = new User();
            user.setOpenid(openid);
            user.setUnionId(json.getStr("unionid"));
            user.setNickname(dto.getNickname());
            user.setAvatar(dto.getAvatar());
            user.setGender(dto.getGender() != null ? dto.getGender() : 0);
            user.setMemberLevel(0);
            userMapper.insert(user);
            isNewUser = true;
            log.info("新用户注册, userId={}, openid={}", user.getId(), openid);
        } else {
            // 更新昵称头像
            user.setNickname(dto.getNickname());
            if (dto.getAvatar() != null) {
                user.setAvatar(dto.getAvatar());
            }
            if (dto.getGender() != null) {
                user.setGender(dto.getGender());
            }
            userMapper.updateById(user);
        }

        // 3. 生成 JWT token
        String token = jwtUtils.generateToken(user.getId(), openid, user.getNickname());

        // 4. 组装返回
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setOpenid(openid);
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setMemberLevel(user.getMemberLevel());
        vo.setIsNewUser(isNewUser);
        return vo;
    }
}
