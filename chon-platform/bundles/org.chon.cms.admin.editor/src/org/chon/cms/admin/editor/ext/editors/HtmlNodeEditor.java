package org.chon.cms.admin.editor.ext.editors;

import org.chon.cms.admin.editor.model.NodeEditor;
import org.chon.cms.model.content.IContentNode;

public class HtmlNodeEditor implements NodeEditor {

	@Override
	public String getTemplate(IContentNode node) {
		return "admin/editor/html/edit.html";
	}

}
