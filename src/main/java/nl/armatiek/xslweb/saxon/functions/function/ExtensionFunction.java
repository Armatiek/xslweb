package nl.armatiek.xslweb.saxon.functions.function;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionFunction {
  
  String uri() default "urn:local";
  String name();
  boolean hasSideEffects() default false;
  
}