package com.choncms.webpage.forms.ext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chon.cms.core.Extension;
import org.chon.cms.core.JCRApplication;
import org.chon.cms.model.content.IContentNode;
import org.chon.web.api.Request;
import org.chon.web.api.Response;
import org.chon.web.mpac.Action;
import org.chon.web.util.FileInfo;
import org.json.JSONException;
import org.json.JSONObject;

import com.choncms.webpage.forms.WorkflowUtils;
import com.choncms.webpage.forms.actions.AjaxDeleteAction;
import com.choncms.webpage.forms.actions.EditAction;
import com.choncms.webpage.forms.actions.ListAction;
import com.choncms.webpage.forms.actions.SaveAction;
import com.choncms.webpage.forms.actions.ViewSimpleSaveData;
import com.choncms.webpage.forms.workflow.Workflow;
import com.choncms.webpage.forms.workflow.WorkflowResult;

public class FormsExtension implements Extension {
	private static final Log log = LogFactory.getLog(FormsExtension.class);
	
	private Map<String, Action> actions = new HashMap<String, Action>();
	Map<String, Action> ajaxActions = new HashMap<String, Action>();
	private IContentNode appFormDataNode;
	private String prefix;
	private JCRApplication app;
	private String ajaxFormSubmitNode;
	
	public FormsExtension(JCRApplication app, String prefix, IContentNode appFormDataNode, String ajaxFormSubmitNode) {
		this.app = app;
		this.prefix = prefix;
		this.appFormDataNode = appFormDataNode;
		this.ajaxFormSubmitNode = ajaxFormSubmitNode;
		
		actions.put(prefix + ".list", new ListAction(prefix, appFormDataNode));
		actions.put(prefix + ".edit", new EditAction(prefix, appFormDataNode));
		actions.put(prefix + ".save", new SaveAction(prefix, appFormDataNode));
		actions.put(prefix + ".viewData", new ViewSimpleSaveData(prefix, appFormDataNode));
		
		ajaxActions.put(prefix + ".delete", new AjaxDeleteAction(prefix, appFormDataNode));
	}

	public String getAjaxFormSubmitNode() {
		return ajaxFormSubmitNode;
	}

	@Override
	public Map<String, Action> getAdminActons() {
		return actions;
	}

	@Override
	public Map<String, Action> getAjaxActons() {
		return ajaxActions;
	}

	@Override
	public Object getTplObject(Request req, Response resp, IContentNode node) {
		return new FormsFE(app, prefix, req, resp, appFormDataNode, this);
	}

	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> processFormSubmition(IContentNode formNode, Request req) {
		Map<String, Object> params = req.getServletRequset().getParameterMap();
		Map<String, Object> formData = new HashMap<String, Object>();
		formData.put("ctx", new HashMap<String, Object>());
		
		log.debug("Processing Form Submition for " + formNode.getName());
		for(String k : params.keySet()) {
			Object v = params.get(k);
			if(v.getClass().isArray()) {
				Object [] arr = (Object []) v;
				if(arr != null && arr.length==1) {
					v = arr[0];
				}
			}
			
			if(k.startsWith("__")) {
				((Map<String, Object>)formData.get("ctx")).put(k.substring(2), v);
			} else {
				formData.put(k, v);
			}
		}
		List<FileInfo> files = req.getFiles();
		if(files != null && files.size() > 0) {
			formData.put("FILES", files);
		}
		
		Workflow workflow = getWorkfow(formNode);
		
		try {
			JSONObject cfg = getWorkflowConfigInFormNode(formNode);
			WorkflowResult rv = workflow.process(formNode, formData, cfg);
			
			((Map<String, Object>)formData.get("ctx")).put("workflowResult", rv);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return formData;
	}
	
	public static JSONObject getWorkflowConfigInFormNode(IContentNode formNode) throws JSONException {
		String workflowConfig = formNode.get("workflowConfig");
		JSONObject cfg = new JSONObject(workflowConfig);
		return cfg;
	}

	private static Workflow getWorkfow(IContentNode formNode) {
		return WorkflowUtils.getWorkflow(formNode.get("workflow"));
	}
}
