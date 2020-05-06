package com.ihrm.company.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.company.service.CompanyService;
import com.ihrm.company.service.DepartmentService;
import com.ihrm.domain.company.Company;
import com.ihrm.domain.company.Department;
import com.ihrm.domain.company.response.DeptListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;

//1.解决跨域
@CrossOrigin
@RestController
//2.设置父路径
@RequestMapping(value = "/company")
public class DepartmentController extends BaseController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private CompanyService companyService;

    //保存部门
    @RequestMapping(value = "/department", method = RequestMethod.POST)
    public Result save(@RequestBody Department department){

        //设置保存的企业id
        //String companyId = "1";

        //这里的companyId使用的是父类的属性
        department.setCompanyId(companyId);

        departmentService.save(department);

        return new Result(ResultCode.SUCCESS);
    }

    //查询企业的部门列表
    @RequestMapping(value = "/department", method = RequestMethod.GET)
    public Result findAll(){

        //根据企业id查询这个企业的所有部门
        String companyId = "1";
        Company company = companyService.findById(companyId);

        List<Department> departmentList = departmentService.findAll(companyId);

        //返回的数据重新构造一个新的实体类，用来封装结果
        DeptListResult deptListResult = new DeptListResult(company, departmentList);


        return new Result(ResultCode.SUCCESS, deptListResult);
    }

    //根据id查询部门
    @RequestMapping(value = "/department/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id){

        Department department = departmentService.findById(id);

        return new Result(ResultCode.SUCCESS, department);

    }

    //修改部门
    @RequestMapping(value = "/department/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable(value = "id") String id, @RequestBody Department department){

        //因为接收的dempartment是表单信息，不带有id，需要根据传入的部门id，将该属性设置到department中，然后根据这个id去数据库查询，修改更改的数据
        department.setId(id);

        departmentService.update(department);

        return new Result(ResultCode.SUCCESS);

    }

    //根据id删除部门
    @RequestMapping(value = "/department/{id}", method = RequestMethod.DELETE)
    public Result delete(@PathVariable(value = "id") String id){

        departmentService.delete(id);

        return new Result(ResultCode.SUCCESS);
    }

    //根据部门标识和企业id查询部门信息，这个方法是在导入excel批量添加用户的时候调用
    @RequestMapping(value = "/department/search", method = RequestMethod.POST)
    public Department findByCode(@RequestParam(value = "code") String code,
                                 @RequestParam(value = "companyId")String companyId){

        Department department = departmentService.findByCode(code, companyId);

        return department;
    }

}
