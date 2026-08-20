package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.dto.StudentQueryDTO;
import com.shaanxi.zhiping.dto.StudentVO;
import com.shaanxi.zhiping.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 管理后台 - 学员管理服务
 * <p>学员数据来自 t_user 表（微信小程序登录时自动注册），
 * 关联 t_practice_record 聚合得到刷题统计。
 */
@Slf4j
@Service
public class AdminStudentService {

    @Resource
    private UserMapper userMapper;

    /**
     * 分页查询学员列表
     */
    public PageResult<StudentVO> page(StudentQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();
        Page<StudentVO> p = new Page<>(page, size);
        IPage<StudentVO> result = userMapper.selectStudentPage(p, query);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }
}
