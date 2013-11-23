package nl.armatiek.xslweb.pipeline;

public class StylesheetParameter {
  
  private String uri;
  private String name;
  private String value;
  
  public StylesheetParameter(String uri, String name, String value) {
    this.uri = uri;
    this.name = name;
    this.value = value;
  }
  
  public String getUri() {
    return uri;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

}
