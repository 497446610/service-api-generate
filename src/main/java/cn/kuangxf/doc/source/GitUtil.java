/*
 * @(#)GitUtil.java        1.0 2018年3月16日
 *
 *
 */

package cn.kuangxf.doc.source;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * git工具类.
 *
 * @version 1.0 2018年3月16日
 * @author kuangxf
 * @history
 * 
 */
public class GitUtil {

	private final static Logger logger = LoggerFactory.getLogger(GitUtil.class);

	private final static String DEFAULT_BRANCH = "master";// 默认为master分支

	/**
	 * 克隆源代码到本地文件
	 * 
	 * @param uri
	 *            git 地址
	 * @param localPath
	 *            本地目录
	 * @param branch
	 *            分支
	 */
	public static void clone(String uri, String localPath, String branch) throws Exception {
		if (StringUtils.isEmpty(uri)) {
			throw new Exception("uri is empty.");
		}
		if (StringUtils.isEmpty(localPath)) {
			throw new Exception("localPath is empty.");
		}
		if (StringUtils.isEmpty(branch)) {
			branch = DEFAULT_BRANCH;
		}
		File dest = new File(localPath);
		if (dest.exists()) {
			throw new Exception("目录已经存在");
		}
		logger.info("start clone:{},to directory:{}", uri, localPath);
		CloneCommand clone = Git.cloneRepository().setURI(uri).setDirectory(new File(localPath));
		clone.call();
		logger.info("finish clone.");
	}

	public static void cloneDelFirst(String uri, String localPath, String branch) throws Exception {
		if (StringUtils.isEmpty(uri)) {
			throw new Exception("uri is empty.");
		}
		if (StringUtils.isEmpty(localPath)) {
			throw new Exception("localPath is empty.");
		}
		File dest = new File(localPath);
		if (dest.exists()) {
			// 目录存在时，先删除目录
			logger.info("remove directory:{}", localPath);
			FileUtils.deleteDirectory(dest);
		}
		clone(uri, localPath, branch);
	}

	/**
	 * 拉取最新的代码
	 * 
	 * @param localRepository
	 *            本地仓库目录
	 */
	public static void pull(String localRepository)throws Exception  {
		if (StringUtils.isEmpty(localRepository)) {
			throw new Exception("localRepository is empty.");
		}
		
		File dest = new File(localRepository);
		if (!dest.exists()) {
			throw new Exception("repository is not exists.");
		}
		Git git = Git.open(dest);
		git.pull();
		logger.info("finish pull.");
	}

	public static void main(String[] args) throws Exception {

		//GitUtil.cloneDelFirst("https://github.com/497446610/zk-config.git", "d:\\test", "master");
		GitUtil.pull("d:\\test");

	}

}
