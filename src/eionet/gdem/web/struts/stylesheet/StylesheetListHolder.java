package eionet.gdem.web.struts.stylesheet;
import java.util.List;

public class StylesheetListHolder {

	private List handCodedStylesheets;
	private List ddStylesheets;
	boolean ssiPrm;
	boolean ssdPrm;
	boolean convPrm;
	boolean handcoded;
	

	public StylesheetListHolder() {
	}


	public List getDdStylesheets() {
		return ddStylesheets;
	}
	


	public void setDdStylesheets(List ddStylesheets) {
		this.ddStylesheets = ddStylesheets;
	}
	


	public List getHandCodedStylesheets() {
		return handCodedStylesheets;
	}
	


	public void setHandCodedStylesheets(List handCodedStylesheets) {
		this.handCodedStylesheets = handCodedStylesheets;
	}


	public boolean isSsiPrm() {
		return ssiPrm;
	}
	


	public void setSsiPrm(boolean ssiPrm) {
		this.ssiPrm = ssiPrm;
	}


	public boolean isSsdPrm() {
		return ssdPrm;
	}
	


	public void setSsdPrm(boolean ssdPrm) {
		this.ssdPrm = ssdPrm;
	}


	public boolean isConvPrm() {
		return convPrm;
	}
	


	public void setConvPrm(boolean convPrm) {
		this.convPrm = convPrm;
	}


	public boolean isHandcoded() {
		return handcoded;
	}
	


	public void setHandcoded(boolean handcoded) {
		this.handcoded = handcoded;
	}
	



	
	
	
	

	

}
