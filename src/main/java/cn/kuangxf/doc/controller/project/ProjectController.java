/*
 * @(#)ProjectController.java        1.0 2018年3月19日
 *
 *
 */

package cn.kuangxf.doc.controller.project;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.kuangxf.doc.bean.JavaGitProjectBean;
import cn.kuangxf.doc.controller.AjaxResult;
import cn.kuangxf.doc.dao.domain.JavaGitProject;
import cn.kuangxf.doc.dao.manager.JavaGitProjectDao;
import cn.kuangxf.doc.source.GitUtil;

/**
 * 项目管理接口.
 *
 * @version 1.0 2018年3月19日
 * @author kuangxf
 * @history
 * 
 */
@RestController
@RequestMapping("/project")
public class ProjectController {
	private final static Logger logger = LoggerFactory.getLogger(ProjectController.class);
	@Autowired
	JavaGitProjectDao javaGitProjectDao;

	/**
	 * 获取所有项目
	 * 
	 * @return
	 */
	@RequestMapping("/list")
	public AjaxResult list() {
		List<JavaGitProject> list = null;
		try {
			list = (List<JavaGitProject>) javaGitProjectDao.findAll();
		} catch (Exception e) {
			logger.error("查询项目信息失败", e);
		}
		return AjaxResult.success(list);
	}

	/**
	 * 保存项目信息
	 * 
	 * @param bean
	 * @return
	 */
	@RequestMapping("/save")
	public AjaxResult save(JavaGitProjectBean bean) {
		try {
			JavaGitProject entity = new JavaGitProject();
			entity.setCode(bean.getCode());
			entity.setName(bean.getName());
			entity.setUri(bean.getUri());
			entity.setCreateTime(DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
			javaGitProjectDao.save(entity);
			return AjaxResult.success();
		} catch (Exception e) {
			logger.error("保存项目信息失败", e);
			return AjaxResult.failed("服务异常，保存项目信息失败！");
		}
	}

	/**
	 * 删除项目
	 * 
	 * @param code
	 * @return
	 */
	@RequestMapping("/delete")
	public AjaxResult delete(String code) {
		try {
			javaGitProjectDao.delete(code);
			return AjaxResult.success();
		} catch (Exception e) {
			logger.error("删除项目失败", e);
			return AjaxResult.failed("服务异常，删除项目失败！");
		}
	}

	/**
	 * 获取项目的分支信息
	 * 
	 * @param code
	 * @return
	 */
	@RequestMapping("/branchs")
	public AjaxResult branchs(String code) {
		try {
			JavaGitProject entity = javaGitProjectDao.findOne(code);
			if (entity == null) {
				return AjaxResult.failed("项目不存在！");
			}
			String uri = entity.getUri();
			List<String> branchs = GitUtil.branchList(uri);
			return AjaxResult.success(branchs);
		} catch (Exception e) {
			logger.error("获取GIT分支失败", e);
			return AjaxResult.failed("服务异常，获取GIT分支失败！");
		}
	}

}
