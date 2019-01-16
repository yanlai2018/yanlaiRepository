package org.spring.springboot.zw.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.spring.springboot.dao.DaoSupport;
import org.spring.springboot.domain.*;
import org.spring.springboot.service.APPService;
import org.spring.springboot.service.IntegrationQryService;
import org.spring.springboot.service.IntegrationUpdService;
import org.spring.springboot.service.UserService;
import org.spring.springboot.service.impl.IntegrationUpdServiceImpl;
import org.spring.springboot.zw.util.Const;
import org.spring.springboot.zw.util.DESUtil;
import org.spring.springboot.zw.util.DateUtil;
import org.spring.springboot.zw.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;

import base.zw.controller.BaseController;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.TransactionManager;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.Statement;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.logging.Logger;


import static org.apache.shiro.web.util.WebUtils.getRequest;
import static org.codehaus.groovy.runtime.StringGroovyMethods.isInteger;
import static org.spring.springboot.zw.util.Const.PAGE;
import static org.spring.springboot.zw.util.DESUtil.decryptBasedDes;
import static org.spring.springboot.zw.util.DESUtil.encryptBasedDes;

/**
 * 接口 Controller 实现 Restful HTTP 服务
 * <p>
 * Created by zhaowei on 20180425.
 */

/**
 * Title: AppController Description:
 *
 * @author zhaowei
 * @version 2018年4月30日 下午4:53:32
 */
@RestController
@RequestMapping(value = "/app")
public class AppController extends BaseController {
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    @Autowired
    private APPService appService;
    @Autowired
    private UserService userService;
    @Autowired
    private IntegrationQryService integrationQryService;
    @Autowired
    private IntegrationUpdService integrationUpdService;


    /**
     * @param reqMap
     * @return
     * @author zhaowei
     * @version 2018年4月26日 上午8:43:05
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody Map<String, Object> reqMap) {
        ReturnData map = new ReturnData();
        PageData pd = new PageData();
        String code = "xzxzxzxz";
        try {
            String OLDPASSWORD = DESUtil.encryptDES(reqMap.get("password").toString(), code);
            logger.info("OLDPASSWORD---" + OLDPASSWORD + "USERNAME---" + reqMap.get("loginname") + "---PASSWORD----"
                    + reqMap.get("password"));
            if ((StringUtil.isEmpty(reqMap.get("loginname")) == false)
                    || (StringUtil.isEmpty(reqMap.get("password")) == false)) {
                // 0代表用户名或密码错误,1代表登录成功,2帐号停用,3登录失败
                map.setCode(Const.EMPTY_CODE);
                logger.info("---returnStatus---0---");
            } else {
                pd.put("USERNAME", reqMap.get("loginname"));
                String PASSWORD = DESUtil.decryptDES(reqMap.get("password").toString(), code);
                // 密码加密
                String passwd = new SimpleHash("SHA-1", reqMap.get("loginname").toString(), PASSWORD).toString();
                pd.put("PASSWORD", passwd);
                logger.info("OLDPASSWORD---" + OLDPASSWORD + "PASSWORD---" + PASSWORD + "---passwd---" + passwd);
                pd = userService.getUserByNameAndPwd(pd);
                if (pd != null) {
                    pd.put("ID", this.get32UUID());
                    pd.put("USERID", pd.get("USER_ID"));
                    pd.put("CREATETIME", DateUtil.getTime().toString());
                    pd.put("USERNAME", reqMap.get("loginname"));
                    pd.put("IP", getRemortIP());
                    userService.saveULogin(pd);
                    List<PageData> contentlist = new ArrayList<PageData>();
                    PageData temppd = new PageData();
                    temppd.put(userId, pd.getString("USER_ID"));
                    temppd.put("username", pd.getString("NAME"));
                    temppd.put("orgname", pd.getString("ORGNAME"));
                    temppd.put("orgid", pd.getString("ORGID"));
                    temppd.put("loginname", reqMap.get("loginname").toString());
                    temppd.put("status", "0");
                    temppd.put("nickname", pd.getString("NAME"));
                    temppd.put("phone", pd.getString("PHONE"));
                    temppd.put("mail", pd.getString("EMAIL"));
                    contentlist.add(temppd);
                    // 验证成功
                    map.setCode(Const.SUCCESS_CODE);
                    map.setContentlist(contentlist);
                } else {
                    // 用户名或密码有误
                    map.setCode(Const.LOGINFAILURE_CODE);
                }
            }
        } catch (Exception e) {
            // 系统错误
            map.setCode(Const.FAILURE_CODE);
        }
        // 输出返回结果
        String jsonReturn = JSON.toJSONString(map);
        logger.info("login---jsonObject.toString()---" + jsonReturn + "---");
        return jsonReturn;
    }

    /**
     * 查询年份或者课程分类信息
     *
     * @param reqMap
     * @return
     * @author zhaowei
     * @version 2018年4月30日 上午10:09:20
     */
    @RequestMapping(value = "/queryAlbumType", method = RequestMethod.POST)
    public String queryAlbumType(@RequestBody Map<String, Object> reqMap) {
        ReturnData map = new ReturnData();
        PageData pd = new PageData();
        try {
            if ((StringUtil.isEmpty(reqMap.get("type")) == false)) {
                map.setCode(Const.EMPTY_CODE);
                logger.info("queryAlbumType---returnStatus---0---");
            } else {
                if ("0".equals(reqMap.get("type").toString())) {
                    // 查询所有的二级分类，即level为1的所有数据
                    Page page = new Page();
                    page.setPd(pd);
                    // 查询课程分类
                    List<PageData> classTypeList0 = appService.typeLevelONEList(page);
                    List<PageData> contentlist = new ArrayList<PageData>();
                    String fid = "";
                    String ids = "";
                    Map map2 = new HashMap();
                    List list1 = new ArrayList();
                    String classxmlstr = "";
                    String classxmlstr1 = "";
                    String jsonReturnMap1 = "";
                    Map<String, String> returnMap1 = new HashMap<String, String>();
                    if (classTypeList0 != null && classTypeList0.size() > 0) {
                        for (int i = 0; i < classTypeList0.size(); i++) {
                            HashMap tempmap = (HashMap) classTypeList0.get(i);
                            PageData classification = new PageData();
                            String id = tempmap.get("id") == null ? "" : (String) tempmap.get("id");
                            //暂时没有这个字段
                            String name = tempmap.get("name") == null ? "" : (String) tempmap.get("name");
                            //暂时没有这个字段
                            String parentid = tempmap.get("parentid") == null ? "" : (String) tempmap.get("parentid");
                            classification.put("id", id);
                            classification.put("name", name);
                            classification.put("parentid", parentid);
                            contentlist.add(classification);
                            map.setCode(Const.SUCCESS_CODE);
                            map.setContentlist(contentlist);
                        }
                    } else {
                        map.setCode(Const.NODATA);
                        logger.info("queryAlbumType---returnStatus---1---");
                    }
                }
            }
        } catch (Exception e) {
            // 系统错误
            map.setCode(Const.FAILURE_CODE);
        }
        // 输出返回结果
        String jsonReturn = JSON.toJSONString(map);
        logger.info("queryAlbumType---jsonObject.toString()---" + jsonReturn + "---");
        return jsonReturn;
    }

    /**
     * 查询通知公告
     */
    @RequestMapping(value = "/queryMessage", method = RequestMethod.POST)
    public String queryMessage(@RequestBody Map<String, Object> reqMap) {
        ReturnData returndata = new ReturnData();
        PageData pd = new PageData();
        List<PageData> contentlist = new ArrayList<PageData>();
        try {
            if ((StringUtil.isEmpty(reqMap.get("type")) == false) || (StringUtil.isEmpty(reqMap.get(userId)) == false)
                    || (StringUtil.isEmpty(reqMap.get("index")) == false)) {
                // 0代表用户名或密码错误,1代表登录成功,2帐号停用,3登录失败
                returndata.setCode(Const.EMPTY_CODE);
                logger.info("queryMessage---returnStatus---0---");
            } else {
                int index = Integer.parseInt(reqMap.get("index") == null ? "1" : reqMap.get("index").toString());
                Page page = new Page();
                Integer limitbegin = (index - 1) * Integer.parseInt(Const.PAGE);
                Integer limitend = index * Integer.parseInt(Const.PAGE);
                pd.put("limitbegin", limitbegin);
                pd.put("limitend", limitend);
                // 查询通知公告
                if ("0".equals(reqMap.get("type").toString())) {
                    page.setPd(pd);
                    List<PageData> list = appService.noticelist(page);
                    if (list != null && list.size() > 0) {
                        String webapppath = Const.NGINXIP;
                        String ftppath = Const.FTPROOTPAHT;
                        for (int i = 0; i < list.size(); i++) {
                            PageData temppd = new PageData();
                            HashMap map = (HashMap) list.get(i);
                            String id = map.get("id") == null ? "0" : (String) map.get("id");
                            String title = map.get("title") == null ? "" : (String) map.get("title");
                            String istop = map.get("istop") == null ? "" : (String) map.get("istop");
                            String url = map.get("url") == null ? "" : (String) map.get("url");
                            String addtime = map.get("addtime") == null ? "" : (String) map.get("addtime");
                            temppd.put("id", id);
                            temppd.put("title", title);
                            temppd.put("istop", istop);
                            temppd.put("url", webapppath + ftppath + url);
                            temppd.put("addtime", addtime);
                            contentlist.add(temppd);
                        }
                        returndata.setCode(Const.SUCCESS_CODE);
                        returndata.setContentlist(contentlist);
                    } else {
                        returndata.setCode(Const.NODATA);
                        logger.info("queryMessage---returnStatus---NODATA---");
                    }
                } else {
                    returndata.setCode(Const.NODATA);
                    logger.info("queryMessage---returnStatus---NODATA---");
                }
            }
        } catch (Exception e) {
            returndata.setCode(Const.FAILURE_CODE); // 系统错误
        }
        // 输出返回结果
        String jsonReturn = JSON.toJSONString(returndata);
        logger.info("queryMessage---jsonObject.toString()---" + jsonReturn + "---");
        return jsonReturn;
    }

    /**
     * 积分管理系统登录
     *
     * @param reqMap
     * @return
     * @author wangyanlai
     * @version 2019年1月26日 上午8:43:05
     */
    @RequestMapping(value = "/loginIn", method = RequestMethod.POST)
    public String loginIn(@RequestBody Map<String, Object> reqMap) {
        ReturnData map = new ReturnData();
        PageData pd = new PageData();
        Object passWd = reqMap.get("password");
        Object userNm = reqMap.get("username");
        if (!StringUtil.isEmpty(passWd) || !StringUtil.isEmpty(userNm)) {
            map.setCode(Const.LOGINFAILURE_CODE);
            String jsonReturn = JSON.toJSONString(map);
            logger.info("login---jsonObject.toString()---" + jsonReturn + "---");
            return jsonReturn;
        }

        HttpServletRequest request = getRequest();
        return userService.loginIn(reqMap, logger, request);
    }

    /**
     * 积分管理系统登录
     *
     * @param reqMap
     * @return
     * @author wangyanlai
     * @version 2019年1月26日 上午8:43:05
     */
    @RequestMapping(value = "/loginOut", method = RequestMethod.POST)
    public String loginOut(@RequestBody Map<String, Object> reqMap) {
        ReturnData map = new ReturnData();
        PageData pd = new PageData();
        HttpServletRequest request = getRequest();
        return userService.loginOut(reqMap, logger, request);
    }

    //新增用户基本信息信息
    @RequestMapping(value = "/addUser", method = RequestMethod.POST, produces = "text/html; charset=utf-8")
    public String addUser(@RequestBody Map<String, Object> reqMap) {
        ReturnData returndata = new ReturnData();
        ReturnData map = new ReturnData();
        returndata.setCode(Const.FAILURE_CODE);
        Object passWd = reqMap.get("password");
        Object userNm = reqMap.get("username");
        if (!StringUtil.isEmpty(passWd) || !StringUtil.isEmpty(userNm)) {
            map.setCode(Const.LOGINFAILURE_CODE);
            String jsonReturn = JSON.toJSONString(map);
            logger.info("login---jsonObject.toString()---" + jsonReturn + "---");
            return jsonReturn;
        }
        HttpServletRequest request = getRequest();
        return userService.addUser(reqMap, logger, request);
    }

    //变更用户基本信息信息
    @RequestMapping(value = "/updUser", method = RequestMethod.POST, produces = "text/html; charset=utf-8")
    public String updUser(@RequestBody Map<String, Object> reqMap) {
        ReturnData map = new ReturnData();
        ReturnData returndata = new ReturnData();
        HttpServletRequest request = getRequest();
        Object passWd = reqMap.get("password");
        Object newpassWd = reqMap.get("newpassword");
        if (!StringUtil.isEmpty(passWd) || !StringUtil.isEmpty(newpassWd)) {
            map.setCode(Const.LOGINFAILURE_CODE);
            String jsonReturn = JSON.toJSONString(map);
            logger.info("login---jsonObject.toString()---" + jsonReturn + "---");
            return jsonReturn;
        }
        return userService.updUser(reqMap, logger, request);
    }

    //删除用户基本信息信息
    @RequestMapping(value = "/deleteUser", method = RequestMethod.POST, produces = "text/html; charset=utf-8")
    public String deleteUser(@RequestBody Map<String, Object> reqMap) {
        ReturnData map = new ReturnData();
        ReturnData returndata = new ReturnData();
        Object passWd = reqMap.get("password");
        Object userNm = reqMap.get("username");
        if (!StringUtil.isEmpty(passWd) || !StringUtil.isEmpty(userNm)) {
            map.setCode(Const.LOGINFAILURE_CODE);
            String jsonReturn = JSON.toJSONString(map);
            logger.info("login---jsonObject.toString()---" + jsonReturn + "---");
            return jsonReturn;
        }
        HttpServletRequest request = getRequest();
        return userService.deleteUser(reqMap, logger, request);
    }


    //新增积分配置信息
    @RequestMapping(value = "/addConfigure", method = RequestMethod.POST, produces = "text/html; charset=utf-8")
    public String addConfigure(@RequestBody Map<String, Object> reqMap) {
        ReturnData returndata = new ReturnData();
        returndata.setCode(Const.IRREGULAR_PARAMETERS);
        logger.info("scoretype======" + reqMap.get("scoretype"));
        if (org.springframework.util.StringUtils.isEmpty((reqMap.get("scoretype")))) {
            return JSON.toJSONString(returndata);
        }
        HttpServletRequest request = getRequest();
        return integrationUpdService.addConfigure(reqMap, logger, request);
    }

    //变更积分配置信息
    @RequestMapping(value = "/updConfigure", method = RequestMethod.POST, produces = "text/html; charset=utf-8")
    public String updConfigure(@RequestBody Map<String, Object> reqMap) {
        ReturnData returndata = new ReturnData();
        returndata.setCode(Const.IRREGULAR_PARAMETERS);
        if (org.springframework.util.StringUtils.isEmpty((reqMap.get("id")))) {
            return JSON.toJSONString(returndata);
        }
        HttpServletRequest request = getRequest();
        return integrationUpdService.updConfigure(reqMap, logger, request);
    }

    //删除积分配置信息
    @RequestMapping(value = "/deleteConfigure", method = RequestMethod.POST, produces = "text/html; charset=utf-8")
    public String deleteConfigure(@RequestBody Map<String, Object> reqMap) {
        ReturnData returndata = new ReturnData();
        returndata.setCode(Const.IRREGULAR_PARAMETERS);
        if (org.springframework.util.StringUtils.isEmpty((reqMap.get("id")))) {
            return JSON.toJSONString(returndata);
        }
        HttpServletRequest request = getRequest();
        return integrationUpdService.deleteConfigure(reqMap, logger, request);
    }

    //查询积分配置信息
    @RequestMapping(value = "/integralConfigure", method = RequestMethod.POST, produces = "text/html; charset=utf-8")
    public String queryConfigure(@RequestBody Map<String, Object> reqMap) {
        ReturnData returndata = new ReturnData();
        returndata.setCode(Const.IRREGULAR_PARAMETERS);
        HttpServletRequest request = getRequest();
        return integrationQryService.queryConfigure(reqMap, logger, request);
    }

    /**
     * 查询积分信息接口(依据查询类型查询的信息分别如下)
     * QUERYSCORE_PER = "00000"; // 表示查询个人汇总积分（单条）
     * QUERYSCORE_LOG = "10000"; // 表示查询个人积分变更日志列表
     * QUERYSCORE_COURSE = "20000"; // 表示查询个人课程积分列表
     * QUERYSCORE_QUESTION = "30000"; // 表示个人问题积分列表
     * QUERYSCORE_TRAINING  = "40000"; // 表示个人培训班积分列表
     * QUERYSCORE_EXCHANGE = "50000"; // 表示查询个人兑换积分列表
     * QUERYSCORE_QUESTIONNAIRE  = "60000"; // 表示个人问卷调查积分列表
     * QUERYSCORE_TASK = "70000"; // 表示查询个人作业积分列表
     *
     * @param reqMap
     * @return
     * @author 王燕来
     * @version 2018年12月17日 上午10:09:20
     */
    private String userId = "userid";
    private String qryType = "qrytype";
    private String updType = "updtype";
    private String key = "key";

    //userid 用户编号
    //qrytype 查询类别
    //updtype 变更类别
    @RequestMapping(value = "/queryScore", method = RequestMethod.POST, produces = "text/html; charset=utf-8")
    public String queryScore(@RequestBody Map<String, Object> reqMap) {
        ReturnData returndata = new ReturnData();
        HttpServletRequest request = getRequest();
        /*如果外部送进来key字段，有值，并且值与免除session校验的特征值一致，就可以跳过session验证。*/
        if (!(!org.springframework.util.StringUtils.isEmpty(reqMap.get(key)) && Const.NO_SESSIONOUTTIMEKEY.equals(((Object) reqMap.get(key)).toString()))) {
            if (!sessionValidate()) {
                returndata.setCode(Const.LOGIN_TIME_OUT);
                return JSON.toJSONString(returndata);
            }
        }

        returndata.setCode(Const.IRREGULAR_PARAMETERS);
        if (org.springframework.util.StringUtils.isEmpty((reqMap.get(qryType)))) {
            return JSON.toJSONString(returndata);
        }
        try {
            //当不是查询日志时，用户号必传
            if (!Const.QUERYSCORE_LOG.equals(decryptBasedDes(((Object) reqMap.get(qryType)).toString()))) {
                if (org.springframework.util.StringUtils.isEmpty((reqMap.get(userId)))) {
                    return JSON.toJSONString(returndata);
                }
            }
        } catch (Exception e) {
            return JSON.toJSONString(returndata);
        }
        return integrationQryService.qryIntegration(reqMap, logger, request);
    }

    /**
     * 变更积分信息接口
     * <p>  其中接口传入的关键的变更类型（updtype）字段值域包含在如下区域【
     * LOGIN_UPDSCORE = "00001"; // 表示用户登陆获取积分
     * LEARN_UPDSCORE = "00002"; // 表示用户通过课程学习获取积分
     * COMMENT_UPDSCORE = "00003"; // 表示用户通过课程评论获取积分
     * HOSTILITY_COMMENT_UPDSCORE = "00004"; // 表示用户通过恶意评论损失积分
     * INVESTIGATION_UPDSCORE  = "00005"; // 表示用户通过问卷调查获取积分
     * SHARE_UPDSCORE = "00006"; // 表示用户通过分享课程获取积分
     * TASK_UPDSCORE = "00007"; // 表示用户通过作业被指定为精华获取积分
     * APPRECIATE_UPDSCORE = "00008"; // 表示用户通过问题点赞获取积分
     * MAX_EXCHANGE_PER = "00009"; // 表示最大兑换人数
     * MAX_EXCHANGE_SCORE  = "00010"; // 表示个人最大兑换金额
     * ACTUAL_EFFECT_TIME  = "00011"; // 表示奖励金额实效时间】
     * author wangyanlai by 2018.12.21
     * email wangyanlai@cei.gov.cn
     */
    @RequestMapping(value = "/updScore", method = RequestMethod.POST)
    public String updScore(@RequestBody Map<String, Object> reqMap) {
        HttpServletRequest request = getRequest();
        ReturnData returndata = new ReturnData();
        /*如果外部送进来key字段，有值，并且值与免除session校验的特征值一致，就可以跳过session验证。*/
        if (!(!org.springframework.util.StringUtils.isEmpty(reqMap.get(key)) && Const.NO_SESSIONOUTTIMEKEY.equals(((Object) reqMap.get(key)).toString()))) {
            if (!sessionValidate()) {
                returndata.setCode(Const.LOGIN_TIME_OUT);
                return JSON.toJSONString(returndata);
            }
        }
        returndata.setCode(Const.IRREGULAR_PARAMETERS);
        if (StringUtils.isBlank(((Object) reqMap.get(userId)).toString()) || StringUtils.isBlank(((Object) reqMap.get(updType)).toString())) {
            return JSON.toJSONString(returndata);
        }
        return integrationUpdService.updIntegration(reqMap, logger, request);
    }

    /*检验session是否超时*/
    public boolean sessionValidate() {
        HttpServletRequest request = getRequest();
        String userId = (String) request.getSession().getAttribute("sessionUserId");
        logger.info("sessionUserId==" + userId);
        if (null == userId || "".equals(userId)) {
            logger.info("session超时退出");
            return false;
        }
        return true;
    }

}
