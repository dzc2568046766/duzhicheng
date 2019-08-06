package duzhicheng.spring.mode.servlet;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import duzhicheng.spring.mode.annotation.Autowired;
import duzhicheng.spring.mode.annotation.Component;
import duzhicheng.spring.mode.annotation.Controller;
import duzhicheng.spring.mode.annotation.RequestMapping;
import duzhicheng.spring.mode.annotation.Server;

public class DispatcherServlet extends HttpServlet{

	/**
	 *
	 */
	private static final long serialVersionUID = 1456062885004779439L;

	//存放包下的字节码的
	private final ArrayList<String> array = new ArrayList<>();

	//存放   类路径 与类实例之间的映射        /XXXX   ===>  /XXXX这个类的实例
	private final Map<String, Object> classMappers = new HashMap<String, Object>();

	//存放  类路径+方法路径 与方法名之间的映射        /XXXX   ===>  /XXXX这个类的实例
	private final Map<String, Method> methodsMappers = new HashMap<String, Method>();   //这个东西 就相当于一个HandlerMapping

//	private final List<Object> param = new ArrayList<>();
//
//	private final Map<String, Object> paramMappers = new HashMap<String, Object>();   //方法参数的map

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp ) {
		try {
			//获取到访问的全路径
			String requestURI = req.getRequestURI(); //   /工程名+类路径+方法路径
			//获取工程路径
			String contextPath = req.getContextPath();
			//把工程路径删掉 , 只留下后面的  类路径和方法路径
			String url = requestURI.replace(contextPath, "");
			//循环我们的map  里面存的就是  路径 <===> 和需要调用的方法
			for (Entry<String, Method> Entry : methodsMappers.entrySet()) {
				//如果访问路径和我们存的路径相等
				if(Entry.getKey().equals(url)) {
					//获取相对应的方法
					Method Method = Entry.getValue();
					//通过这个方法获取这个类的类名
					String methodStr = Method.toString();
					if(methodStr.contains("(") || methodStr.contains(")")) {
						methodStr = Method.toString().substring(0,Method.toString().lastIndexOf("("));
					}
					String string = methodStr.substring(0,methodStr.lastIndexOf("."));
					string = string.substring(string.lastIndexOf(".")+1);
					//获取对象
					//通过url  /aaa/bbb   取到 /aaa   通过前面的classMapper中存的  类路径    ===   类的实例   取到controller的实例
					Object object = classMappers.get("/"+url.split("/")[1]);
					if(object == null) {
						//代表类上面没有requestMapping注解
						object = classMappers.get(string);
					}
					Object args[] = new Object[Method.getParameterCount()];
					Parameter[] parameters = Method.getParameters();
					for (int i = 0; i < parameters.length; i++) {
						String name = parameters[i].getName();
						Type typeName = parameters[i].getParameterizedType();
						Object parameter2 = req.getParameter(name);
						args[i] = parameter2;
					}
					//调用方法
					Method.invoke(object,args);
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp ) {
		doGet(req, resp);
	}

	@Override
	public void init() {
		try {
			//扫包
			scanPackage("duzhicheng.spring.mode");
			if(array.isEmpty()) {
				System.out.println("兄弟,怕是出了点问题哟");
				return;
			}
			//类路径映射
			classMapper();
			//依赖注入
			fieldMapper();
			//方法映射
			methodsMapper();   // 做到这里就相当于完成了一个HandlerMapping

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fieldMapper() throws InstantiationException, IllegalAccessException {
		for (Entry<String, Object> Entry : classMappers.entrySet()) {
			Object object = Entry.getValue();
			Field[] fields = object.getClass().getDeclaredFields();
			for (Field field : fields) {
				//设置为强制访问
				field.setAccessible(true);
				//判断属性上面是不是有  @Autowired  注解
				if(field.isAnnotationPresent(Autowired.class)) {
					//这里取到被注解标识属性的类型calss 这里我们一般都是注入的接口
					Class<?> class1 = field.getType();
					// 再次循环 取到路面的每一个value值  就是里面的每一个实例
					for (Entry<String, Object> Entry1 : classMappers.entrySet()) {
						Object object2 = Entry1.getValue();
						//如果class1 ==> 这个接口的class  是object2 的被实现接口
						//如果 object2   是 class1 的实现类
						if(class1.isInstance(object2)) {
							//给这个属性重新赋值
							field.set(object, object2);
						}
					}
				}
			}
		}
	}

	private void methodsMapper() throws Exception {
		if(classMappers.isEmpty()) {
			System.out.println("兄弟 , 你怕是掉了什么注解哟");
			return;
		}
		for (Entry<String, Object> Entry : classMappers.entrySet()) {
			//这里的ClassMapperName   就是我们类上面的路径
			String ClassMapperName = Entry.getKey();
			if(!ClassMapperName.contains("/")) {
				ClassMapperName = "";
			}
			Class<? extends Object> class1 = Entry.getValue().getClass();
			Method[] methods = class1.getMethods();
			for (Method method : methods) {
				if(method.isAnnotationPresent(RequestMapping.class)) {
					//获取到方法上面requestmapeer里面的值
					RequestMapping mapping = method.getAnnotation(RequestMapping.class);
					//value  ===>  方法上的路径
					String value = mapping.value();
//					Parameter[] parameters = method.getParameters();
//					int parameterCount = method.getParameterCount(); // 返回该方法参数的个数
//					String mrthodName = method.getName(); //返回该方法的名字
//					String typeName = "";
//					for (int i = 0; i < parameters.length; i++) {
//						Parameter parameter = parameters[i];
//						String name = parameter.getName();// 返回该方法的参数名
//						String paramTypeName = parameter.getParameterizedType().getTypeName();   //返回参数类型的名字
//						param.add(name);
//						typeName += paramTypeName+"/";
//					}
//					if(parameterCount > 0) {
//						paramMappers.put(mrthodName+parameterCount+typeName, typeName);
//					}
					methodsMappers.put(ClassMapperName+value, method);
					//获取方法里面的参数

				}
			}
		}
	}

	public void classMapper() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		for (String string : array) {
			//获取这个类的字节码 ,在加载的时候是不需要后缀的,仅需要完整的名包+文件名
			Class<?> clazz = Class.forName(string.replace(".class", ""));
			//获取这个类的类名
			String string2 = clazz.toString().substring(clazz.toString().lastIndexOf(".")+1);
			//找到所有带controller的类
			if(clazz.isAnnotationPresent(Controller.class)){
				//进行实例化
				Object object = clazz.newInstance();
				//获取类上面的mapping注解        mapping = @RequestMapping(index='/xxxx')
				RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
				//获取到里面的值   value = /xxxx
				String value = "";
				//判断类上面是否存在RequestMapping注解
				if(null != mapping) {
					//存在  map的key为 对应的value值
					value = mapping.value();
				}else {
					//不存在 就用类名进行代替 , 同一个包下面类名是唯一的
					value = string2;
				}
				//这一步就是完成类上面的路径映射
				//例如 我们的访问路径是  localhost:8080/test/index
				//那我们这里的value值就是    /test
				//当我们吧 value  和我们已经实例化出来的类   put到一个map里面 是不是以为着  当我们访问/test的时候 我们就可以直接访问这个类
				classMappers.put(value, object);
			}
			if(clazz.isAnnotationPresent(Server.class)){//找到的是实现类
				//进行实例化
				Object object = clazz.newInstance();
				classMappers.put(clazz.getName(), object);
			}
			if(clazz.isAnnotationPresent(Component.class)) {
				clazz.newInstance();
			}
		}
	}

	private void scanPackage(String string) {
		//这里我们传入的路径是   aaa.bbb.ccc   我们需要换成aaa/bbb/ccc  目录结构的形式
		String packagePath = string.replaceAll("\\.", "/");
		//获取全路径 盘符开始
		URL url = this.getClass().getClassLoader().getResource("/"+packagePath);
		File[] files = new File(url.getFile()).listFiles();
		for (File file : files) {
			if(file.isDirectory()) {
				//如果是目录
				String name = file.getName();
				scanPackage(string+"."+name);
			}else {
				//例如包是XXX.XXX.XXX.AAA
				//在这个我们获取的文件是AAA
				//当类加载的时候是全限命名的,所以要加上包名
				array.add(string+"."+file.getName());
			}
		}
	}
}
