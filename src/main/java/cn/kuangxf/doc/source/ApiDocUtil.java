/*
 * @(#)ApiDocUtil.java        1.0 2018年3月16日
 *
 *
 */

package cn.kuangxf.doc.source;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * 接口文档.
 *
 * @version 1.0 2018年3月16日
 * @author kuangxf
 * @history
 * 
 */
public class ApiDocUtil {

	public static void main(String[] args) throws Exception{
		File file = new File("D:\\IOrderMainService.java");
		/*
		 new VoidVisitorAdapter<Object>() {
			@Override
			public void visit(ClassOrInterfaceDeclaration n, Object arg) {
				super.visit(n, arg);
				System.out.println(" * " + n.getName());
			}
			
			@Override
			public void visit(JavadocComment n, Object arg) {
				super.visit(n, arg);
				
				System.out.println(" * " + n.getContent()); 
			}
			
		}.visit(JavaParser.parse(file), null);*/
		CompilationUnit unit = JavaParser.parse(file);
		System.out.println("hello");
		
	}
}
