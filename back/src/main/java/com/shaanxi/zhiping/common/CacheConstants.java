package com.shaanxi.zhiping.common;

/**
 * Redis 缓存 Key 规范与 TTL 常量
 *
 * Key 命名规范：业务模块:子域:标识
 * 例如：question:detail:123
 *      category:tree
 *      session:token:abc123
 */
public final class CacheConstants {

    private CacheConstants() {}

    // ==================== Key 前缀 ====================

    /** 题目详情前缀，完整 key: question:detail:{id} */
    public static final String QUESTION_DETAIL_PREFIX = "question:detail:";

    /** 复合题子题列表前缀，完整 key: question:children:{parentId} */
    public static final String QUESTION_CHILDREN_PREFIX = "question:children:";

    /** 题目列表分页前缀，完整 key: question:list:{bizSection}:{categoryId}:{type}:{page}:{size} */
    public static final String QUESTION_LIST_PREFIX = "question:list:";

    /** 小程序真题练习整卷缓存前缀，完整 key: practice:list:{year}:{categoryId} */
    public static final String PRACTICE_LIST_PREFIX = "practice:list:";

    /** 试卷详情前缀，完整 key: paper:detail:{id} */
    public static final String PAPER_DETAIL_PREFIX = "paper:detail:";

    /** 分类树（全量），完整 key: category:tree */
    public static final String CATEGORY_TREE_KEY = "category:tree";

    /** 分类详情前缀，完整 key: category:detail:{id} */
    public static final String CATEGORY_DETAIL_PREFIX = "category:detail:";

    /** 院校详情前缀，完整 key: college:detail:{id} */
    public static final String COLLEGE_DETAIL_PREFIX = "college:detail:";

    /** 首页统计缓存前缀，完整 key: stats:home:{userId} */
    public static final String STATS_HOME_PREFIX = "stats:home:";

    /** 会话 token 前缀，完整 key: session:token:{token} */
    public static final String SESSION_TOKEN_PREFIX = "session:token:";

    /** 用户信息前缀，完整 key: session:user:{userId} */
    public static final String SESSION_USER_PREFIX = "session:user:";

    /** 限流计数器前缀，完整 key: rate:limit:{module}:{key} */
    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    /** 微信登录 code 防重放前缀，完整 key: wx:code:{code} */
    public static final String WX_CODE_PREFIX = "wx:code:";

    /** 管理后台会话 token 前缀，完整 key: session:admin:token:{token} */
    public static final String SESSION_ADMIN_TOKEN_PREFIX = "session:admin:token:";

    /** 管理后台用户信息前缀，完整 key: session:admin:user:{adminId} */
    public static final String SESSION_ADMIN_USER_PREFIX = "session:admin:user:";

    // ==================== TTL（秒） ====================

    /** 题目详情缓存 30 分钟 */
    public static final long TTL_QUESTION_DETAIL = 30 * 60;

    /** 复合题子题缓存 30 分钟 */
    public static final long TTL_QUESTION_CHILDREN = 30 * 60;

    /** 题目列表分页缓存 10 分钟 */
    public static final long TTL_QUESTION_LIST = 10 * 60;

    /** 试卷详情缓存 30 分钟 */
    public static final long TTL_PAPER_DETAIL = 30 * 60;

    /** 分类树缓存 2 小时（分类变更频率低） */
    public static final long TTL_CATEGORY_TREE = 2 * 60 * 60;

    /** 分类详情缓存 1 小时 */
    public static final long TTL_CATEGORY_DETAIL = 60 * 60;

    /** 院校详情缓存 1 小时 */
    public static final long TTL_COLLEGE_DETAIL = 60 * 60;

    /** 首页统计缓存 5 分钟 */
    public static final long TTL_STATS_HOME = 5 * 60;

    /** 会话 token 缓存 7 天（与 JWT 过期一致） */
    public static final long TTL_SESSION = 7 * 24 * 60 * 60;

    /** 微信登录 code 防重放 5 分钟 */
    public static final long TTL_WX_CODE = 5 * 60;

    /** 限流计数器默认窗口 60 秒 */
    public static final long TTL_RATE_LIMIT_DEFAULT = 60;
}
