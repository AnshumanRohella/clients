
package presto.android.gui.clients;

import presto.android.Configs;
import presto.android.gui.GUIAnalysisClient;
import presto.android.gui.GUIAnalysisOutput;
import presto.android.gui.clients.energy.VarUtil;
import presto.android.gui.wtg.WTGAnalysisOutput;
import presto.android.gui.wtg.WTGBuilder;
import presto.android.gui.wtg.ds.WTG;
import presto.android.gui.wtg.ds.WTGEdge;
import presto.android.gui.wtg.ds.WTGNode;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zero on 10/21/15.
 */
public class WTGDemoClient implements GUIAnalysisClient {

    ArrayList<String> mUnwantedEvents;


    PrintStream out;

    private ArrayList<String> Activities = new ArrayList<>();
    private Stack<WTGNode> mDialogStack;
    @Override
    public void run(GUIAnalysisOutput output){

        mUnwantedEvents = new ArrayList<>();
        addUnwantedEvents(mUnwantedEvents);
        try{

            System.out.println("Looking for a WTG for a folder");
            File file = null;
            if(!new File(Configs.project+"/WTG/WTG.xml").exists())
            {

                System.out.println("CREATING WTG.xml file");
                new File(Configs.project+"/WTG/").mkdir();
                file = new File(Configs.project+"/WTG/WTG.xml");
            }else
            {
                file = new File(Configs.project+"/WTG/WTG.xml");
            }

            
            out = new PrintStream(file);

            VarUtil.v().guiOutput = output;
            WTGBuilder wtgBuilder = new WTGBuilder();
            wtgBuilder.build(output);
            WTGAnalysisOutput wtgAO = new WTGAnalysisOutput(output, wtgBuilder);
            WTG wtg = wtgAO.getWTG();
            
            Collection<WTGEdge> edges = wtg.getEdges();
            Collection<WTGNode> nodes = wtg.getNodes();

            for(WTGNode n: nodes){
              String temp = parseNodeName(n.getWindow().toString());
              if(!temp.contains("Alert") && !Activities.contains(temp)){
                Activities.add(temp);
                System.out.println("Found and Added node activity :"+temp);
              
              }
            }
            
            
            out.print("<WindowsTransitionGraph>\n");
            
            for (WTGNode n : nodes){
                //    Element activity = doc.createElement("Activity");
                System.out.println("FOUND NODE STRING ::"+n.getWindow().toString());
                String node_tag_name = n.getWindow().toString();

                if(node_tag_name.startsWith("ACT[") || node_tag_name.startsWith("LAUNCHER")){

                        printForActivity(n);
                }
                else if(node_tag_name.startsWith("OptionsMenu[")){
                    printForMenu(n);
                }
                else
                {
                    System.out.println("NOT PRINTING FOR::"+n.getWindow().toString());
                    continue;
                }

            }
            out.print("</WindowsTransitionGraph>\n");
            out.flush();
            out.close();

            System.out.println("Created a WTG.xml file at: "+Configs.project+"/WTG/WTG.xml");

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



    private void printForDialogueNode(String callingActivity,WTGNode node){

        String node_name =  parseNodeName(node.getWindow().toString());

        out.println("\t<Activity name=\""+callingActivity+"_"+node_name+"\" >");

            for(WTGEdge ed : node.getOutEdges()){
                if(mUnwantedEvents.contains(ed.getEventType().toString())){
                    continue;
                }

                if(ed.getTargetNode().equals(node)){

                    out.println("\t\t<View Type=\"" + parseEventType(ed.getGUIWidget().toString()) + "\" Event=\"" + ed.getEventType() + "\" Destination=\"" + callingActivity+"_"+node_name+ "\" ID=\"" + "dia_" + parseWidgetId(ed.getGUIWidget().toString()) + "\" />");
                }else {
                    out.println("\t\t<View Type=\"" + parseEventType(ed.getGUIWidget().toString()) + "\" Event=\"" + ed.getEventType() + "\" Destination=\"" + parseNodeName(ed.getTargetNode().getWindow().toString()) + "\" ID=\"" + "dia_" + parseWidgetId(ed.getGUIWidget().toString()) + "\" />");
                }
            }
        out.println("\t</Activity>");
    }


    private void printForActivity(WTGNode node){
        mDialogStack = new Stack<>();
        String node_name =  parseNodeName(node.getWindow().toString());

        out.println("\t<Activity name=\""+node_name+"\" >");

        for(WTGEdge ed : node.getOutEdges()){

            System.out.println("Printing Event String "+ed.getEventType().toString());
            if(mUnwantedEvents.contains(ed.getEventType().toString())){
                continue;
            }
            // Case check for destination as dialog.
            if(ed.getTargetNode().getWindow().toString().startsWith("DIALOG[")){

                mDialogStack.push(ed.getTargetNode());
                out.println("\t\t<View Type=\""+ parseEventType(ed.getGUIWidget().toString())+"\" Event=\""+ed.getEventType()+"\" Destination=\""+node_name+"_"+ parseNodeName(ed.getTargetNode().getWindow().toString())+"\" ID=\""+ parseWidgetId(ed.getGUIWidget().toString())+"\" />");

            }
            else
                out.println("\t\t<View Type=\""+ parseEventType(ed.getGUIWidget().toString())+"\" Event=\""+ed.getEventType()+"\" Destination=\""+ parseNodeName(ed.getTargetNode().getWindow().toString())+"\" ID=\""+ parseWidgetId(ed.getGUIWidget().toString())+"\" />");

        }
        out.println("\t</Activity>");

            while(!mDialogStack.isEmpty()){
                printForDialogueNode(node_name,mDialogStack.pop());
            }

    }

    private void printForMenu(WTGNode node){

        mDialogStack =  new Stack<>();
        String node_name = parseNodeName(node.getWindow().toString());

        out.println("\t<Activity name=\""+node_name+"\" >");

        for(WTGEdge ed:node.getOutEdges()) {


            if(String.valueOf(ed.getEventType()).compareTo("click") == 0) {
                if(ed.getTargetNode().getWindow().toString().startsWith("DIALOG[")){
                    mDialogStack.push(ed.getTargetNode());
                    out.println("\t\t<View Type=\""+ parseEventType(ed.getGUIWidget().toString())+"\" Event=\""+ed.getEventType()+"\" Destination=\""+node_name+"_"+ parseNodeName(ed.getTargetNode().getWindow().toString())+"\" ID=\""+ parseWidgetId(ed.getGUIWidget().toString())+"\" />");

                }
                else {
                    out.println("\t\t<View Type=\"" + parseEventType(ed.getGUIWidget().toString()) + "\" Event=\"" + ed.getEventType() + "\" Destination=\"" + parseNodeName(ed.getTargetNode().getWindow().toString()) + "\" ID=\"" + parseWidgetId(ed.getGUIWidget().toString()) + "\" />");
                }
            }
        }
        out.println("\t</Activity>");

        while(!mDialogStack.isEmpty()){
            printForDialogueNode(node_name,mDialogStack.pop());
        }

    }

    private String parseNodeName(String act_name){
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
            else if(act_name.startsWith("DIALOG")){
                Pattern1 = "DIALOG[";
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
    
    public String parseEventType(String widget){
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



    public String parseWidgetId(String widget){
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

    private void addUnwantedEvents(ArrayList<String> unwanted_Events){
        unwanted_Events.add("implicit_home_event");
        unwanted_Events.add("implicit_power_event");
        unwanted_Events.add("implicit_home_event");
        unwanted_Events.add("implicit_on_activity_result");


    }
    
}

