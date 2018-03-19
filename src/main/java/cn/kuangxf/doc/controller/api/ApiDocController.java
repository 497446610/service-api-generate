/*
 * @(#)ApiDocController.java        1.0 2018年3月19日
 *
 *
 */

package cn.kuangxf.doc.controller.api;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;

import cn.kuangxf.doc.config.GlobalConfig;
import cn.kuangxf.doc.controller.AjaxResult;
import cn.kuangxf.doc.dao.domain.JavaGitProject;
import cn.kuangxf.doc.dao.manager.JavaGitProjectDao;
import cn.kuangxf.doc.source.GitUtil;
import cn.kuangxf.doc.source.JavaFileUtils;

/**
 * api doc 接口.
 *
 * @version 1.0 2018年3月19日
 * @author kuangxf
 * @history
 * 
 */
@RestController
@RequestMapping("/api")
public class ApiDocController {
	private final static Logger logger = LoggerFactory.getLogger(ApiDocController.class);
	@Autowired
	JavaGitProjectDao javaGitProjectDao;

	/**
	 * 获取项目某个分支的所有的class
	 * 
	 * @param code
	 *            项目代码
	 * @param branch
	 *            分支
	 * @return
	 */
	@RequestMapping("classList")
	public AjaxResult classList(String code, String branch) {
		String sourceDirName = GlobalConfig.getSourceDir(code, branch);
		File sourceDir = new File(sourceDirName);
		if (!sourceDir.exists()) {// 本地仓库不存在,需要从远程仓库克隆源代码到本地
			cloneSource(code, sourceDirName, branch);
			return AjaxResult.failed("服务器后台正在克隆源代码，请稍后再试...");
		}

		try {
			GitUtil.pull(sourceDirName);
		} catch (Exception e) {
			logger.error("pull git code error:{}", sourceDirName);
		}

		try {
			JSONArray jsonArray = JavaFileUtils.listFiles(sourceDirName);
			return AjaxResult.success(jsonArray);
		} catch (Exception e) {
			logger.error("遍历项目\"{}\"Java文件失败", sourceDirName);
			return AjaxResult.failed("遍历项目Java文件失败");
		}

	}

	/**
	 * 异步克隆源代码
	 * 
	 * @param code
	 * @param sourceDirName
	 */
	@Async
	private void cloneSource(String code, String sourceDirName, String branch) {
		JavaGitProject entity = javaGitProjectDao.findOne(code);
		if (entity == null) {
			logger.error("项目不存在：{}", code);
			return;
		}

		try {
			GitUtil.clone(entity.getUri(), sourceDirName, branch);
		} catch (Exception e) {
			logger.error("clone git code error:{}", entity.getUri());
		}
	}

}
