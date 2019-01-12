package org.spring.springboot.zw.controller;

import base.zw.controller.BaseController;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.springboot.dao.DaoSupport;
import org.spring.springboot.domain.Page;
import org.spring.springboot.domain.PageData;
import org.spring.springboot.domain.ReturnData;
import org.spring.springboot.service.APPService;
import org.spring.springboot.service.IntegrationQryService;
import org.spring.springboot.service.IntegrationUpdService;
import org.spring.springboot.service.UserService;
import org.spring.springboot.zw.util.Const;
import org.spring.springboot.zw.util.DESUtil;
import org.spring.springboot.zw.util.DateUtil;
import org.spring.springboot.zw.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * Title: AppController Description:
 *
 * @author wangyanlai
 * @version 2019年1月20日 下午4:53:32
 * 专用于前台页面的返回
 */
@org.springframework.stereotype.Controller
public class Controller extends BaseController {
    //系统主页
    @RequestMapping(value = "/")
    public String index(ModelMap map) {
        return "/login/index";
    }

    @RequestMapping(value = "/index")
    public String welcome(ModelMap map) {
        return "/login/index";
    }
    //系统菜单
    @RequestMapping(value = "/menu")
    public String menu(ModelMap map) {
        return "/login/menu";
    }

    //积分汇总列表
    @RequestMapping(value = "/integrationBasis")
    public String configureBasis(ModelMap map) {
        return "/login/integral_basis";
    }

    //积分课程列表
    @RequestMapping(value = "/integrationCourse")
    public String integrationCourse(ModelMap map) {
        return "/login/integral_course";
    }

    //积分培训列表
    @RequestMapping(value = "/integrationTraining")
    public String integrationTraining(ModelMap map) {
        return "/login/integral_training";
    }

    //积分积分兑换列表
    @RequestMapping(value = "/integrationExchange")
    public String integrationExchange(ModelMap map) {
        return "/login/integral_exchange";
    }

    //积分作业列表
    @RequestMapping(value = "/integrationTask")
    public String integrationTask(ModelMap map) {
        return "/login/integral_task";
    }

    //积分跑批列表
    @RequestMapping(value = "/integrationDayend")
    public String integrationDayend(ModelMap map) {
        return "/login/integral_dayend";
    }

    //提交问卷调查积分列表
    @RequestMapping(value = "/integrationQuestionnaire")
    public String integrationQuestionnaire(ModelMap map) {
        return "/login/integral_questionnaire";
    }

    //提交问题积分列表
    @RequestMapping(value = "/integrationQuestion")
    public String integrationQuestion(ModelMap map) {
        return "/login/integral_question";
    }

}
