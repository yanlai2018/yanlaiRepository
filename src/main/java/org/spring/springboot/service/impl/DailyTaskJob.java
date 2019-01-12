package org.spring.springboot.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import base.zw.controller.BaseController;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.input.DataFormat;
import org.apache.commons.lang.StringUtils;
import org.spring.springboot.dao.DaoSupport;
import org.spring.springboot.domain.*;
import org.spring.springboot.zw.util.Const;
import org.spring.springboot.zw.util.DateUtil;
import org.spring.springboot.zw.util.StringUtil;
import org.spring.springboot.zw.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.*;

import static org.apache.commons.lang3.StringUtils.INDEX_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.spring.springboot.zw.util.Const.PAGE;
import static org.spring.springboot.zw.util.DESUtil.decryptBasedDes;
import static org.spring.springboot.zw.util.DESUtil.encryptBasedDes;

/**
 * Title: BaseController
 * Description:
 *
 * @author 王燕来
 * @version 2018年12月26日 上午11:31:41
 */

@Component
@Configurable
@EnableScheduling
public class DailyTaskJob extends BaseController {
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    @Autowired
    private DataSourceTransactionManager transactionManager;
    int userNum = 0;
    int nowPage = 0;
    long begintime = System.currentTimeMillis();

    //        @Scheduled(cron = "0 0 1 * * ?")   //每天凌晨1点01执行一次,跑批任务，主要针对个人积分过期后的扣减处理
//   @Scheduled(cron = "59 * * * * ?")  //每59秒钟执行一次(测试使用)
//   @Scheduled(cron = "22 45 * * * ?")  //每10秒钟执行一次(测试使用)
   @Scheduled(cron = "55 18 * * * ?")  //每小时47分31秒执行(测试使用)
    public void reportCurrentByCron() throws Exception {
        begintime = System.currentTimeMillis();
        final String nowDate = DateUtil.getDay();
        logger.info("系统后台日终自动跑分开始——Scheduling Tasks Examples By Cron: The time is now " + DateUtil.getTime());
        logger.info("个人积分信息日终处置开始");
        //1、查询用户积分汇总信息表，2、整理出所有用户ID做成列表(采用分页查询办法)，3、查询积分配置信息表中用户积分实效时间（单位日），
        //3、然后依据列表中的用户id，和（当前日期减去对应有效期的时间得出的日期），逐一查询积分变更日志中变更积分为正值得所有记录，
        //4、对每个用户的所有日志变更记录的正向积分进行汇总，汇总之后对单一用户的需要扣除积分和，单一用户的当前积分表积分进行比对，
        //5、不够扣除，直接清零，满足扣除作差并且更新个人积分汇总信息表，添加日志。以此类推。
        logger.info("检查个人积分表所有积分");
        final PageData[] qureyData = {new PageData()};
        //添加分页信息    //初始化设置每次查询20000条//默认从第一页开始查询
        final Page page = new Page();
        final String pageSize = Const.MAX_PAGESIZE; /*每次查询多少条作为一个批次进行处理*/
        final String[] score = {null};
        final String[] userId = {null};
        logger.info("插入个人操作日志表");
        insertSysOperatelog(logger);
        logger.info("查询积分有效期。");
        PerIntegrationConfigure perIntegrationConfigure = new PerIntegrationConfigure();
        perIntegrationConfigure.setScoreType(Const.ACTUAL_EFFECT_TIME);
        //默认使用常量配置的指定天数，如果积分配置信息表已经配置了，就使用积分配置信息表里面的天数
        String scoreEffectTime = Const.DEFAULT_ACTUAL_EFFECT_TIME;
        PageData pd = (PageData) dao.findForObject("PerIntegrationConfigureMapper.selectByexamplePage", perIntegrationConfigure);
        if (null != pd) {
            if (isNumeric(pd.getString("SCORE"))) {
                //注意这里的分数实际代表的时间
                scoreEffectTime = pd.getString("SCORE");
            }
        }
        final String startDate = DateUtil.subDateOnToday(scoreEffectTime);
        //暂时最高支持8千万用户
        /*一共查询多少页，目前Const.MAX_PAGENUM代表4000页，总共可以查询800000000条数据（8千万）*/
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(16);
        Future<Integer> result = pool.schedule(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int classTypeListSize = 0;
                outerLoop:
                for (int nowPage = 1; nowPage < Const.MAX_PAGENUM; nowPage++) {
                    qureyData[0] = getPage(qureyData[0], pageSize, String.valueOf(nowPage));
                    page.setPd(qureyData[0]);
                    final List<PageData> classTypeList0 = (List<PageData>) dao.findForList("PerIntegrationBasisMapper.selectByexampleAllIdPage", page);
                    if (classTypeList0 != null && classTypeList0.size() != 0) {
                        classTypeListSize = classTypeList0.size();
                        final List<PageData> contentlist = null;
                        for (int i = 0; i < classTypeListSize; i++) {
                            final int finalI = i;
                            HashMap tempmap = (HashMap) classTypeList0.get(finalI);
                            userId[0] = tempmap.get("ID") == null ? "" : tempmap.get("ID").toString();
                            score[0] = tempmap.get("SCORE") == null ? "0" : tempmap.get("SCORE").toString();
                            logger.info("针对用户id[" + userId[0] + "]进行积分处置。");
                            //时间扣减逻辑，送进去的值是需要从当前日期扣减的天数
                            Boolean userUpdBegin = autoUpdScoreBasis(nowDate, userId[0],
                                    score[0], logger, startDate, contentlist);
                            if (!userUpdBegin) {
                                logger.info("针对用户id[" + userId[0] + "]进行积分处置失败，请运维人员尽快检查该用户积分变更日志表是否存在残损数据或其他。");
                                logger.info("添加到跑批失败登记信息表。");
                                addPerDayendRunbatch(Const.AUTO_UPDSCORE, nowDate, get32UUID(), userId[0], Const.IF_BATCH_SUCC_NO);
                                logger.info("添加个人跑批失败信息完成, 为不影响整体进度，本次跑批将会继续。");
                            } else {
                                //跑批成功分支
                                //考虑到性能要求，暂时跑批成功的用户除了在积分变更日志信息表中有所体现之外，在跑批信息登记表中不再记录日后如果有需求可以在此添加
                            }

                        }
                    }
                    if (Integer.valueOf(Const.MAX_PAGESIZE) != classTypeListSize) {
                        //当前查询已经到了数据库表的末尾，不可继续下一次查询
                        break outerLoop;
                    }
                    logger.info("当前第" + nowPage + "页数据");
                }
                return null;
            }
        }, 1, TimeUnit.SECONDS);
        pool.shutdown();
        long endtinme = System.currentTimeMillis();
        long costTime = (endtinme - begintime);
        logger.info("当前日期" + nowDate + "日终处理共计消耗时间=" + costTime);
        logger.info("日终处理用户积分信息完毕，用户的所有过期奖励积分已经被扣除。扣除失败的用户的积分信息请根据当前日期[" + nowDate + "]," +
                "在数据库表per_dayend_runbatch中进行查看并跟踪解决");
    }

    /*
     * 添加分页信息公共方法
     *
     */
    public PageData getPage(PageData data, String pageSize, String pageNum) {
        if (StringUtils.isBlank(pageSize) || pageSize.length() >= 10) {
            pageSize = PAGE;
        }
        if (StringUtils.isBlank(pageNum) || "0".equals(pageNum) || pageNum.length() >= 10) {
            pageNum = "1";
        }
        Integer limitbegin = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        Integer limitend = Integer.valueOf(pageSize);
        data.put("limitbegin", limitbegin);
        data.put("limitend", limitend);
        return data;
    }

    /**
     * Title: 用户操作日志新增:
     *
     * @author wangyanlai
     * @version 2019年1月3日 下午4:29:44
     * wangyanlai@cei.gov.cn
     */
    public void insertSysOperatelog(Logger logger) throws Exception {
        SysOperatelog sysOperatelog = new SysOperatelog();
        sysOperatelog.setCreatetime(DateUtil.getTime());
        sysOperatelog.setIp("");
        sysOperatelog.setLogid(UuidUtil.get32UUID());
        String endDt = DateUtil.subDateOnToday("1");
        endDt = endDt.substring(0, 4) + endDt.substring(5, 7) + endDt.substring(8, 10);
        sysOperatelog.setLogname("系统针对用户奖励积分，进行每日失效处理，执行日终跑批操作,处理有效期截止日期【" + endDt + "】的奖励积分信息。");
        sysOperatelog.setLogtype("0");
        sysOperatelog.setOperatetype("SYSTEM");
        sysOperatelog.setModuletype("INTEGRATION");
        sysOperatelog.setOperater("");
        sysOperatelog.setLoggertype("SYSTEM");
        Object updOperaLogNum = null;
        try {
            updOperaLogNum = dao.update("SysOperatelogMapper.insert", sysOperatelog);
        } catch (Exception e) {
            logger.info("插入操作日志表失败");
        }
        if (updOperaLogNum == null) {
            logger.info("插入操作日志表失败");
        } else {
            logger.info("插入操作日志表成功");
        }
    }


    /*
     *author wangyanlai 2018年12月24日
     *wangyanlai@cei.gov.cn
     *检索用户积分配置表实效时间长度，通过扣减失效时间检索变更日志表所有实效分数，汇总之后对分数进行扣除，然后登记日志*/
    public Boolean autoUpdScoreBasis(String nowDate, String userIdObject, String score, Logger logger, String startDate,
                                     List<PageData> contentlist) throws Exception {
        userNum = userNum + 1;
        long endtinme = System.currentTimeMillis();
        long costTime = (endtinme - begintime);
        logger.info("当前正在处理第" + userNum + "个用户, 当前已经消耗时间=" + costTime);
        //初始化事务参数配置
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        //隔离级别为 PROPAGATION_NESTED--如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则进行与PROPAGATION_REQUIRED 类似的操作。
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus status = transactionManager.getTransaction(def);
        PerIntegrationBasis perIntegrationBasis = new PerIntegrationBasis();
        perIntegrationBasis.setId(userIdObject);
        PerIntegrationLog perIntegrationLog = new PerIntegrationLog();
        perIntegrationLog.setUserId(userIdObject);
        startDate = startDate.substring(0, 4) + startDate.substring(5, 7) + startDate.substring(8, 10);
        perIntegrationLog.setCreationDt(startDate);
        //检索有效积分表
        EffectiveIntegral effectiveIntegral = new EffectiveIntegral();
        String endDt = DateUtil.subDateOnToday("1");
        endDt = endDt.substring(0, 4) + endDt.substring(5, 7) + endDt.substring(8, 10);
        effectiveIntegral.setEndDt(endDt);
        effectiveIntegral.setUserId(userIdObject);
        Integer updScore = 0;
        List<EffectiveIntegral> classTypeListBasis = (List<EffectiveIntegral>) dao.findForList("EffectiveIntegralMapper.selectByexample", effectiveIntegral);
        if (classTypeListBasis != null && classTypeListBasis.size() != 0) {
            effectiveIntegral = classTypeListBasis.get(0);
            updScore = (effectiveIntegral.getScore() == null || effectiveIntegral.getScore() == "") ? 0 : Integer.valueOf(effectiveIntegral.getScore());
            effectiveIntegral.setIsEnabled(Const.ISENABLED_NO);
            try {
                //---------------------更新个人有效积分信息表
                dao.update("EffectiveIntegralMapper.updateByPrimaryKey", effectiveIntegral);
            } catch (Exception e) {
                logger.info("有效期过期积分跑批失败");
                //事务回滚
                logger.info("===============事务回滚======================");
                transactionManager.rollback(status);
                //json格式没有被打乱不需要格式化
                e.printStackTrace();
                return false;
            }

        }

        if (0 != updScore) {
            //针对变更的分数进行实际扣除
            perIntegrationBasis.setUpdateTm(DateUtil.getTime());
            perIntegrationBasis.setLastTimeScore(score);
            perIntegrationBasis.setYesterdayScore(score);
            perIntegrationBasis.setTmSmp(DateUtil.getTime());
            if (updScore >= Integer.valueOf(score)) {
                //注意下面两个语句不可以上下换位置
                updScore = Integer.valueOf(score);
                score = "0";
            } else {
                score = String.valueOf(Integer.valueOf(score) - updScore);
            }
            perIntegrationBasis.setScore(score);
            try {
                //---------------------更新个人积分汇总信息表
                dao.update("PerIntegrationBasisMapper.updateByPrimaryKey", perIntegrationBasis);
            } catch (Exception e) {
                transactionManager.rollback(status);
                return false;
            }
        } else {
            logger.info("该用户" + userIdObject + "不存在过期失效的奖励积分");
        }
        //---------------------更新个人积分变更日志信息表
        perIntegrationLog.setScoreType(Const.ACTUAL_EFFECT_TIME);
        perIntegrationLog.setScore(perIntegrationBasis.getScore());
        perIntegrationLog.setUpdScore("-" + String.valueOf(updScore));
        perIntegrationLog.setUserId(perIntegrationBasis.getId());
        perIntegrationLog.setId(UuidUtil.get32UUID());
        perIntegrationLog.setIp("");
        perIntegrationLog.setLogname("该用户于" + startDate + "日的积分已经过期");
        perIntegrationLog.setLastTimeScore(perIntegrationBasis.getLastTimeScore());
        String creationDt = nowDate;
        creationDt = creationDt.substring(0, 4) + creationDt.substring(5, 7) + creationDt.substring(8, 10);
        perIntegrationLog.setCreationDt(creationDt);
        perIntegrationLog.setCreationTm(perIntegrationBasis.getCreationTm());
        perIntegrationLog.setTmSmp(DateUtil.getTime());
//        生成新积分变更日志开始 ---插入变更日志表
        perIntegrationLog.setUpdateTm(perIntegrationBasis.getCreationTm());
        if (!String.valueOf(updScore).equals("0")) {
            //只有发生金额变动时候才会插入日志
            try {
                dao.update("PerIntegrationLogMapper.insertOne", perIntegrationLog);
            } catch (Exception e) {
                logger.info("添加积分变更日志失败");
                //事务回滚
                logger.info("===============事务回滚======================");
                transactionManager.rollback(status);
                //json格式没有被打乱不需要格式化
                e.printStackTrace();
                return false;
            }
        } else {
            logger.info("没有针对积分做出金额变动，无需添加日志。");
        }
        logger.info("添加用户【" + perIntegrationBasis.getId() + "】积分变更日志完成,继续操作下一个用户");
        transactionManager.commit(status);
        return true;
    }

    /*
     *author wangyanlai 2018年12月24日
     *wangyanlai@cei.gov.cn
     *登记跑批信息日志*/
    public void addPerDayendRunbatch(String updType, String nowDate, String uuId, String userId, String ifSucc) {
        PerDayendRunbatch perDayendRunbatch = new PerDayendRunbatch();
        perDayendRunbatch.setBatchType(Const.AUTO_UPDSCORE);
        perDayendRunbatch.setCreationDt(nowDate);
        perDayendRunbatch.setId(get32UUID());
        perDayendRunbatch.setUpdateTm(DateUtil.getTime());
        perDayendRunbatch.setCreationTm(DateUtil.getTime());
        perDayendRunbatch.setUserId(userId);
        perDayendRunbatch.setIsSucc(Const.IF_BATCH_SUCC_YES);
        try {
            dao.update("PerDayendRunbatchMapper.insertOne", perDayendRunbatch);
        } catch (Exception e) {
            logger.info("添加个人跑批失败信息失败");
        }
    }

    /*
     * 获取登录ip
     */
    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
