/*
 * @(#)ApiDocController.java        1.0 2018年3月19日
 *
 *
 */

package cn.kuangxf.doc.controller.api;

import java.io.File;
import java.util.Date;
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
import cn.kuangxf.doc.source.ApiDocUtil;
import cn.kuangxf.doc.source.GitUtil;
import cn.kuangxf.doc.source.JavaFileUtils;
import cn.kuangxf.doc.source.PODocUtil;

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
	 * 获取java文件的api解析数据
	 * 
	 * @param fileId
	 *            文件ID
	 * @return
	 */
	@RequestMapping("doc")
	public AjaxResult doc(String fileId) {

		JavaFile javaFile = javaFileManager.findOne(fileId);

		if (javaFile == null) {
			return AjaxResult.failed("Java文件不存在，可以尝试先刷新一下代码哟...");
		}
		JSONObject jsonObject = parseJSONByJavaFile(javaFile);
		if (jsonObject == null) {
			return AjaxResult.failed("解析文件错误，不能识别的java文件...");
		} else {
			return AjaxResult.success(jsonObject);
		}
	}

	/**
	 * 获取java文件的api解析数据
	 * 
	 */
	@RequestMapping("docByName")
	public AjaxResult docByName(String code, String branch, String className) {

		List<JavaFile> list = javaFileManager.findByClassName(code, branch, className);

		if (list == null || list.isEmpty()) {
			return AjaxResult.failed("Java文件不存在，可以尝试先刷新一下代码哟...");
		}

		JavaFile javaFile = list.get(0);

		JSONObject jsonObject = parseJSONByJavaFile(javaFile);
		if (jsonObject == null) {
			return AjaxResult.failed("解析文件错误，不能识别的java文件...");
		} else {
			return AjaxResult.success(jsonObject);
		}

	}

	@RequestMapping("doc4Model")
	public AjaxResult doc4Model(String code, String branch, String className) {

		List<JavaFile> list = javaFileManager.findByClassName(code, branch, className);
		List<JavaFile> allList = javaFileManager.findByCodeAndBranch(code, branch);
		if (list == null || list.isEmpty()) {
			return AjaxResult.failed("Java文件不存在，可以尝试先刷新一下代码哟...");
		}

		JavaFile javaFile = list.get(0);

		JSONArray jsonArray = parseJSONByJavaModelFile(javaFile, allList);
		if (jsonArray == null) {
			return AjaxResult.failed("解析文件错误，不能识别的java文件...");
		} else {
			return AjaxResult.success(jsonArray);
		}

	}

	private JSONObject parseJSONByJavaFile(JavaFile javaFile) {

		String apiFilePath = joinJavaJsonFilePath(javaFile);
		File file = new File(apiFilePath);
		if (file.exists()) {// api json文件存在，直接读取文件返回
			JSONObject jsonObject = ApiDocUtil.readApiFromFile(apiFilePath);
			return jsonObject;
		} else { // 不存在，解析java文件生成api json文件
			// java文件相对路径
			String javaRelativePath = javaFile.getRelativePath();
			// java文件绝对路径
			String javaFullPath = GlobalConfig.getJavaSourcePath(javaFile.getCode(), javaFile.getBranch(),
					javaRelativePath);
			JSONObject jsonObject = ApiDocUtil.parseApiFromJavaFile(javaFullPath);
			jsonObject.put("code", javaFile.getCode());
			jsonObject.put("brance", javaFile.getBranch());
			ApiDocUtil.writeApi2File(jsonObject, apiFilePath);
			return jsonObject;
		}
	}

	private JSONArray parseJSONByJavaModelFile(JavaFile javaFile, List<JavaFile> allList) {
		// java文件相对路径
		String javaRelativePath = javaFile.getRelativePath();
		// java文件绝对路径
		String javaFullPath = GlobalConfig.getJavaSourcePath(javaFile.getCode(), javaFile.getBranch(),
				javaRelativePath);
		JSONArray jsonArray = PODocUtil.parseApiFromJavaFile(javaFullPath, allList);
		return jsonArray;
	}

	/**
	 * 文件名格式：类全名.类文件修改时间.json
	 * 
	 * @param javaFile
	 * @return
	 */
	private String joinJavaJsonFilePath(JavaFile javaFile) {
		String apiDir = GlobalConfig.getApiDir(javaFile.getCode(), javaFile.getBranch());
		long modifyTime = javaFile.getModifyTime();
		String apiFilePath = apiDir + JavaFileUtils.SEPARATOR + javaFile.getClassName() + "."
				+ String.valueOf(modifyTime) + ".json";

		return apiFilePath;
	}

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

			if (result == null || result.isEmpty()) {
				refresh(code, branch);
				return AjaxResult.failed("服务器后台正在分析源代码，请稍后再试...");
			}

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
		} else {
			try {
				GitUtil.pull(sourceDirName);
			} catch (Exception e) {
				logger.error("pull git code error:{}", sourceDirName);
				return AjaxResult.failed("拉取代码失败");
			}
		}

		try {
			// 遍历所有的java文件
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
		long version = new Date().getTime();
		for (int i = 0, j = jsonArray.size(); i < j; i++) {
			JavaFile javaFile = new JavaFile();
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			javaFile.setFileId(String.valueOf(jsonObject.getString("relativePath").hashCode()));
			javaFile.setClassName(jsonObject.getString("className"));
			javaFile.setRelativePath(jsonObject.getString("relativePath"));
			javaFile.setModifyTime(jsonObject.getLong("modifyTime"));
			javaFile.setCode(code);
			javaFile.setBranch(branch);
			javaFile.setVersion(version);
			javaFileManager.save(javaFile);
		}

		javaFileManager.deleteByVersion(version);

	}

}
