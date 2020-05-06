package com.ihrm.company.controller;

import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.company.service.CompanyService;
import com.ihrm.domain.company.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    //@RequestBody 注解可以将接受的json对象转化为java对象接受
    //保存企业
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Result add(@RequestBody Company company){

        companyService.add(company);

        return new Result(ResultCode.SUCCESS);
    }

    //根据id更新企业  @PathVariable(value = "id") String id 表示接收地址参数
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable(value = "id") String id, @RequestBody Company company){

        //@RequestBody Company company  这个参数company接收的是页面填写的数据不包含id参数，所以先设置一下company属性
        company.setId(id);
        companyService.update(company);

        return new Result(ResultCode.SUCCESS);

    }

    //根据id删除企业
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Result delete(@PathVariable(value = "id") String id) {

        companyService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }

    //根据id查询企业
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id){

        Company company = companyService.findById(id);

        //return new Result(ResultCode.SUCCESS, company);
        Result result = new Result(ResultCode.SUCCESS);
        result.setData(company);
        return result;

    }

    //查询全部企业列表
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Result findAll(){

        List<Company> list = companyService.findAll();
        Result result = new Result(ResultCode.SUCCESS);
        result.setData(list);
        return result;
    }


}
