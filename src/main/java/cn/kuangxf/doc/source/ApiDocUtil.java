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
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

/**
 * 接口文档.
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
	 * @return
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
		if (!(node instanceof ClassOrInterfaceDeclaration ) ) {
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

		comment = cutComment(comment);
		clsJson.put("comment", comment); // 类备注

		// 类的属性
		JSONArray property = new JSONArray();

		// 类的方法
		JSONArray method = new JSONArray();

		List<Node> clsChilds = clsDecl.getChildNodes();
		for (Node childNode : clsChilds) {
			if (childNode instanceof FieldDeclaration) {
				// 解析属性
				FieldDeclaration fieldDecl = (FieldDeclaration) childNode;
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
				property.add(varJson);
			} else if (childNode instanceof MethodDeclaration) {
				// 解析方法
				MethodDeclaration methodDecl = (MethodDeclaration) childNode;

				String methodName = methodDecl.getDeclarationAsString();
				String methodComment = "";
				if (methodDecl.getComment().isPresent()) {
					methodComment = methodDecl.getComment().get().getContent().toString();
				}

				Set<String> poTypeSet = new HashSet<>();

				methodComment = cutComment(methodComment);
				String returnType = methodDecl.getType().asString();
				JSONObject methodJson = new JSONObject();
				methodJson.put("name", methodName);
				methodJson.put("rtype", returnType);
				methodJson.put("comment", methodComment);
				method.add(methodJson);

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

		}

		clsJson.put("propertys", property);
		clsJson.put("methods", method);

		logger.info(clsJson.toString());
		return clsJson;

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
				if (isPrimitive(type)) {
					continue;
				}
				allTypes.add(type);
			}
		}
		for (String type : allTypes) {
			String fullTypeName = importMap.get(type);
			if (StringUtils.isEmpty(fullTypeName)) {
				continue;
			}
			JSONObject poJson = new JSONObject();
			poJson.put("simpleName", type);
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
		JSONObject api = ApiDocUtil.parseApiFromJavaFile("D:\\IOrderMainService.java");

	}
}
