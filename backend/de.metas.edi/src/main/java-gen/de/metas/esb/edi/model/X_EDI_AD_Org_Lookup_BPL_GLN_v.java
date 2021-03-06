/** Generated Model - DO NOT CHANGE */
package de.metas.esb.edi.model;

import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for EDI_AD_Org_Lookup_BPL_GLN_v
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_EDI_AD_Org_Lookup_BPL_GLN_v extends org.compiere.model.PO implements I_EDI_AD_Org_Lookup_BPL_GLN_v, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = -1523469948L;

    /** Standard Constructor */
    public X_EDI_AD_Org_Lookup_BPL_GLN_v (Properties ctx, int EDI_AD_Org_Lookup_BPL_GLN_v_ID, String trxName)
    {
      super (ctx, EDI_AD_Org_Lookup_BPL_GLN_v_ID, trxName);
      /** if (EDI_AD_Org_Lookup_BPL_GLN_v_ID == 0)
        {
        } */
    }

    /** Load Constructor */
    public X_EDI_AD_Org_Lookup_BPL_GLN_v (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }


    /** Load Meta Data */
    @Override
    protected org.compiere.model.POInfo initPO (Properties ctx)
    {
      org.compiere.model.POInfo poi = org.compiere.model.POInfo.getPOInfo (ctx, Table_Name, get_TrxName());
      return poi;
    }

	/** Set GLN.
		@param GLN GLN	  */
	@Override
	public void setGLN (java.lang.String GLN)
	{
		set_Value (COLUMNNAME_GLN, GLN);
	}

	/** Get GLN.
		@return GLN	  */
	@Override
	public java.lang.String getGLN () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_GLN);
	}
}