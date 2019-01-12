/**
 * <p>Description: </p>
 * <p>Title: UserServiceImpl.java</p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: cei</p>
 * <p>@author zhaowei</p>
 * <p>@version 2018年4月27日 下午6:25:34</p>
 */
package org.spring.springboot.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.spring.springboot.dao.APPDao;
import org.spring.springboot.dao.DaoSupport;
import org.spring.springboot.domain.PageData;
import org.spring.springboot.domain.User;
import org.spring.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * Title: UserServiceImpl  
 * Description: 
 * @author zhaowei
 * @version 2018年4月27日 下午6:25:34
 */
//@Repository
@Service
public class UserServiceImpl implements UserService {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /*
     * 通过id获取数据
     */
    @Override
    public PageData findByUiId(PageData pd) {
        PageData temppd = new PageData();
        try {
            temppd = (PageData) dao.findForObject("UserXMapper.findByUiId", pd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temppd;
    }

    /*
     * 通过loginname获取数据
     */
    @Override
    public PageData findByUId(PageData pd) {
        PageData temppd = new PageData();
        try {
            temppd = (PageData) dao.findForObject("UserXMapper.findByUId", pd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temppd;
    }

    /*
     * 登录判断
     */
    @Override
    public PageData getUserByNameAndPwd(PageData pd) {
        PageData temppd = new PageData();
        try {
            temppd = (PageData) dao.findForObject("UserXMapper.getUserInfo", pd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temppd;
    }

    /*
     * 保存用户登陆信息
     */
    @Override
    public void saveULogin(PageData pd) {
        try {
            dao.save("UserXMapper.saveULogin", pd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改页面--前端使用
     * @throws Exception
     * @返回值：void
     * @创建时间：2018年3月20日 下午1:22:09
     */
    @Override
    public void editUserPro(PageData pd) {
        try {
            dao.update("UserXAdminMapper.editUserPro", pd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
