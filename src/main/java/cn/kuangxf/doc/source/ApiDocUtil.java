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
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

/**
 * 完整解析Class工具类.<br>
 * 
 *
 * @version 1.0 2018年3月16日
 * @author kuangxf
 * @history
 * 
 */
public class ApiDocUtil {
	private final static Logger logger = LoggerFactory.getLogger(ApiDocUtil.class);

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

	public static void writeApi2File(JSONObject api, String apiFilePath) {
		File apiFile = new File(apiFilePath);

		if (!apiFile.exists()) {
			apiFile.delete();
		}

		try {
			FileUtils.forceMkdirParent(apiFile);
		} catch (IOException e) {
			logger.error("保存API文件失败", e);
		}

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(apiFile);
		} catch (IOException e) {
			logger.error("保存API文件失败", e);
		}

		try {
			fileWriter.write(api.toString());
		} catch (IOException e) {
			logger.error("保存API文件失败", e);
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				logger.error("保存API文件失败", e);
			}
		}
	}

	/**
	 * 解析java文件生成 api json数据
	 * 
	 * @param javaFilePath
	 *            java文件全路径
	 * @return class json 格式,样列：<br/>
	 {
	"code": "zk-config",                              //项目代码
 	"brance": "main",                                 //git分支
 	"package": "com.panda.generic.ware.domain.to",    //包名
 	"modifyTime": 1520490330903,                      //文件修改时间戳
 	"propertys": [{                                   //class属性数组
 		"poTypes": [{
 			"simpleName": "WareSeachPo",              //类名
 			"fullName": "com.panda.generic.ware.domain.po.WareSeachPo"//类包名
 		}],
 		"name": "wareSeachs",                         //属性名称
 		"comment": "",                                //注释
 		"type": "List<WareSeachPo>"                   //属性类型
 	}, {......}],
 	
 	"methods": [{									  //class方法数组
 		"rtype": "List<WareSeachPo>",                 //方法返回类型
 		"poTypes": [{                                 //方法中涉及到类的数组
 			"simpleName": "WareSeachPo",              //类名
 			"fullName": "com.panda.generic.ware.domain.po.WareSeachPo" //类包名
 		}],
 		"deprecated": false,                          //是否过期
 		"name": "public List<WareSeachPo> getWareSeachs()",            //方法
 		"comment": "",                                //方法注释
 		"params": []                                  //参数列表
 	}, {......}],
 	"fullName": "com.panda.generic.ware.domain.to.WareSeachTo",        //类全名
 	"comment": "",
 	"extendeds": [                                    //继承的类数组
 		"com.panda.generic.ware.domain.po.WareSeachPo"                 //类全名
 	],
 	"class": "WareSeachTo"                           //类名
 }
	 */
	public static JSONObject parseApiFromJavaFile(String javaFilePath) {

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
		long modifyTime = file.lastModified();

		if (childNodes.size() == 0) {
			return null;
		}
		Node node = childNodes.get(0);
		if (!(node instanceof ClassOrInterfaceDeclaration)) {
			return null;
		}
		ClassOrInterfaceDeclaration clsDecl = (ClassOrInterfaceDeclaration) node;
		JSONObject clsJson = new JSONObject();
		clsJson.put("package", packageName);// 包名
		clsJson.put("modifyTime", modifyTime);// 文件修改时间
		clsJson.put("class", clsDecl.getName().asString());// 类名
		clsJson.put("fullName", packageName + "." + clsDecl.getName().asString());// 类全名
		String comment = "";
		if (clsDecl.getComment().isPresent()) {
			comment = clsDecl.getComment().get().getContent().toString();
		}

		// 解析继承的父类
		// 继承的类
		JSONArray extendeds = new JSONArray();
		NodeList<ClassOrInterfaceType> extendedNodeList = clsDecl.getExtendedTypes();
		for (ClassOrInterfaceType classOrInterfaceType : extendedNodeList) {
			String parentType = classOrInterfaceType.getNameAsString();
			String fullTypeName = importMap.get(parentType);
			if (StringUtils.isEmpty(fullTypeName)) {
				extendeds.add(parentType);
			} else {
				extendeds.add(fullTypeName);
			}
		}

		comment = cutComment(comment);
		clsJson.put("comment", comment); // 类备注

		// 类的属性
		JSONArray property = new JSONArray();
		// 类的方法
		JSONArray method = new JSONArray();
		List<Node> clsChilds = clsDecl.getChildNodes();
		for (Node childNode : clsChilds) {
			if (childNode instanceof FieldDeclaration) {// 解析属性
				parseField(property, (FieldDeclaration) childNode, importMap);
			} else if (childNode instanceof MethodDeclaration) {// 解析方法
				parseMethod(method, (MethodDeclaration) childNode, importMap);
			}

		}

		clsJson.put("propertys", property);
		clsJson.put("methods", method);
		clsJson.put("extendeds", extendeds);

		logger.info(clsJson.toString());
		return clsJson;

	}

	private static void parseField(JSONArray properties, FieldDeclaration childNode, Map<String, String> importMap) {
		// 解析属性
		FieldDeclaration fieldDecl = childNode;
		String fieldComment = "";
		if (fieldDecl.getComment().isPresent()) {
			fieldComment = fieldDecl.getComment().get().getContent().toString();
		}

		String varType = fieldDecl.getVariables().get(0).getType().asString();
		String varName = fieldDecl.getVariables().get(0).getNameAsString();
		fieldComment = cutComment(fieldComment);

		JSONObject varJson = new JSONObject();
		varJson.put("name", varName);
		varJson.put("type", varType);

		varJson.put("comment", fieldComment);

		// 解析属性中涉及到的所有class
		varJson.put("poTypes", parsePOType(varType, importMap));
		properties.add(varJson);
	}

	private static void parseMethod(JSONArray methods, MethodDeclaration childNode, Map<String, String> importMap) {
		// 解析方法
		MethodDeclaration methodDecl = (MethodDeclaration) childNode;

		// 方法是否过期
		boolean deprecated = false;
		NodeList<AnnotationExpr> annotationList = methodDecl.getAnnotations();
		for (AnnotationExpr annotationExpr : annotationList) {
			String annoName = annotationExpr.getName().asString();
			if (annoName.indexOf("Deprecated") >= 0) {
				deprecated = true;
			}
		}

		String methodName = methodDecl.getDeclarationAsString();
		String methodComment = "";
		if (methodDecl.getComment().isPresent()) {
			methodComment = methodDecl.getComment().get().getContent().toString();
		}

		Set<String> poTypeSet = new HashSet<>();

		methodComment = cutComment(methodComment);
		String returnType = methodDecl.getType().asString();
		JSONObject methodJson = new JSONObject();
		methodJson.put("name", methodName); // 方法名称
		methodJson.put("rtype", returnType);// 方法返回类型
		methodJson.put("comment", methodComment);// 方法注释
		methodJson.put("deprecated", deprecated);// 是否过期
		methods.add(methodJson);

		poTypeSet.add(returnType);

		// 解析方法参数
		NodeList<Parameter> paramList = methodDecl.getParameters();
		JSONArray paramsJson = new JSONArray();

		for (Parameter param : paramList) {
			JSONObject paramJson = new JSONObject();
			paramJson.put("name", param.getName().asString());
			paramJson.put("type", param.getType().asString());
			paramsJson.add(paramJson);

			poTypeSet.add(param.getType().asString());
		}
		methodJson.put("params", paramsJson);

		// 解析方法涉及到的所有PO类（包括返回，参数，泛型）
		methodJson.put("poTypes", parsePOType(poTypeSet, importMap));
	}

	/**
	 * 解析poTypeSet中所有的PO类(不包含java.util.*,java.lang.*)
	 * 
	 * @param poTypeSet
	 * @param importMap
	 * @return
	 */
	private static JSONArray parsePOType(Set<String> poTypeSet, Map<String, String> importMap) {
		JSONArray poTypes = new JSONArray();

		Set<String> allTypes = new HashSet<>();
		for (String type : poTypeSet) {// 解析出所有的类型（分解出泛型中的类型）
			allTypes.addAll(parseGenericType(type));
		}
		for (String simpleType : allTypes) {
			String fullTypeName = importMap.get(simpleType);
			if (StringUtils.isEmpty(fullTypeName)) {
				continue;
			}
			JSONObject poJson = new JSONObject();
			poJson.put("simpleName", simpleType);
			poJson.put("fullName", fullTypeName);
			poTypes.add(poJson);
		}
		return poTypes;
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

	private static JSONArray parsePOType(String type, Map<String, String> importMap) {
		Set<String> allTypes = parseGenericType(type);
		JSONArray poTypes = new JSONArray();
		for (String simpleType : allTypes) {
			String fullTypeName = importMap.get(simpleType);
			if (StringUtils.isEmpty(fullTypeName)) {
				continue;
			}
			JSONObject poJson = new JSONObject();
			poJson.put("simpleName", simpleType);
			poJson.put("fullName", fullTypeName);
			poTypes.add(poJson);
		}
		return poTypes;
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
