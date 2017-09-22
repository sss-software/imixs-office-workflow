package com.imixs.workflow.office.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.workflow.ChildWorkitemController;
import org.imixs.marty.workflow.FormController;
import org.imixs.marty.workflow.FormDefinition;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.exceptions.QueryException;

/**
 * The MinuteController is a session scoped backing bean to provide a list of
 * team member profiles. The controller provides alos methods to add or remove a
 * entry into the member list
 * 
 * @author rsoika
 * 
 */
@Named
@ConversationScoped
public class MinuteController extends ChildWorkitemController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MinuteController.class.getName());

	protected FormController formController = null;

	private FormDefinition formDefinition = null;;

	public MinuteController() {
		super();
		formController = new FormController();
	}

	/**
	 * This method overwrites the behavior of the childWorkitemController. The
	 * method returns all minutes sorted by numSequenceNumber
	 * 
	 * @return - sorted list of minutes
	 */
	@Override
	public List<ItemCollection> loadWorkitems() {
		logger.fine("load minute list...");
		List<ItemCollection> minutes = new ArrayList<ItemCollection>();

		if (getParentWorkitem() != null) {
			String uniqueIdRef = getParentWorkitem().getItemValueString(WorkflowKernel.UNIQUEID);
			if (!uniqueIdRef.isEmpty()) {
				String sQuery = null;

				sQuery = "( (type:\"workitem\" OR type:\"childworkitem\" OR type:\"workitemarchive\" OR type:\"childworkitemarchive\") ";
				sQuery += " AND ($uniqueidref:\"" + uniqueIdRef + "\")) ";

				try {
					minutes = this.getDocumentService().find(sQuery, 999, 0);
					// sort by numsequencenumber
					Collections.sort(minutes, new ItemCollectionComparator("numsequencenumber", true));
				} catch (QueryException e) {
					logger.warning("loadWorkitems - invalid query: " + e.getMessage());
				}

			}
		}
		return minutes;
	}

	public FormDefinition getFormDefinition() {
		return formDefinition;
	}

	/**
	 * This toggle method will either load a new child workitem or reset the current
	 * 
	 * @param id
	 */
	public void toggleWorkitem(String id) {
		if (this.getWorkitem().getUniqueID().equals(id)) {
			// reset
			this.setWorkitem(null);
		} else {
			// load by id
			super.load(id);

			// update formDefinition
			formDefinition = formController.computeFormDefinition(getWorkitem());
		}
	}

}
