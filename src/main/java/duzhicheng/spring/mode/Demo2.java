package duzhicheng.spring.mode;

import duzhicheng.spring.mode.annotation.Autowired;
import duzhicheng.spring.mode.annotation.Controller;
import duzhicheng.spring.mode.annotation.RequestMapping;

@Controller
public class Demo2 {
	
	@Autowired
	private DemoService demoService;
	
	@RequestMapping("/ddd")
	public void get(String name,int age) {
		System.out.println(demoService.getStr()+"demo2 我是ccc"+name);
	}
	@RequestMapping("/eee")
	public void get(int age) {
		System.out.println(demoService.getStr()+"demo2 我是eee"+age);
	}
}
