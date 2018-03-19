package cn.kuangxf.doc.controller;

import java.io.Serializable;


/**
 * 说明: 功能描述
 * 
 * @author 况小锋
 * @version 1.0
 */
public class AjaxResult implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final Integer SUCCESS = 1;// 成功
	public static final Integer FAILE = -1;// 业务调用失败
	public static final Integer VALIDERROR = 2;// 数据验证错误
	public static final Integer NOAUTH = 3;// 未登录

	/**
	 * 错误代码
	 */
	private Integer code;
	/**
	 * 消息
	 */
	private String message;

	/**
	 * 数据
	 */
	private Object data;

	/**
	 * 总的记录数
	 */
	private Long total;

	/**
	 * 总的页数
	 */
	private Long totalPage;

	
	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public AjaxResult() {
	}

	public AjaxResult(Integer code) {
		this.code = code;
	}

	public AjaxResult(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public AjaxResult(Integer code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public AjaxResult(Integer code, String message, Object data, Long total, Long totalPage) {
		this.code = code;
		this.message = message;
		this.data = data;
		this.total = total;
		this.totalPage = totalPage;
	}

	

	public static AjaxResult success() {
		return new AjaxResult(SUCCESS);
	}

	public static AjaxResult success(String msg) {
		return new AjaxResult(SUCCESS, msg);
	}

	public static AjaxResult success(Object data) {
		return new AjaxResult(SUCCESS, "", data);
	}

	public static AjaxResult success(Object data, Long total, Long totalPage) {
		return new AjaxResult(SUCCESS, "", data, total, totalPage);
	}

	

	public static AjaxResult failed(String msgs) {
		return new AjaxResult(FAILE, msgs);
	}

	public static AjaxResult failed(Integer errorId, String msgs) {
		return new AjaxResult(errorId, msgs);
	}

	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	public Long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Long totalPage) {
		this.totalPage = totalPage;
	}

	

}
