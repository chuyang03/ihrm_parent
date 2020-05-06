package com.ihrm.common.controller;

import com.ihrm.domain.system.response.ProfileResult;
import io.jsonwebtoken.Claims;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseController {

    protected HttpServletRequest request;
    protected HttpServletResponse response;

    protected String companyId;
    protected String companyName;

    protected Claims claims;

    //使用jwt方式获取企业id和企业名字
//    //使用这个注解可以使得这个方法在进入控制器之前执行
//    @ModelAttribute
//    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response){
//
//        this.request = request;
//        this.response = response;
//
//        //直接从请求域中获取claims
//        Claims claims = (Claims) request.getAttribute("user_claims");
//
//        if (claims != null){
//            this.claims = claims;
//
//            //因为之前token的内容自定义了两个claims属性，就是companyId和companyName，所以现在直接获取就可以了
//            this.companyId = (String)claims.get("companyId");
//            this.companyName = (String)claims.get("companyName");
//        }
//
//    }


    //使用shiro获取
    @ModelAttribute
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {

        this.request = request;
        this.response = response;

        //获取session中的安全数据
        Subject subject = SecurityUtils.getSubject();
        //获取安全数据集合
        PrincipalCollection principals = subject.getPrincipals();
        if (principals != null && !principals.isEmpty()){

            ProfileResult result = (ProfileResult) principals.getPrimaryPrincipal();

            this.companyId = result.getCompanyId();
            this.companyName = result.getCompany();
        }
    }
}
