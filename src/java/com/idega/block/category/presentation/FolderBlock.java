package com.idega.block.category.presentation;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import com.idega.block.category.business.FolderBlockBusiness;
import com.idega.block.category.data.InformationCategory;
import com.idega.block.category.data.InformationFolder;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.component.data.ICObject;
import com.idega.core.component.data.ICObjectHome;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.localisation.data.ICLocale;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;

/**
 *  <p>
 *  Title: idegaWeb</p> <p>
 *  Description: </p> <p>
 *  Copyright: Copyright (c) 2002</p> <p>
 *  Company: idega</p>
 *
 *@author     <a href="gummi@idega.is">Gudmundur Agust Saemundsson</a>
 *@created    15. mars 2002
 *@version    1.0
 */
public class FolderBlock extends Block {

	private boolean _useLocalizedFolders = true;
	private InformationFolder _workFolder = null;
	private InformationFolder[] _visibleFolders = null;
	private InformationCategory[] _categoriesForInstance = null;
	private boolean _autocreate = true;
	private String _contentLocaleIdentifier = null;
	FolderBlockBusiness _business= null;
	
	private int _blockInstanceID = -1;
	private int _blockObjectID = -1;
	private boolean _utilisesCategories = true;
	

	/**
	 *  Constructor for the FolderBlock object
	 */
	public FolderBlock() {
	}
	
	public void useLocalizedFolders(boolean use){
		this._useLocalizedFolders = use;
	}
	

	/**
	 *  Gets the workFolderId attribute of the FolderBlock object
	 *
	 *@return    The workingFolderId value
	 *@deprecated rather use getWorkFolder()
	 */
	public int getWorkFolderID() {
		return this._workFolder.getID();
	}

	/**
	 *  Gets the workingFolder attribute of the FolderBlock object
	 *
	 *@return    The workingFolder value
	 */
	public InformationFolder getWorkFolder() {
		return this._workFolder;
	}

	/*
	 *  public InformationFolder[] getFoldersToView(){
	 *  return _viewFolders;
	 *  }
	 */

	/**
	 *  Gets the categoriesToView attribute of the FolderBlock object
	 *
	 *@return    The categoriesToView value
	 */
	public InformationCategory[] getCategoriesToView() {
		return this._categoriesForInstance;
	}

	/**
	 *  Sets the work folder attribute of the FolderBlock object
	 *
	 *@param  folder  The new work folder value
	 */
	public void setWorkFolder(InformationFolder folder) {
		this._workFolder = folder;
	}

	/**
	 *  Sets the autoCreate attribute of the FolderBlock object
	 *
	 *@param  autocreate  The new autoCreate value
	 */
	public void setAutoCreate(boolean autocreate) {
		this._autocreate = autocreate;
	}

	/**
	 *  Sets the contentLocaleIdentifier attribute of the FolderBlock object
	 *
	 *@param  identifier  The new contentLocaleIdentifier value
	 */
	public void setContentLocaleIdentifier(String identifier) {
		this._contentLocaleIdentifier = identifier;
	}

	/**
	 *  Gets the contentLocaleIdentifier attribute of the FolderBlock object
	 *
	 *@return    The contentLocaleIdentifier value
	 */
	public String getContentLocaleIdentifier() {
		return this._contentLocaleIdentifier;
	}

	/**
	 *  Sets the contentLocaleDependent attribute of the FolderBlock object
	 */
	public void setContentLocaleDependent() {
		this._contentLocaleIdentifier = null;
	}

	
	public FolderBlockBusiness getBlockBusinessInstance(IWApplicationContext iwac) throws IBOLookupException{
		if(this._business == null){
			this._business = (FolderBlockBusiness) IBOLookup.getServiceInstance(iwac,getBlockBusinessClass());
		}
		return this._business;
	}
	
	public Class getBlockBusinessClass(){
		return FolderBlockBusiness.class;
	}
	
	/**
	 *  Description of the Method
	 *
	 *@param  iwc            Description of the Parameter
	 *@exception  Exception  Description of the Exception
	 */
	public void _main(IWContext iwc) throws Exception {
		if (getBlockInstanceID() > 0) {
			FolderBlockBusiness business = getBlockBusinessInstance(iwc);
			int localeId = -1;
			if (getContentLocaleIdentifier() != null) {
				ICLocale locale = ICLocaleBusiness.getICLocale(getContentLocaleIdentifier());
				//getContentLocaleIdentifier();
				if (locale != null) {
					localeId = locale.getLocaleID();
				}
			}
			if (localeId == -1) {
				localeId = iwc.getCurrentLocaleId();
			}
			InformationFolder folder = business.getInstanceWorkeFolder(getBlockInstanceID(), getBlockObjectID(), localeId, this._autocreate);
			if (folder != null) {
				if(this._useLocalizedFolders){
					setWorkFolder(folder);
				} else {
					setWorkFolder(folder.getParent());
				}
				
			}
			List infoCategories = business.getInstanceCategories(getBlockInstanceID());
			if (infoCategories != null && infoCategories.size() > 0) {
				this._categoriesForInstance = new InformationCategory[infoCategories.size()];
				int pos = 0;
				Iterator iter = infoCategories.iterator();
				while (iter.hasNext()) {
					InformationCategory item = (InformationCategory)iter.next();
					this._categoriesForInstance[pos] = item;
					pos++;
				}
			} else {
				if(utilisesCategories() && !business.hasAvailableCategory(this.getBlockObjectID())){
					createDefaultCategories(iwc);
				}				
				this._categoriesForInstance = new InformationCategory[0];
			}
		}
		super._main(iwc);
	}

	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public synchronized Object clone() {
		FolderBlock obj = null;
		try {
			obj = (FolderBlock)super.clone();
			obj._workFolder = this._workFolder;
			obj._visibleFolders = this._visibleFolders;
			obj._categoriesForInstance = this._categoriesForInstance;
			obj._autocreate = this._autocreate;
			obj._blockInstanceID = this._blockInstanceID;
			obj._blockObjectID = this._blockObjectID;
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		return obj;
	}
	
	public boolean deleteBlock(int ICObjectInstanceId){
		try {
			return ((FolderBlockBusiness)IBOLookup.getServiceInstance(getIWApplicationContext(),FolderBlockBusiness.class)).detachWorkfolderFromObjectInstance(this.getICObjectInstance());
		} catch (IBOLookupException e) {
			e.printStackTrace();
			return false;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	/**
	 *  Returns a Link to FolderBlockCategoryWindow
	 */
	public Link getCategoryLink(){
		Link L = new Link();
		//L.addParameter(FolderBlockCategoryWindow.prmCategoryId,getCategoryId());
		L.addParameter(FolderBlockCategoryWindow.prmObjInstId,getBlockInstanceID());
		L.addParameter(FolderBlockCategoryWindow.prmObjId,getBlockObjectID());
		L.addParameter(FolderBlockCategoryWindow.prmWorkingFolder,this.getWorkFolderID());
//		if(getMultible()) {
			L.addParameter(FolderBlockCategoryWindow.prmMulti,"true");
//		}
//		if (orderManually) {
//			L.addParameter(CategoryWindow.prmOrder, "true");
//		}

		L.setWindowToOpen(FolderBlockCategoryWindow.class);
		return L;
	}
	
	
	public boolean createDefaultCategories(IWContext iwc){
		try {
			this.getBlockBusinessInstance(iwc).createICInformationCategory(iwc,iwc.getCurrentLocaleId(),"Default","Default category",null,getBlockObjectID(),-1);
			return true;
		} catch (IBOLookupException e) {
			e.printStackTrace();
			return false;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public int getBlockInstanceID(){
		if(this._blockInstanceID == -1){
			this._blockInstanceID = this.getICObjectInstanceID();
		}
		return this._blockInstanceID;
	}
	
	public int getBlockObjectID(){
		if(this._blockObjectID == -1){
			try {
				ICObject object = this.getICObjectHome().findByClassName(this.getClassName());
				this._blockObjectID = ((Integer) object.getPrimaryKey()).intValue();
			} catch (Exception e) {
				e.printStackTrace();
				this._blockObjectID = this.getICObjectID();
				
			}			
		}
		return this._blockObjectID;
	}
	
	public void setBlockInstanceID(int id){
		this._blockInstanceID = id;
	}

	public void setBlockObjectID(int id){
		this._blockObjectID = id;
	}
	
	
	
	public boolean utilisesCategories() {
		return this._utilisesCategories;
	}

	public void utiliseCategories(boolean value) {
		this._utilisesCategories = value;
	}

	private ICObjectHome getICObjectHome() throws RemoteException {
		return (ICObjectHome) IDOLookup.getHome(ICObject.class);
	}
	
}