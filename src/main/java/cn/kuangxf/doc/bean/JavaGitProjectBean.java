/*
 * @(#)JavaGitProjectBean.java        1.0 2018年3月19日
 *
 *
 */

package cn.kuangxf.doc.bean;

/**
 * 项目信息.
 *
 * @version 1.0 2018年3月19日
 * @author kuangxf
 * @history
 * 
 */
public class JavaGitProjectBean {

	/**
	 * 项目代码
	 */
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
