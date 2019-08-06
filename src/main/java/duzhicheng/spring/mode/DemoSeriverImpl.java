package duzhicheng.spring.mode;

import duzhicheng.spring.mode.annotation.Server;

@Server
public class DemoSeriverImpl implements DemoService{

	@Override
	public String getStr() {
		return "我就想看看成功没有";
	}

}
