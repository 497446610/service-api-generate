/*
 * @(#)JavaFileMananger.java        1.0 2018年3月20日
 *
 *
 */

package cn.kuangxf.doc.dao.manager;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
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
	@Query("select u from JavaFile u where u.code = ?1 and u.branch=?2 order by u.packageName asc, u.className asc")
	List<JavaFile> findByCodeAndBranch(String code, String branch);
	
	@Query("select u from JavaFile u where u.code = ?1 and u.branch=?2 and u.packageName=?3 order by u.className asc")
	List<JavaFile> findByCodeAndBranch(String code, String branch,String packageName);
	
	@Query("select distinct u.packageName from JavaFile u where u.code = ?1 and u.branch=?2 order by u.packageName asc")
	List<String> findPackageName(String code, String branch);

	@Query("select u from JavaFile u where u.code = ?1 and u.branch=?2 and u.className=?3")
	List<JavaFile> findByClassName(String code, String branch, String className);
	
	

	@Modifying
	@Transactional
	@Query("delete from JavaFile u where u.version != ?1 ")
	void deleteByVersion(long version);
}
