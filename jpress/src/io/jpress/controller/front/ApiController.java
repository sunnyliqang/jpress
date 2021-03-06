package io.jpress.controller.front;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.jpress.Consts;
import io.jpress.core.JBaseController;
import io.jpress.core.annotation.UrlMapping;
import io.jpress.model.Content;
import io.jpress.model.Option;
import io.jpress.utils.EncryptUtils;
import io.jpress.utils.StringUtils;

@SuppressWarnings("unused")
@UrlMapping(url = "/api")
public class ApiController extends JBaseController {

	public void index() {
		Boolean isOpen = Option.findValueAsBool("api_enable");
		if (isOpen == null || isOpen == false) {
			renderAjaxResult("api is not open", 1);
			return;
		}

		String appkey = getPara("appkey");
		if (!StringUtils.isNotBlank(appkey)) {
			renderAjaxResultForError("appkey must not empty!");
			return;
		}

		String sql = "select * from content where module = ? and text = ?";
		Content content = Content.DAO.findFirst(sql, Consts.MODULE_API_APPLICATION, appkey);
		if (content == null) {
			renderAjaxResultForError("appkey is error!");
			return;
		}

		String appSecret = content.getFlag();

		String sign = getPara("sign");
		if (!StringUtils.isNotBlank(sign)) {
			renderAjaxResultForError("sign must not empty!");
			return;
		}

		String sign_method = getPara("sign_method");
		if (!StringUtils.isNotBlank(sign_method)) {
			renderAjaxResultForError("sign_method must not empty!");
			return;
		}

		String method = getPara("method");
		if (!StringUtils.isNotBlank(method)) {
			renderAjaxResultForError("method must not empty!");
			return;
		}

		Map<String, String> params = new HashMap<String, String>();
		Map<String, String[]> oParams = getParaMap();
		if (oParams != null) {
			for (Map.Entry<String, String[]> entry : oParams.entrySet()) {
				String value = entry.getValue() == null ? "" : (entry.getValue()[0] == null ? "" : entry.getValue()[0]);
				params.put(entry.getKey(), value);
			}
		}
		params.remove("sign");

		String mySign = EncryptUtils.signForRequest(params, appSecret);
		if (!sign.equals(mySign)) {
			renderAjaxResultForError("sign is error!");
			return;
		}

		try {
			invoke(method);
		} catch (NoSuchMethodException e) {
			renderAjaxResultForError("hava no this method : " + method);
			return;
		} catch (Exception e) {
			renderAjaxResultForError("system error!");
			return;
		}
	}

	private void invoke(String methodName) throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Method method = ApiController.class.getDeclaredMethod(methodName);
		if (method == null) {
			throw new NoSuchMethodException();
		}
		method.setAccessible(true);
		method.invoke(this);
	}

	/////////////////////// api methods////////////////////////////
	private void test() {
		renderText("test ok!");
	}

}
