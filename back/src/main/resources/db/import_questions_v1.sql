-- ============================================================
-- 试题导入脚本：2025年乙(A)试卷
-- 所有题目 biz_section = 1（单招）
-- 生成时间: 2026-08-09
-- ============================================================
USE `zhiping`;

-- 清理旧测试数据（可选，按需启用）
-- DELETE FROM t_question WHERE biz_section = 1 AND source = '2025年乙(A)试卷';

SET @sort = 0;

-- 单选题 #1
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 1, 2, '1. 下列各组词语中，加点字的书写或注音全都正确的一组是（ ）（3分）', '["淘冶 蜷缩（quán） 转弯抹（mǒ）角 旬私舞弊", "训戒 收敛（liǎn） 戎马倥偬（zǒng） 飞皇腾达", "青睐 铁锹（qiāo） 泥古不化（ní） 无与轮比", "熨帖 芭蕉（bā） 锋芒毕露（lù） 一拍即合"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #2
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 2, 2, '2.下列各句中，有语病的一项是（）（3分）', '["面对美国代表在联合国大会上的污蔑，中国代表耿爽回 击表示，如果中国 真的向俄罗斯提供军事补给，那战场的局势早就不是今天这个样子。", "2024年9月13日，全国人大常委会会议表决通过了关于 实施渐进式延迟法 定退休年龄的决定。", "山东省烟台市大数据局成功部署国产大模型 DeepSeek， 并率先将其应用于 政务服务领域，从而来加强智能化水平。", "2024年房地产市场在一揽子政策密集推出的作用下，呈 现止跌回稳的积极 势头。"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #3
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 3, 2, '3.对下列各句的分析，正确的一项是（）（3分）', '["某同学在讨论会上说：“像孙老师这样快要退休的老师仍 在为培养我们而略 尽绵薄之力，我们深感荣光。(该句表述不得体。)", "明天的活动，你是参加呢？还是不参加呢？（该句标点符 号使用正确。)", "她妈妈的衣服做得很漂亮。(该句无歧义。)", "“回眸一笑百媚生，六宫粉黛无颜色”描写的是王昭君。( 该句表述正确。)"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #4
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 4, 2, '4.下列选项中，能准确体现“执子之手，与子偕老”最早所形容感情的一项是 ()(3分)', '["卫青与霍去病之间的战友之情。", "孟母与孟子之间的亲情。", "梁山伯与祝英台之间的爱情。", "伯牙与钟子期之间的知音之情。"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 复合题 #5
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 7, NULL, 5, 2, '5.阅读语段，按要求完成下面的题目。(7分)
2025年春晚，魔术《画蛇添福》无疑是最受瞩目的节目之一。魔术师刘谦与尼
格买提搭档，以（）的技艺和（）的构思，为观众呈现了一场奇幻之旅。节目中，
刘谦将中国传统文化元素与魔术表演完美融合，从精心准备的道具到精美的传统图案，
都充满了浓浓的中国风。随着刘谦的一声“接下来，就是见证奇迹的时刻”，各种不
可思议的变化接连发生，让观众们惊叹不已。
，更是对传统文化传承与创新的一次成功探索。它让观众在欣
赏魔术的同时，也感受到了中国传统文化的独特魅力。通过魔术这一独特的艺术形式，
将传统文化以一种新颖、有趣的方式呈现出来，不仅吸引了更多年轻人对传统文化的
关注，也增强了文化传承活力，为传统文化的传承与发展注入了新的活力。', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);
SET @parent_5 = LAST_INSERT_ID();

-- 子题 #6 (parent=@parent_5)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_5, 1, '单选题', 6, 2, '(1）依次填入文中括号内的词语,最恰当的一组是（）（2分）', '["精湛巧妙", "精湛灵敏", "精细敏捷", "精深敏捷"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #7 (parent=@parent_5)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_5, 1, '单选题', 7, 2, '(2）根据文本内容，以下推理正确的一项是（）（2分）', '["因为刘谦与尼格买提成为搭档，所以魔术《画蛇添福》成为最受瞩目的节目。", "魔术《画蛇添福》成功是因为它将传统文化与魔术表演完美融合。", "只要将传统文化以新颖有趣的方式呈现，就能吸引年轻人关注传统文化。", "魔术《画蛇添福》让观众感受到传统文化的魅力，所以它是唯一成功的节目。"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #8 (parent=@parent_5)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_5, 4, '简答题', 8, 2, '（3）请在文中横线处补写恰当的语句，使整段文字语意连贯，逻辑严密，不超 过20个字。(3分)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #9
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 9, 2, '6. (1) ，谁家新燕啄春泥。(白居易《钱塘湖春行》)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #10
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 10, 2, '6. （2）但见悲鸟号古木， 。(李白《蜀道难》)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #11
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 11, 2, '6. （3）三顾频频天下计， 。(杜甫《蜀相》)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #12
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 12, 2, '6. (4) ，便胜却人间无数。（秦观《鹊桥仙》', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #13
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 13, 2, '6. (5）毛泽东在《沁园春·长沙》中，描绘了一幅色彩绚烂的秋景图，其中描写 江水和船只，展现出江面的生机勃勃与动态之美的两句是：', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 复合题 #14
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 7, NULL, 14, 2, '生命的声音
那是发生在一次煤矿透水事件中的故事。
他被困在矿井下，四周一片漆黑。卧在一个几十米高的工作台上，两天两夜了，
他的精神已经临近崩溃。他知道自己这一次是在劫难逃了。一个人孤零零地身处千米
以下的矿井中，没有吃的，没有喝的，更没有一点声音，不说饿死，憋也会把人憋死 。
他听老矿工说过，以往煤矿透水事件中死亡的人，很少是饿死或窒息死亡，大都
是精神崩溃，在救援队伍还未到来之前，先绝望死去。一般人是肉体死了，而后精神
随之消失；而精神绝望的人，一般都是精神死去，而后肉体也随之死去。
他就属于后者。他放弃了，与其这样孤孤单单地熬下去，这样在孤独中无望地等
还不如早些死了，早些解脱。
黑洞洞的煤坑里什么也没有，除了死亡的影子紧紧地跟随着他，咬噬着他的肉体
咀嚼着他的灵魂之外，什么也没有。这时，若有一点儿声音，哪怕是对他最恶毒的诅
咒，不，即使是一双手打在他脸上发出的声音，也会让他欣喜若狂，从而从恍恍惚惚
中醒来，重新振作起来。
但没有，一点儿也没有，连一块垃圾滚动的声音都不再有。
迷迷糊糊地，他感到光着的膀子上有点痒，下意识的用手去挠。同时，有一个声
音响起，声音很小，若有若无，但在他耳中听来，却如巨雷一样惊天动地。
嗡——分明是蚊子的声音。
他悚然一惊，忙坐起来，听着这天外之音，细细的，一波三折，时断时续。一会
儿离耳朵近了，很是清楚，如二胡的尾音；一会儿又远了，像梦的影子，让他努力侧
着耳朵去寻。
这大概也是一只饿极了的蚊子，已临近死亡的边缘。他暗暗地叹了一口气。
当这只蚊子再一次落在他的脖子上时，他一动不动。他清晰地感觉到这只蚊子几
只长长的肢在皮肤上爬动。接着，是一只管子扎了进去，吸他的血。
他如老僧入定一般，静静地躺在那里，一动不动。
蚊子吸饱了，飞起来了，嗡嗡地唱着。真好听。它飞向哪儿，他的头就转向哪儿 。
一直到它飞累了，停了下来，他也停止了寻找。他想打开矿灯去看看，可又怕惊吓了
它。
这一刻，他的心宁静极了。
他知道，他还活着，他不孤单，也不感到黑暗，至少，这儿还有一个生命陪伴着
他。虽然它那么小那么小，可此时，他们互相是对方的全部，包括希望，包括精神，
也包括生命。要活下去，他想，生命之间是相互关心的，尤其在患难中更是需要相濡
以沫。他相信，外面的工友们一定在千方百计地设法营救自己，他们绝不会坐视不管
他没有别的吃的，就将煤撮着一点一点往胃里咽。他听说过，有人在煤坑里就曾
以吃煤救过命。
此后的五天，他就以听蚊子叫和吃煤延续着自己的生命。
第六天，一道亮光倾泻而下。他得救了。
当他被救出时，耳边依然听到嗡嗡的唱歌声。
他的眼睛被包着，看不见，但分明感觉到了蚊子飞走的姿势，矫健，优美，绝不
拖泥带水。他想，生命是多么美好啊，正是在相互支撑相互扶持中，才显得丰富多彩
而毫不孤单。', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);
SET @parent_14 = LAST_INSERT_ID();

-- 子题 #15 (parent=@parent_14)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_14, 4, '简答题', 15, 2, '7.他为什么会放弃求生？后来又为什么重新燃起希望？请简要回答。(5分)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #16 (parent=@parent_14)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_14, 4, '简答题', 16, 2, '8.请从修辞的角度赏析下列句子。(5分) 一会儿离耳朵近了，很是清楚，如二胡的尾音；一会儿又远了，像梦的影子，让 他努力侧着耳朵去寻。', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #17 (parent=@parent_14)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_14, 4, '简答题', 17, 2, '9. 请分析下列句子在文中的作用。(5分) 他听老矿工说过，以往煤矿透水事件中死亡的人，很少是饿死或窒息死亡，大都装 是精神崩溃，在救援队伍还未到来之前，先绝望死去。', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #18 (parent=@parent_14)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_14, 4, '简答题', 18, 2, '10. 请结合文本分析文中主人公的性格特点。(5分)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #19 (parent=@parent_14)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_14, 4, '简答题', 19, 2, '11. 假如你是救援队伍中的一员，成功救出被困者后，请结合当时的情境，写一 段50字左右的话，表达你对他的问候与鼓励。(5分)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 复合题 #20
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 7, NULL, 20, 2, '猩猩嗜酒
猩猩，兽之好酒者也。大麓之人设以醴尊②。陈之饮器，小大具列焉。织草为履③
勾连相属也，而置之道旁。猩猩见，则知其诱之也，又知设者之姓名与其父母祖先
一一数而骂之。已而谓其朋曰：“盍④少尝之？慎无多饮矣！”相与取小器饮，骂而
去之。已而取差®大者饮，又骂而去之。如是者四，不胜其唇吻之甘也，遂大爵⑦而
忘其醉。醉则群睨嘻笑，取草履着之。麓人追之，相蹈藉而就絜®，无一得免焉。其
后来者亦然。夫猩猩智矣，恶其为诱也，而卒不免于死，贪为之也。
【注释】①麓(1ù)：山脚下。②醴(1ǐ)尊：醴，甜酒。尊，同“樽”，酒壶。③
履：鞋。④盍(hé)：何不。⑤差：稍微。⑥唇吻：指的是嘴。⑦爵：古代的一种酒杯。⑧
蹈藉：践踏。⑨就絷(zhí)：被拘囚。', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);
SET @parent_20 = LAST_INSERT_ID();

-- 子题 #21 (parent=@parent_20)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_20, 1, '单选题', 21, 2, '12. 给下面各句中加点词语选择合适的意思。(4分) (1)陈之饮器（）', '["陈列", "陈旧", "陈述", "姓陈"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #22 (parent=@parent_20)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_20, 1, '单选题', 22, 2, '12. 给下面各句中加点词语选择合适的意思。(4分) (2) 相与取小器饮（）', '["互相", "一起", "相同", "给与"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #23 (parent=@parent_20)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_20, 1, '单选题', 23, 2, '12. 给下面各句中加点词语选择合适的意思。(4分) (3） 慎无多饮矣（）', '["慎重", "谨慎", "千万", "小心"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #24 (parent=@parent_20)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_20, 1, '单选题', 24, 2, '12. 给下面各句中加点词语选择合适的意思。(4分) (4） 遂大爵而忘其醉（）', '["于是", "成功", "最终", "顺心"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #25 (parent=@parent_20)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_20, 4, '简答题', 25, 2, '13. 请将下面的句子翻译成现代汉语。(8分) (1)猩猩，兽之好酒者也。(4分) (2）已而谓其朋曰：“盍少尝之？慎无多饮矣！”(4分)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #26 (parent=@parent_20)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_20, 4, '简答题', 26, 2, '14. 猩猩明知是陷阱，却最终被擒，原因是什么？请用原文语句 回答。(4分)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #27 (parent=@parent_20)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_20, 4, '简答题', 27, 2, '15.从这则故事中，你获得了怎样的启示？请结合生活实际谈谈 。(4分)', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 简答题 #28
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 5, NULL, 28, 2, '16. 按要求写作文。(30分) 请以“家乡的味道”为题目，写一篇作文。 要求：文体自选；不要套作，不得抄袭；不少于300字；不得 透露真实地名、校 名、人名等相关信息。', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #29
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 29, 2, '1. 已知集合A={1,3,4, 5}，B={2, 3,4}，则A∩B=（）.', '["{1, 2, 3}", "{3, 4}", "{1, 3, 4}", "{1, 2,3,4,5}"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #30
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 30, 2, '2. 已知函数 f(x) = 2x + 5，f(-2) 的值为( ).', '["1", "-1", "9", "-9"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #31
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 31, 2, '3.已知向量a=(3,-2)，向量b =(-4,x)，向量a与向量b垂 直，则x是（）', '["6", "10", "-10", "-6"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #32
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 32, 2, '4. 已知等差数列 {an} 的前 n 项和为 Sn，若 a3 + a7 = 20，则 S9等于（）.', '["45", "90", "120", "75"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #33
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 33, 2, '5. 圆的方程为(x+1)²+(y+2)²=3，该圆的半径是（）.', '["1", "2", "√3", "3"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #34
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 34, 2, '6. 抛物线 y²=2x的开口方向（）.', '["向上", "向下", "向左", "向 右"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #35
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 35, 2, '7. 若函数 y = ax + b 在(-∞, +∞) 上是增函数，则（ ).', '["a>0", "b>0", "a<0", "b< 0"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #36
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 36, 2, '8. 函数 y = sin(2x - - )的对称轴是（）.', '["x = 0", "x = π-6", "x = π3", "x = π2"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #37
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 37, 2, '9. 函数 y = 2x + 1 与直线 x = 3 的交点坐标为', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #38
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 38, 2, '10. 函数 f (x) = sin (2x + π) 的最小正周期为 4', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #39
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 39, 2, '11.当0≤x≤4时，2a<-x²+4x恒成立，则实数a的取值范围为', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #40
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 40, 2, '12. 直线l:√3x+y+5=0的倾斜角α为', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #41
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 41, 2, '13.某中学高一年级有400人，高二年级有360人，高三年级有240人，若每人被 抽到的可能性都为0.3，用随机数表法在该中学抽取容量为n的样本，则n为', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 计算题 #42
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 6, NULL, 42, 2, '14.（1）解方程：2x2+5x-3=0. （2）计算：log,3 ·log34-5l0g53.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 计算题 #43
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 6, NULL, 43, 2, '15.点（2,1）和（3,3）在函数f(x)=3ax+b的图象上，求f(x)的解析式.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 计算题 #44
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 6, NULL, 44, 2, '16. 某机械厂每月固定生产甲、乙两种零件共60万件，并能全部售出.甲零件每 件成本10元，售价14元；乙零件每件成本9元，售价12元.设每月生产甲零件x万 件，甲、乙两种零件所获总利润y万元. (1）写出y与x的函数关系式； (2）如果每月投入的总成本不超过560万元，应该怎样安排甲、乙零件的产量， 可使所获的总利润最大？最大总利润是多少万元？', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #45
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 45, 2, '1. Tom may know where your bag is. You can ask', '["him", "his", "he", "himself"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #46
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 46, 2, '2. —Excuse me.can I get to the bank? —Take the No. 2 bus.', '["How", "What", "When", "Where"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #47
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 47, 2, '3. I can just useInternet once a week.', '["an", "a", "the", "/"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #48
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 48, 2, '4. Iily hurt her head,she didn\'t want to go to the hospital.', '["and", "so", "therefore", "but"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #49
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 49, 2, '5. Oh, I can\'t see anything. Could you pleasethe light?', '["turn off", "turn on", "turn down", "turn to"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #50
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 50, 2, '6. It\'s normal to wait for your guests. You can\'t expectto arrive on time.', '["no one", "someone", "none", "everyone"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #51
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 51, 2, '7. Lucy is asas Nancy. Nobody can make them laugh easily.', '["funny", "outgoing", "interesting", "serious"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #52
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 52, 2, '8.many students like to watch game shows, cartoons are the most popular.', '["When", "If", "Although", "Because"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #53
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 53, 2, '9. Jerryhis uncle\'s farm with his family last weekend.', '["visits", "visited", "will visit", "is visiting"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #54
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 54, 2, '10. —What does your father do on weekends? —He always sits on the sofa. Heexercises.', '["often", "sometimes", "usually", "hardly"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #55
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 55, 2, '11. —Who did the work better, Leo or Nick? —Leo was more careful. I think Leo did Nick.', '["as good as", "as well as", "better than", "worse than"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #56
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 56, 2, '12. This is my beautiful schoolis near the famous library.', '["which", "where", "what", "it"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #57
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 57, 2, '13.more, and you will improve your spoken English.', '["Speak", "Spoken", "Speaking", "To speak"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #58
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 58, 2, '14. Mum, my dress is too small. I need aone.', '["nice", "fat", "large", "red"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #59
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 59, 2, '15. I couldn\'t see the film clearly. Two tall boys satme.', '["between", "in front of", "next to", "behind"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #60
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 60, 2, '16. He wanted to play a on his friend, but he fooled himself.', '["game", "truth", "card", "joke"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #61
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 61, 2, '17. I\'m expecting a pet dog for long, but Mum has no time to buyfor me.', '["it", "one", "them", "that"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #62
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 62, 2, '18. Would you mind meyour bike?', '["use", "using", "to use", "used"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #63
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 63, 2, '19. —Tomorrow I\'m flying to Shanghai with my friends. —！', '["Thank you", "Best wishes", "Have a good trip", "Excuse me"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 单选题 #64
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 1, NULL, 64, 2, '20. —There is a saying that a friend in need is a friend indeed.', '["Sure it is", "It doesn\'t matter", "It\'s a piece of cake", "Never mind"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 复合题 #65
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 7, NULL, 65, 2, '根据短文内容，从A、B、C、D四个选项中选出一个能填入相应空格内的最佳答
案。
Peter is a boy who is 15 years old now. He works hard and does 21 in school. But it is
hard to believe that he used to have difficulties in school.
When his parents moved to 22 city to work, they could not stay at home to look after
him. So he became 23 in studying and was often absent from classes. He hated being in
groups, and he was 24 all the time. Then his parents decided to send him to a boarding
school. He found life there 25.
One day, Peter told his teacher he wanted to 26 the school. His teacher advised his
parents to talk with 27 son in person. The father took a 6-hour train and a 2-hour bus ride
to the boarding school, and he had a conversation 28 his son. This conversation changed
Peter\'s29 and he chose to stay at school. He realized that his parents would always love
him, and they would 30 everything good that he did.
Now he is much happier and more outgoing than he used to be.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);
SET @parent_65 = LAST_INSERT_ID();

-- 子题 #66 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 66, 2, '21.', '["good", "well", "badly", "bad"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #67 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 67, 2, '22.', '["other", "others", "another", "the other"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #68 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 68, 2, '23.', '["cleverer", "less interested", "better", "More stupid"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #69 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 69, 2, '24.', '["active", "popular", "outgoing", "alone"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #70 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 70, 2, '25.', '["easy", "difficult", "comfortable", "funny"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #71 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 71, 2, '26.', '["thank", "introduce", "leave", "visit"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #72 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 72, 2, '27.', '["your", "her", "their", "his"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #73 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 73, 2, '28.', '["with", "in", "on", "about"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #74 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 74, 2, '29.', '["mind", "school", "weight", "subject"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #75 (parent=@parent_65)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_65, 1, '单选题', 75, 2, '30.', '["give up", "give away", "take away", "take pride in"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 复合题 #76
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 7, NULL, 76, 2, '阅读下面两篇短文，根据短文内容，从A、B、C、D四个选项中选出一个最佳答
案。
Passage One
There are some activities on Tom\'s school bulletin board(布告牌).
A Basketball Game
Where: the Sports Center
When: 3:00 on Monday afternoon
Who: students of Class 2 and 3 in Senior Grade One
Movie Night
Where: Classroom 5
When: 5:05p.m.-6:34p.m. on Friday
What: Monkey King: Hero is Black
Happy Book Day
Where: the School Hall
When: 2:30p.m.-7:20p.m. on Wednesday
What: share your favorite books and writers with other stude nts', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);
SET @parent_76 = LAST_INSERT_ID();

-- 子题 #77 (parent=@parent_76)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_76, 1, '单选题', 77, 2, '31. If Tom wants to go to the Sports Center, what can he see?', '["A tennis game.", "A basketball game.", "A volleyball game.", "A swimming game."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #78 (parent=@parent_76)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_76, 1, '单选题', 78, 2, '32. When does the game start on Monday?', '["At 2:30p.m.", "At 3:00p.m.", "At 3:00a.m.", "At 5:05p.m."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #79 (parent=@parent_76)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_76, 1, '单选题', 79, 2, '33. How long is the movie Monkey King: Hero is Black?', '["For 59 minutes.", "For 69 minutes.", "For 89 minutes.", "For 99 minutes."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #80 (parent=@parent_76)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_76, 1, '单选题', 80, 2, '34. What can we do in Happy Book Day?', '["See favorite writers.", "Buy all kinds of books", "Make friends with others.", "Share favorite books a nd writers."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #81 (parent=@parent_76)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_76, 1, '单选题', 81, 2, '35. Who are the activities on the bulletin board for?', '["Students.", "Doctors.", "Teachers.", "Parents."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 复合题 #82
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 7, NULL, 82, 2, 'Passage Two
Lizzie, a foreign student at Tsinghua University, first came to Beijin g in 2017. She soon
fell in love with the city. Her favorite place is the hutong. She also find Is her new hobby—
Peking Opera.
She first saw Peking Opera when she was in an opera museum. "I fin d it really amazing,"
she said. "The clothes and the movements are also beautiful. The face pa inting is magic. It is
so much different from western opera."
Out of interest, she joined the Peking Opera group of Tsinghua University. Here she saw
that Peking Opera was much more than just the looks. "There is really great cultural
background. And it is the most important in Peking Opera."
The group members practice on Mondays, Wednesdays and Saturdays. Mostly, they
practice singing and movements. The big problem Lizzie had was learning the way of singing.
"In the beginning, I was using my voice in the wrong way," she said. But with the help of
teachers, she did better.
There are also students from other countries in the group. "Peking Opera brings us
together," she said. "I hope I can share this hobby with people all over the world."', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);
SET @parent_82 = LAST_INSERT_ID();

-- 子题 #83 (parent=@parent_82)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_82, 1, '单选题', 83, 2, '36. What does the underlined word "it" in Paragraph 2 refer to?', '["The university.", "The hutong.", "Peking Opera.", "Beijing."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #84 (parent=@parent_82)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_82, 1, '单选题', 84, 2, '37. In Lizzie\'s eyes, which of the following is the most important in Peking Opera?', '["The beautiful clothes.", "The movements.", "The face painting.", "The cultural background."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #85 (parent=@parent_82)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_82, 1, '单选题', 85, 2, '38. How often does Lizzie practice Peking Opera in the group?', '["Four times a week.", "Three times a week.", "Five times a week.", "Six times a week."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #86 (parent=@parent_82)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_82, 1, '单选题', 86, 2, '39. Lizzie\'s words in the last paragraph show that', '["Peking Opera brings people together", "learning Peking Opera is hard", "students like opera museums", "her teachers help her a lot"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #87 (parent=@parent_82)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_82, 1, '单选题', 87, 2, '40. What\'s the best title for the passage?', '["Lizzie\'s Interests in Music.", "Lizzie\'s Study in China.", "Lizzie\'s Love for Peking Opera.", "Lizzie\'s Problems in China."]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 复合题 #88
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 7, NULL, 88, 2, '根据对话情景，从方框中选择适当的句子补全对话，每句话只能用一次。
A: Welcome to my shop. 41
B: Yes. Have you got any white T-shirts with a dragon on it?
A: Of course. 42
B: I\'m not sure. Can I try them on?
A: Of course, here you are. Hmm... And I think the large size is right for you.
B: OK. 43
A: Sir, would you like anything else?
B: I\'d like this pair of gloves. 44 How much are they?
A: They are 232 yuan altogether. 45
B: In cash.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);
SET @parent_88 = LAST_INSERT_ID();

-- 子题 #89 (parent=@parent_88)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_88, 1, '单选题', 89, 2, '41. A: Welcome to my shop. ___', '["How would you like to pay, sir?", "I\'ll take a large one.", "Can I help you?", "They look beautiful and warm.", "What size do you take?"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #90 (parent=@parent_88)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_88, 1, '单选题', 90, 2, '42. A: Of course. ___', '["How would you like to pay, sir?", "I\'ll take a large one.", "Can I help you?", "They look beautiful and warm.", "What size do you take?"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #91 (parent=@parent_88)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_88, 1, '单选题', 91, 2, '43. B: OK. ___', '["How would you like to pay, sir?", "I\'ll take a large one.", "Can I help you?", "They look beautiful and warm.", "What size do you take?"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #92 (parent=@parent_88)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_88, 1, '单选题', 92, 2, '44. B: I\'d like this pair of gloves. ___ How much are they?', '["How would you like to pay, sir?", "I\'ll take a large one.", "Can I help you?", "They look beautiful and warm.", "What size do you take?"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 子题 #93 (parent=@parent_88)
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, @parent_88, 1, '单选题', 93, 2, '45. A: They are 232 yuan altogether. ___', '["How would you like to pay, sir?", "I\'ll take a large one.", "Can I help you?", "They look beautiful and warm.", "What size do you take?"]', NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #94
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 94, 2, '46. These problems are too hard to', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #95
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 95, 2, '47. Jane began to save money she could buy a birthday gift for her mother.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #96
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 96, 2, '48. Lu Xun is a great writer of China.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #97
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 97, 2, '49. Our teacher a bookshelf at the back of the classroom to make a smal reading corner.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #98
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 98, 2, '50. Kunming is famous the City of Spring.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #99
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 99, 2, '51. The kid is very and gets good grades.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #100
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 100, 2, '52. My mother is busy. She spends little time playing with me, so I\'d like her her timetable.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #101
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 101, 2, '53. There is a lot of food in this shopping center.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #102
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 102, 2, '54. Looking at the for a long time is bad for your eyes.', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 填空题 #103
INSERT INTO t_question (biz_section, category_id, parent_id, type, sub_type, sort, difficulty, content, options, answer, score, analysis, source, year)
VALUES (1, 1, 0, 4, NULL, 103, 2, '55. Jack, watching movies with us on Saturday?', NULL, NULL, NULL, NULL, '2025年乙(A)试卷', 2025);

-- 导入完成，共 103 条 INSERT
SELECT COUNT(*) AS total FROM t_question WHERE source = '2025年乙(A)试卷';