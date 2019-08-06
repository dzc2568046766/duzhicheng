package duzhicheng.spring.mode;

import duzhicheng.spring.mode.annotation.Autowired;
import duzhicheng.spring.mode.annotation.Controller;
import duzhicheng.spring.mode.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/aaa")
public class Dome {
	
	@Autowired
	private DemoService demoService;
	
	@RequestMapping(value = "/bbb")
	public String name() {
		String str = demoService.getStr();
		System.out.println(str);
		return str;
	}
}
