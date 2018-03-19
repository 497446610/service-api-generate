import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.kuangxf.doc.App;
import cn.kuangxf.doc.dao.domain.JavaGitProject;
import cn.kuangxf.doc.dao.manager.JavaGitProjectDao;

/*
 * @(#)SpringbootH2ApplicationTests.java        1.0 2018年3月19日
 *
 *
 */

/**
 * Class description goes here.
 *
 * @version 	1.0 2018年3月19日
 * @author		Administrator
 * @history	
 *		
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=App.class)
public class SpringbootH2ApplicationTests {
	
	@Autowired
	JavaGitProjectDao javaGitProjectDao;
	
	@Test
	public void testInsert() {
		JavaGitProject entity = new JavaGitProject();
		entity.setCode("test1");
		entity.setName("name1");
		entity.setCreateTime("2018-03-18");
		javaGitProjectDao.save(entity);
	}
	
	@Test
	public void testQuery() {
		JavaGitProject entity = javaGitProjectDao.findOne("test1");
		System.out.println(entity.getName());
	}
	
	@Test
	public void testDelete() {
		javaGitProjectDao.delete("test1");
	}

}
