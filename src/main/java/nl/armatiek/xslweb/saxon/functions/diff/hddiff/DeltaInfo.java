package nl.armatiek.xslweb.saxon.functions.diff.hddiff;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DeltaInfo {
  
  private boolean insertInfo;
  private boolean deleteInfo;
  private TextInfo textInfo;
  private AttrInfo attrInfo;
    
  public void addInsertInfo(boolean value) {
    insertInfo = value;
  }
  
  public void addDeleteInfo(boolean value) {
    deleteInfo = value;
  }
   
  public void addTextInfo(String oldValue, String newValue) {
    textInfo = new TextInfo(oldValue, newValue);
  }
  
  public void addAttrInfo(Element elem, NamedNodeMap newAttrs) {
    attrInfo = new AttrInfo(elem, newAttrs);
  }
  
  public boolean hasInsertInfo() {
    return insertInfo;
  }
  
  public boolean hasDeleteInfo() {
    return deleteInfo;
  }
  
  public TextInfo getTextInfo() {
    return textInfo;
  }
  
  public AttrInfo getAttrInfo() {
    return attrInfo;
  }
  
  /*
  public boolean hasDeleteInfo() {
    return deletes != null && !deletes.isEmpty();
  }
  */
  
  public boolean hasTextInfo() {
    return textInfo != null;
  }
  
  public boolean hasAttrInfo() {
    return attrInfo != null;
  }
  
  public static final class DeleteInfo {
    
    public final Node deletedNode;
    public Node nextSiblingNode;
    public Node prevSiblingNode;
    public final int position;

    public DeleteInfo(Node deletedNode, Node nextSiblingNode, Node prevSiblingNode, int position) {
      this.deletedNode = deletedNode;
      this.nextSiblingNode = nextSiblingNode;
      this.prevSiblingNode = prevSiblingNode;
      this.position = position;
    }
    
  }
  
  public static final class TextInfo {
    
    public String oldValue;
    public String newValue;

    public TextInfo(String oldValue, String newValue) {
      this.oldValue = oldValue;
      this.newValue = newValue;
    }
    
  }
    
  public static final class AttrInfo {
    
    public Element elem;
    public NamedNodeMap newAttrs;

    public AttrInfo(Element elem, NamedNodeMap newAttrs) {
      this.elem = (Element) elem.cloneNode(false);
      this.newAttrs = newAttrs;
    }
    
  }

}