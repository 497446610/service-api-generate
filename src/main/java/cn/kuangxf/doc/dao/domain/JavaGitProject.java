/*
 * @(#)JavaGitProject.java        1.0 2018年3月19日
 *
 *
 */

package cn.kuangxf.doc.dao.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * java git 工程配置信息.
 *
 * @version 1.0 2018年3月19日
 * @author kuangxf
 * @history
 * 
 */
@Entity
@Table(name = "java_git_groject")
public class JavaGitProject {

	/**
	 * 项目代码
	 */
	@Id
	private String code;

	/**
	 * 姓名名称
	 */
	private String name;

	/**
	 * git 地址
	 */
	private String uri;

	/**
	 * 创建时间
	 */
	private String createTime;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

}
