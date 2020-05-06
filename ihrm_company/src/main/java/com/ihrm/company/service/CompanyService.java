package com.ihrm.company.service;

import com.ihrm.common.utils.IdWorker;
import com.ihrm.company.dao.CompanyDao;
import com.ihrm.domain.company.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    @Autowired
    private CompanyDao companyDao;

    @Autowired
    private IdWorker idWorker;

    //保存企业
    /**
     * 1.配置idworker到当前工程当中
     * 在主启动类中使用注解@Bean注入bean
     * 2.在service中注入idworker
     * 3.通过idworker生成id
     * 4.保存企业
     */
    public void add(Company company){
        String id = idWorker.nextId() + "";
        company.setId(id);
        //是否被审核 0 表示未审核
        company.setAuditState("0");
        //是否被激活,1  表示已激活
        company.setState(1);
        companyDao.save(company);

    }

    //更新企业,更新方法都是根据id先查询出数据库中原有的数据，然后根据新数据更新
    public void update(Company company){

        Company tmpCompany = companyDao.findById(company.getId()).get();
        tmpCompany.setName(company.getName());
        tmpCompany.setCompanyPhone(company.getCompanyPhone());

        companyDao.save(tmpCompany);
    }

    //删除企业
    public void deleteById(String id){
        companyDao.deleteById(id);
    }

    //根据id查询企业
    public Company findById(String id){

        return companyDao.findById(id).get();
    }

    //查询企业列表
    public List<Company> findAll(){

        return companyDao.findAll();
    }
}
