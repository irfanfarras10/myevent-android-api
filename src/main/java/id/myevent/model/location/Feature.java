package id.myevent.model.location;

import java.util.ArrayList;

/**
 * Feature Model.
 */
public class Feature {
  public String type;
  public Properties properties;
  public Geometry geometry;
  public ArrayList<Double> bbox;
}