package presto.android.gui.clients;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import presto.android.Configs;
import presto.android.Debug;
import presto.android.Logger;
import presto.android.gui.GUIAnalysisClient;
import presto.android.gui.GUIAnalysisOutput;
import presto.android.gui.clients.energy.EnergyAnalyzer;
import presto.android.gui.clients.energy.EnergyUtils;
import presto.android.gui.clients.energy.Pair;
import presto.android.gui.clients.energy.VarUtil;
import presto.android.gui.graph.*;
import presto.android.gui.wtg.EventHandler;
import presto.android.gui.wtg.StackOperation;
import presto.android.gui.wtg.WTGAnalysisOutput;
import presto.android.gui.wtg.WTGBuilder;
import presto.android.gui.wtg.ds.WTG;
import presto.android.gui.wtg.ds.WTGEdge;
import presto.android.gui.wtg.ds.WTGNode;
import presto.android.gui.wtg.flowgraph.NLauncherNode;
import soot.SootMethod;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zero on 10/21/15.
 */
public class WTGDemoClient implements GUIAnalysisClient {
  
  @Override
  public void run(GUIAnalysisOutput output){
    
    try{
        File file = new File("/Users/anshuman/Documents/workspace/BeginImplementation/WTG/WTG.xml");
        
       PrintStream out = new PrintStream(file);
   /*
      DocumentBuilderFactory dbFactory =DocumentBuilderFactory.newInstance();

      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.newDocument();
  */
    
    VarUtil.v().guiOutput = output;
    WTGBuilder wtgBuilder = new WTGBuilder();
    wtgBuilder.build(output);
    WTGAnalysisOutput wtgAO = new WTGAnalysisOutput(output, wtgBuilder);
    WTG wtg = wtgAO.getWTG();

    Collection<WTGEdge> edges = wtg.getEdges();
    Collection<WTGNode> nodes = wtg.getNodes();


   // Logger.verb("DEMO", "Application: " + Configs.benchmarkName);
    //Logger.verb("DEMO", "Launcher Node: " + wtg.getLauncherNode());
  //  pw.println(wtg.getLauncherNode().getWindow().toString());
  
    //pw.prinln(wtg.)
    
    out.print("<WindowsTransitionGraph>\n");
    
    for (WTGNode n : nodes){
    
    //  Element activity = doc.createElement("Activity");
        out.println("\t<Activity name=\""+actString(n.getWindow().toString())+"\" >");

      for(WTGEdge ed : n.getOutEdges()){
        if(actString(n.getWindow().toString()).contains(".Menu"))
        {
          if(String.valueOf(ed.getEventType()).compareTo("click")==0)
          {
            out.println("\t\t<View Type=\""+getEventType(ed.getGUIWidget().toString())+"\" Event=\""+ed.getEventType()+"\" Destination=\""+actString(ed.getTargetNode().getWindow().toString())+"\" ID=\""+getWidgetId(ed.getGUIWidget().toString())+"\" />");
          }
        }
        else
          out.println("\t\t<View Type=\""+getEventType(ed.getGUIWidget().toString())+"\" Event=\""+ed.getEventType()+"\" Destination=\""+actString(ed.getTargetNode().getWindow().toString())+"\" ID=\""+getWidgetId(ed.getGUIWidget().toString())+"\" />");
       
      }
      out.println("\t</Activity>");
      
    }
    out.print("</WindowsTransitionGraph>\n");
    out.flush();
    out.close();
/*
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File("/Users/anshumanrohella/Documents/workspace/BeginImplementation/TippyTipper-master/WTG/WTG.xml"));
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(source, result);
   */
  }catch(Exception e){e.printStackTrace();}
  }
  
  public String actString(String act_name){
    if(act_name.contains("LAUNCHER")){
      
      return act_name;
    }
    System.out.println("Received String :::"+act_name);
    
    String Pattern1= "";
    String Pattern2="";
    Matcher m = null;
    String ret="";
    try{
    
   
    if(act_name.startsWith("ACT")){
      
       Pattern1 = "ACT[";
       Pattern2 = "]";
       Pattern  p = Pattern.compile(Pattern.quote(Pattern1)+"(.*?)"+Pattern.quote(Pattern2));
       m = p.matcher(act_name);
        m.find();
        ret = m.group(1);
      
    }
    else if(act_name.startsWith("OptionsMenu"))
    {
      Pattern1 = "OptionsMenu[";
      Pattern2 =  "]";
      Pattern  p = Pattern.compile(Pattern.quote(Pattern1)+"(.*?)"+Pattern.quote(Pattern2));
      m = p.matcher(act_name);
      m.find();
      ret = m.group(1)+".Menu";
    }
    
    
    
    }catch(Exception e){e.printStackTrace();}
    
    return ret;
    
  }
  
  public String getEventType(String widget){
    if(widget.contains("LAUNCHER")){
      
      return widget;
    }
    System.out.println("Received widget :::"+widget);
    String ret ="";
    String Pattern1= "";
    String Pattern2="";
    Matcher m = null;
    try{

    if(widget.startsWith("INFL"))
    {
      Pattern1 = "INFL[";
      Pattern2 =  ","; 
     
    
  
    Pattern  p = Pattern.compile(Pattern.quote(Pattern1)+"(.*?)"+Pattern.quote(Pattern2));
    m = p.matcher(widget);
    m.find();
    ret = m.group(1);
    }
    else
      ret ="android.view.MenuItem";
    
    }catch(Exception e){e.printStackTrace();}
    
    return ret;
    
  }
  public String getWidgetId(String widget){
    if(widget.contains("LAUNCHER")){
      
      return widget;
    }
    System.out.println("Received widget :::"+widget);
    String ret = null;
    String Pattern1= "";
    String Pattern2="";
    Matcher m = null;
try{
    if(widget.startsWith("INFL") || widget.startsWith("MenuItemINFL"))
    {
      Pattern1 = "|";
      Pattern2 =  "]"; 
      Pattern  p = Pattern.compile(Pattern.quote(Pattern1)+"(.*?)"+Pattern.quote(Pattern2));
      m = p.matcher(widget);
      m.find();
      ret = m.group(1);
      
    }

    
    
    
    }catch(Exception e){e.printStackTrace(); return "nullId";}
    
    return ret;
    
  }
  
}
