package org.spring.springboot.dao;

import base.zw.controller.BaseController;
import javafx.scene.input.DataFormat;
import net.sf.json.JSONObject;
import org.spring.springboot.domain.PerIntegrationBasis;
import org.spring.springboot.zw.util.Const;
import org.spring.springboot.zw.util.DESUtil;
import org.spring.springboot.zw.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.rmi.transport.Connection;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.spring.springboot.zw.util.DESUtil.aesDecrypt;
import static org.spring.springboot.zw.util.DESUtil.aesEncrypt;
import static org.spring.springboot.zw.util.DESUtil.encryptBasedDes;
import static org.spring.springboot.zw.util.DateUtil.getAfterDayDate;

@Component
@Configurable
@EnableScheduling

public class test {
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    private final int AAA = 11;
    public static int i = 0;

    public static void main(String[] args) {
        String aaa = "353601eea8764e61bc94eac174b7b9f4";
        /*00001————> wt5YqDq06SM=
                00002————>sjQJ/SMWFT8=
                00003————>shzKJAmLy4M=
                00004————>letGAYQk8pQ=
                00005————>P1VBdcMXXis=
                00006————>UuUF2xLFBbk=
                00007————>ViaNFomId2Y=
                00008————>inn6CmPV5sA=
                00009————>JhYRv87WZJQ=
                00010————>2ek4OsdvxrM=
                00011————>26nhwHLJRLk=
                00012————>HjBNpR+i8k8=
                00013————>yOzkQprH7To=*/
        String content = "1122-zzii122.11kkqqlloo";
        //String content = "123456";  //
        System.out.println("加密前：" + content);
        System.out.println("加密密钥和解密密钥：" + Const.ALLENCRYPTCODE);
        try{
        String encrypt = aesEncrypt(content, Const.ALLENCRYPTCODE);
        System.out.println(encrypt.length()+":加密后：" + encrypt);
        encrypt = "zFoYX4wtpZrLMXYHRhvD3g==";
        String decrypt = aesDecrypt(encrypt, Const.ALLENCRYPTCODE);
        System.out.println("解密后：" + decrypt);
        }catch (Exception e){

        }

//
                /*00001————> wt5YqDq06SM=
                00002————>sjQJ/SMWFT8=
                00003————>shzKJAmLy4M=
                00004————>letGAYQk8pQ=
                00005————>P1VBdcMXXis=
                00006————>UuUF2xLFBbk=
                00007————>ViaNFomId2Y=
                00008————>inn6CmPV5sA=
                00009————>JhYRv87WZJQ=
                00010————>2ek4OsdvxrM=
                00011————>26nhwHLJRLk=
                00012————>HjBNpR+i8k8=
                00013————>yOzkQprH7To=*/

              /*00000————> KKK/TZbJP2Y=
                10000————>5O4blFHH5T4=
                20000————>RxTSbaEGyFs=
                30000————>xOooIe4nuuM=
                40000————>rFBThg9ZD5Y=
                50000————>Z/oZ8sVGrDM=
                60000————>CJekBFwZOtw=
                70000————>XxwBGcAsa/4=
                80000————>bRfYlXo9krA=*/
//        String[] qryType={"12121","12121","12121",
//                "123123411423","12312341423"};
////        System.out.println(Arrays.asList(qryType));//先将数组转换为List
//        System.out.println(Arrays.asList(qryType).contains(aaa));
//        Set<String> set = new HashSet<>(Arrays.asList(str));
//        System.out.println(set.contains(s));
    }


}
