package com.ihrm.common.interceptor;

import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * 自定义拦截器
 *
 * preHandle：
 *      1.简化获取token数据的代码编写
 *          统一的用户权限校验（是否登陆）
 *      2.判断用户是否具有当前访问接口的权限
 */
@Component
public class JwtInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 进入到控制器方法之前执行的方法
     *      这个方法又返回值，
     *      返回值为true：表示继续执行控制器方法
     *      返回值为false：表示拦截
     *
     */

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取请求头中的数据
        String authorization = request.getHeader("Authorization");

        if (!StringUtils.isEmpty(authorization) && authorization.startsWith("Bearer")){

            String token = authorization.replace("Bearer ", "");

            //解析token
            Claims claims = jwtUtils.parseJwt(token);
            /**
             * 将token中用户拥有的api权限字符串解析出来，然后将其与访问接口上注解的name属性进行比较，name属性就设置为api的标识名
             */
            if (claims != null){

                String apis = (String) claims.get("apis");
                //通过这个类可以获取接口上的RequestMapping注解
                HandlerMethod handlerMethod = (HandlerMethod)handler;

                RequestMapping annotation = handlerMethod.getMethodAnnotation(RequestMapping.class);
                //获取当前请求接口中的name属性
                String name = annotation.name();

                //判断当前用户是否具有请求权限
                if (apis.contains(name)){
                    request.setAttribute("user_claims", claims);

                    return true;    //返回true表示继续执行控制器方法

                }else {

                    //如果该用户没有访问该接口的权限，则抛出没有权限的信息
                    throw new CommonException(ResultCode.UNAUTHORISE);
                }
            }

        }
        throw new CommonException(ResultCode.UNAUTHENTICATED);
    }



}
