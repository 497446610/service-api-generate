/*
 * @(#)ApiDocController.java        1.0 2018年3月19日
 *
 *
 */

package cn.kuangxf.doc.controller.api;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.kuangxf.doc.config.GlobalConfig;
import cn.kuangxf.doc.controller.AjaxResult;
import cn.kuangxf.doc.dao.domain.JavaFile;
import cn.kuangxf.doc.dao.domain.JavaGitProject;
import cn.kuangxf.doc.dao.manager.JavaFileMananger;
import cn.kuangxf.doc.dao.manager.JavaGitProjectMananger;
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
	JavaGitProjectMananger javaGitProjectMananger;

	@Autowired
	JavaFileMananger javaFileManager;

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
			List<JavaFile> result = javaFileManager.findByCodeAndBranch(code, branch);
			return AjaxResult.success(result);
		} catch (Exception e) {
			logger.error("query java file error", e);
			return AjaxResult.failed("服务器异常，查询数据失败");
		}

	}

	/**
	 * 刷新代码
	 * 
	 * @param code
	 *            项目代码
	 * @param branch
	 *            git分支
	 * @return
	 */
	@RequestMapping("refresh")
	public AjaxResult refresh(String code, String branch) {
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
			return AjaxResult.failed("拉取代码失败");
		}

		try {
			JSONArray jsonArray = JavaFileUtils.listFiles(sourceDirName);

			saveJavaFile(code, branch, jsonArray);

			return AjaxResult.success();
		} catch (Exception e) {
			logger.error("遍历项目\"{}\"Java文件失败", e);
			return AjaxResult.failed("遍历项目Java文件失败");
		}

	}

	/**
	 * 异步克隆源代码
	 * 
	 * @param code
	 *            项目代码
	 * @param sourceDirName
	 *            本地路径
	 * @param branch
	 *            分支
	 */
	@Async
	private void cloneSource(String code, String sourceDirName, String branch) {
		JavaGitProject entity = javaGitProjectMananger.findOne(code);
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

	/**
	 * 保存java文件信息
	 * 
	 * @param code
	 *            项目代码
	 * @param branch
	 *            本地路径
	 * @param jsonArray
	 *            java文件信息
	 */
	@Async
	private void saveJavaFile(String code, String branch, JSONArray jsonArray) {
		for (int i = 0, j = jsonArray.size(); i < j; i++) {
			JavaFile javaFile = new JavaFile();
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			javaFile.setFileId(String.valueOf(jsonObject.getString("relativePath").hashCode()));
			javaFile.setClassName(jsonObject.getString("className"));
			javaFile.setRelativePath(jsonObject.getString("relativePath"));
			javaFile.setModifyTime(jsonObject.getLong("modifyTime"));
			javaFile.setCode(code);
			javaFile.setBranch(branch);
			javaFileManager.save(javaFile);
		}
	}

}
