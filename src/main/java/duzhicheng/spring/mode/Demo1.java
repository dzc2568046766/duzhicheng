package duzhicheng.spring.mode;

import duzhicheng.spring.mode.annotation.Autowired;
import duzhicheng.spring.mode.annotation.Controller;
import duzhicheng.spring.mode.annotation.RequestMapping;

@Controller
public class Demo1 {
	@Autowired
	private DemoService demoService;
	
	@RequestMapping("/ccc")
	public String get() {
		System.out.println(demoService.getStr()+"我是ccc");
		return demoService.getStr()+"我是ccc";
	}
}
