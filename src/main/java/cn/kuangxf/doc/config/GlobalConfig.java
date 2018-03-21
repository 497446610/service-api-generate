/*
 * @(#)GlobalConfig.java        1.0 2018年3月19日
 *
 *
 */

package cn.kuangxf.doc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.kuangxf.doc.source.JavaFileUtils;

/**
 * 全局配置类.
 *
 * @version 1.0 2018年3月19日
 * @author kuangxf
 * @history
 * 
 */
@Component
public class GlobalConfig {

	public static String dataDir;

	@Value("${doc.data.dir}")
	public void setDataDir(String dataDir) {
		GlobalConfig.dataDir = dataDir;
	}

	/**
	 * 获得git 代码存放路径
	 * 
	 * @param code
	 * @param branch
	 * @return
	 */
	public static String getSourceDir(String code, String branch) {
		String dataDir = GlobalConfig.dataDir;

		StringBuilder sourceDir = new StringBuilder(dataDir);
		if (!dataDir.endsWith(JavaFileUtils.SEPARATOR)) {
			sourceDir.append(JavaFileUtils.SEPARATOR);
		}

		sourceDir.append("source");
		sourceDir.append(JavaFileUtils.SEPARATOR);

		sourceDir.append(code);
		sourceDir.append(JavaFileUtils.SEPARATOR);

		sourceDir.append(branch);

		return sourceDir.toString();
	}

	/**
	 * 取得java 文件的全路径
	 * 
	 * @param code
	 * @param branch
	 * @param relativePath
	 * @return
	 */
	public static String getJavaSourcePath(String code, String branch, String relativePath) {
		StringBuilder sourceDir = new StringBuilder(getSourceDir(code, branch));

		if (!relativePath.startsWith("\\")) {
			sourceDir.append(JavaFileUtils.SEPARATOR);
		}
		sourceDir.append(relativePath);

		return sourceDir.toString();
	}

	/**
	 * 获得类解析的json数据存放路径
	 * 
	 * @param code
	 * @param branch
	 * @return
	 */
	public static String getApiDir(String code, String branch) {
		String dataDir = GlobalConfig.dataDir;
		StringBuilder sourceDir = new StringBuilder(dataDir);
		if (!dataDir.endsWith(JavaFileUtils.SEPARATOR)) {
			sourceDir.append(JavaFileUtils.SEPARATOR);
		}

		sourceDir.append("api");
		sourceDir.append(JavaFileUtils.SEPARATOR);

		sourceDir.append(code);
		sourceDir.append(JavaFileUtils.SEPARATOR);

		sourceDir.append(branch);

		return sourceDir.toString();
	}
}
