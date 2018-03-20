/*
 * @(#)JavaFileMananger.java        1.0 2018年3月20日
 *
 *
 */

package cn.kuangxf.doc.dao.manager;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import cn.kuangxf.doc.dao.domain.JavaFile;

/**
 * Class description goes here.
 *
 * @version 1.0 2018年3月20日
 * @author kuangxf
 * @history
 * 
 */
public interface JavaFileMananger extends CrudRepository<JavaFile, String> {
	@Query("select u from JavaFile u where u.code = ?1 and u.branch=?2")
	List<JavaFile> findByCodeAndBranch(String code, String branch);
}
