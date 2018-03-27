/*
 * @(#)ApiDocUtil.java        1.0 2018年3月16日
 *
 *
 */

package cn.kuangxf.doc.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import cn.kuangxf.doc.config.GlobalConfig;
import cn.kuangxf.doc.dao.domain.JavaFile;

/**
 * 只解析Class的属性.<br>
 * 
 *
 * @version 1.0 2018年3月16日
 * @author kuangxf
 * @history
 * 
 */
public class PODocUtil {
	private final static Logger logger = LoggerFactory.getLogger(PODocUtil.class);

	/**
	 * 从文件读取API 解析结果
	 * 
	 * @param javaFilePath
	 *            api json 文件路径
	 * @return
	 */
	public static JSONObject readApiFromFile(String javaFilePath) {
		File apiFile = new File(javaFilePath);

		if (!apiFile.exists()) {
			return null;
		}

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(apiFile);
		} catch (FileNotFoundException e) {
			logger.error("读取API文件失败", e);
			return null;
		}
		BufferedReader bf = new BufferedReader(fileReader);

		String content = "";
		StringBuilder sb = new StringBuilder();

		try {
			while (content != null) {
				content = bf.readLine();

				if (content == null) {
					break;
				}

				sb.append(content.trim());
			}
		} catch (IOException e) {
			logger.error("读取API文件失败", e);
			return null;
		} finally {
			try {
				bf.close();
				fileReader.close();
			} catch (IOException e) {
				logger.error("读取API文件失败", e);
				return null;
			}
		}

		JSONObject result = JSONObject.parseObject(sb.toString());
		return result;

	}

	/**
	 * 解析java文件生成 api json数据
	 * 
	 * @param javaFilePath
	 *            java文件全路径
	 * @return class json 格式,样列：<br/>
	 *         { "code": "zk-config", //项目代码 "brance": "main", //git分支
	 *         "package": "com.panda.generic.ware.domain.to", //包名 "modifyTime":
	 *         1520490330903, //文件修改时间戳 "propertys": [{ //class属性数组 "poTypes":
	 *         [{ "simpleName": "WareSeachPo", //类名 "fullName":
	 *         "com.panda.generic.ware.domain.po.WareSeachPo"//类包名 }], "name":
	 *         "wareSeachs", //属性名称 "comment": "", //注释 "type":
	 *         "List<WareSeachPo>" //属性类型 }, {......}],
	 * 
	 *         "methods": [{ //class方法数组 "rtype": "List<WareSeachPo>", //方法返回类型
	 *         "poTypes": [{ //方法中涉及到类的数组 "simpleName": "WareSeachPo", //类名
	 *         "fullName": "com.panda.generic.ware.domain.po.WareSeachPo" //类包名
	 *         }], "deprecated": false, //是否过期 "name": "public List<WareSeachPo>
	 *         getWareSeachs()", //方法 "comment": "", //方法注释 "params": [] //参数列表
	 *         }, {......}], "fullName":
	 *         "com.panda.generic.ware.domain.to.WareSeachTo", //类全名 "comment":
	 *         "", "extendeds": [ //继承的类数组
	 *         "com.panda.generic.ware.domain.po.WareSeachPo" //类全名 ], "class":
	 *         "WareSeachTo" //类名 }
	 */
	public static JSONArray parseApiFromJavaFile(String javaFilePath, List<JavaFile> javaList) {

		File file = new File(javaFilePath);
		if (!file.exists()) {
			return null;
		}

		CompilationUnit unit = null;
		try {
			unit = JavaParser.parse(file);
		} catch (FileNotFoundException e) {
			logger.error("解析Java文件失败", e);
		}

		if (unit == null) {
			return null;
		}

		NodeList<?> childNodes = unit.getTypes();
		// 包名
		String packageName = unit.getPackageDeclaration().get().getName().asString();
		NodeList<ImportDeclaration> imports = unit.getImports();
		Map<String, String> importMap = new HashMap<>();
		for (ImportDeclaration importDeclaration : imports) {
			String importName = importDeclaration.getNameAsString();
			if (importName.indexOf("java") > 0) {//
				continue;
			}
			int index = importName.lastIndexOf(".");
			String clsName = importName.substring(index + 1);
			importMap.put(clsName, importName);
		}

		if (childNodes.size() == 0) {
			return null;
		}
		Node node = childNodes.get(0);
		if (!(node instanceof ClassOrInterfaceDeclaration)) {
			return null;
		}
		ClassOrInterfaceDeclaration clsDecl = (ClassOrInterfaceDeclaration) node;

		// 解析继承的父类

		// 类的属性
		JSONArray property = new JSONArray();
		List<Node> clsChilds = clsDecl.getChildNodes();
		for (Node childNode : clsChilds) {
			if (childNode instanceof FieldDeclaration) {// 解析属性
				parseField(property, (FieldDeclaration) childNode, packageName, importMap, javaList);
			}
		}

		// 继承的类,合并继承的类的属性
		NodeList<ClassOrInterfaceType> extendedNodeList = clsDecl.getExtendedTypes();
		for (ClassOrInterfaceType classOrInterfaceType : extendedNodeList) {
			String parentType = classOrInterfaceType.getNameAsString();
			String fullTypeName = importMap.get(parentType);
			if (StringUtils.isEmpty(fullTypeName)) {
				fullTypeName = packageName + "." + parentType;
			}
			String parentJavaFilePath = findJavaFilePathByClassName(fullTypeName, javaList);
			if (parentJavaFilePath == null) {
				continue;
			}
			JSONArray parentProperties = parseApiFromJavaFile(parentJavaFilePath, javaList);
			if (parentProperties == null) {
				continue;
			}
			// 合并父类的熟悉
			for (Object propertyParent : parentProperties) {
				property.add(propertyParent);
			}
		}
		return property;
	}

	private static void parseField(JSONArray properties, FieldDeclaration childNode, String packageName,
			Map<String, String> importMap, List<JavaFile> javaList) {
		// 解析属性
		FieldDeclaration fieldDecl = childNode;
		String fieldComment = "";
		if (fieldDecl.getComment().isPresent()) {
			fieldComment = fieldDecl.getComment().get().getContent().toString();
		}

		String varType = fieldDecl.getVariables().get(0).getType().asString();
		String varName = fieldDecl.getVariables().get(0).getNameAsString();
		fieldComment = cutComment(fieldComment);

		JSONObject propertyInfo = new JSONObject();
		propertyInfo.put("comment", fieldComment);
		propertyInfo.put("type", varType);

		// 解析属性中引用到的其他属性
		List<String> referList = parsePOType(packageName, varType, importMap);
		for (String classFullName : referList) {
			String javaFilePath = findJavaFilePathByClassName(classFullName, javaList);
			if (javaFilePath == null) {
				continue;
			}
			JSONArray temp = parseApiFromJavaFile(javaFilePath, javaList);
			if (temp != null) {
				propertyInfo.put(classFullName, temp);
			}
		}

		JSONObject property = new JSONObject();
		property.put(varName, propertyInfo);
		properties.add(property);
	}

	private static String findJavaFilePathByClassName(String classFullName, List<JavaFile> javaList) {
		for (JavaFile javaFile : javaList) {
			if (javaFile.getClassName().equalsIgnoreCase(classFullName)) {
				return GlobalConfig.getJavaSourcePath(javaFile.getCode(), javaFile.getBranch(),
						javaFile.getRelativePath());
			}
		}
		return null;
	}

	/**
	 * 解析出所有泛型中的class
	 * 
	 * @param type
	 * @return
	 */
	private static Set<String> parseGenericType(String type) {
		Set<String> allTypes = new HashSet<>();
		if (type.indexOf("<") > 0) { // 处理泛型
			type = type.replace("<", ",");
			type = type.replace(">", ",");
			String[] generics = type.split(",");
			for (String genericType : generics) {
				if (StringUtils.isEmpty(genericType)) {
					continue;
				}
				if (isPrimitive(genericType)) {
					continue;
				}
				allTypes.add(genericType);
			}
		} else {
			if (!isPrimitive(type)) {
				allTypes.add(type);
			}
		}
		return allTypes;

	}

	private static List<String> parsePOType(String classPackage, String type, Map<String, String> importMap) {
		Set<String> allTypes = parseGenericType(type);
		List<String> result = new ArrayList<>();
		for (String simpleType : allTypes) {
			String fullTypeName = importMap.get(simpleType);
			if (!StringUtils.isEmpty(fullTypeName)) {
				result.add(fullTypeName);
			}
			if (simpleType.indexOf(".") > 0) {
				result.add(simpleType);
			} else {// 没有引用的包时，默认和应用的类同包名
				result.add(classPackage + "." + simpleType);
			}

		}
		return result;
	}

	private static boolean isPrimitive(String type) {
		switch (type) {
		case "byte":
		case "short":
		case "int":
		case "long":
		case "boolean":
		case "char":
		case "float":
		case "double":
		case "Byte":
		case "String":
		case "Short":
		case "Integer":
		case "Long":
		case "Boolean":
		case "Date":
		case "BigDecimal":
		case "Character":
		case "Float":
		case "Double":
		case "Object":
		case "List":
		case "Set":
		case "Collection":
		case "Map":
		case "ArrayList":
		case "HashMap":
		case "HashSet":
			return true;
		default:
			return false;
		}
	}

	private static String cutComment(String comment) {
		comment = comment.replace("*", "");
		comment = comment.replace("\r\n", "<br/>");
		comment = comment.replace("\t", " ");
		return comment.trim();
	}

	public static void main(String[] args) throws Exception {

		/*
		 * new VoidVisitorAdapter<Object>() {
		 * 
		 * @Override public void visit(ClassOrInterfaceDeclaration n, Object
		 * arg) { super.visit(n, arg); System.out.println(" * " + n.getName());
		 * }
		 * 
		 * @Override public void visit(JavadocComment n, Object arg) {
		 * super.visit(n, arg);
		 * 
		 * System.out.println(" * " + n.getContent()); }
		 * 
		 * }.visit(JavaParser.parse(file), null);
		 */

		// JSONObject api =
		// ApiDocUtil.parseApiFromFile("D:\\JavaGitProjectBean.java");
		// JSONObject api =
		// ApiDocUtil.parseApiFromJavaFile("D:\\IOrderMainService.java");

	}
}
