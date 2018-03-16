/*
 * @(#)SshSessionFactory.java        1.0 2018年3月16日
 *
 *
 */

package cn.kuangxf.doc.source;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;

import com.jcraft.jsch.Session;

/**
 * Class description goes here.
 *
 * @version 	1.0 2018年3月16日
 * @author		Administrator
 * @history	
 *		
 */
public class SshSessionFactory extends JschConfigSessionFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.jgit.transport.JschConfigSessionFactory#configure(org.eclipse.jgit.transport.OpenSshConfig.Host, com.jcraft.jsch.Session)
	 */
	@Override
	protected void configure(Host hc, Session session) {
		// TODO Auto-generated method stub

	}

}
