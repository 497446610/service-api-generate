/*
 * @(#)JavaGitProjectDao.java        1.0 2018年3月19日
 *
 *
 */

package cn.kuangxf.doc.dao.manager;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import cn.kuangxf.doc.dao.domain.JavaGitProject;

/**
 * JavaGitProject dao.
 *
 * @version 1.0 2018年3月19日
 * @author kuangxf
 * @history
 * 
 */
@Repository
public interface JavaGitProjectDao extends CrudRepository<JavaGitProject, String> {

}
