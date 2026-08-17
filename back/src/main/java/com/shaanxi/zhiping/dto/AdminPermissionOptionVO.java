package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.util.List;

/**
 * 全部可分配权限项
 */
@Data
public class AdminPermissionOptionVO {

    /** 权限码 */
    private String code;

    /** 显示名 */
    private String name;

    /** 分组 */
    private String group;

    public AdminPermissionOptionVO() {
    }

    public AdminPermissionOptionVO(String code, String name, String group) {
        this.code = code;
        this.name = name;
        this.group = group;
    }

    /**
     * 系统内置权限清单
     */
    public static final List<AdminPermissionOptionVO> BUILTIN;

    static {
        List<AdminPermissionOptionVO> list = new java.util.ArrayList<>();
        // 题库中心
        list.add(new AdminPermissionOptionVO("question:view", "试题查看", "题库中心"));
        list.add(new AdminPermissionOptionVO("question:entry", "试题录入", "题库中心"));
        list.add(new AdminPermissionOptionVO("question:import", "批量导入", "题库中心"));
        list.add(new AdminPermissionOptionVO("question:proofread", "试题校对", "题库中心"));
        list.add(new AdminPermissionOptionVO("question:edit", "试题编辑", "题库中心"));
        list.add(new AdminPermissionOptionVO("question:review", "试题审核", "题库中心"));
        // 院校管理
        list.add(new AdminPermissionOptionVO("college:list", "院校列表", "院校管理"));
        list.add(new AdminPermissionOptionVO("college:edit", "院校编辑", "院校管理"));
        // 用户管理（仅超管）
        list.add(new AdminPermissionOptionVO("admin:user:list", "管理员列表", "系统管理"));
        list.add(new AdminPermissionOptionVO("admin:user:edit", "管理员编辑", "系统管理"));
        BUILTIN = java.util.Collections.unmodifiableList(list);
    }
}
