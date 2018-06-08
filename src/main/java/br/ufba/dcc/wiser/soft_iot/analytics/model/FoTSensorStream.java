/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.soft_iot.analytics.model;



import br.ufba.dcc.wiser.soft_iot.analytics.util.UtilDebug;
import br.ufba.dcc.wiser.soft_iot.tatu.TATUWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.edgent.connectors.mqtt.MqttConfig;
import org.apache.edgent.connectors.mqtt.MqttStreams;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;


/**
 *
 * @author brenno
 */
public class FoTSensorStream {
    
    private String Sensorid;
    private String type;
    private int collectionTime;
    private int publishingTime;
    private MqttStreams connector;
    private Topology topology;
    private String topicPrefix = "";
    private int qos;
    private FoTDeviceStream fotDeviceStream; 
    
    public FoTSensorStream(Topology topology, MqttConfig mqttConfig, String Sensorid, FoTDeviceStream fotDeviceStream){
        this.topology = topology;
        this.Sensorid = Sensorid;
        this.fotDeviceStream = fotDeviceStream;
	UtilDebug.printDebugConsole(mqttConfig.getServerURLs()[0]);
        this.connector = new MqttStreams(topology, mqttConfig.getServerURLs()[0], Sensorid);
        
        if(this.connector == null)
            throw new ExceptionInInitializerError("Error starting sensor");
        this.qos = 0;
        initGetSensorData();
    }   
    
    
    public String getSensorid() {
        return Sensorid;
    }

    public void setSensorid(String Sensorid) {
        this.Sensorid = Sensorid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(int collectionTime) {
        this.collectionTime = collectionTime;
    }

    public int getPublishingTime() {
        return publishingTime;
    }

    public void setPublishingTime(int publishingTime) {
        this.publishingTime = publishingTime;
    }

    public MqttStreams getConnector() {
        return connector;
    }

    public void setConnector(MqttStreams connector) {
        this.connector = connector;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topology) {
        this.topology = topology;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public FoTDeviceStream getFotDeviceStream() {
        return fotDeviceStream;
    }

    public void setFotDeviceStream(FoTDeviceStream fotDeviceStream) {
        this.fotDeviceStream = fotDeviceStream;
    }
	
   private void initGetSensorData(){
       UtilDebug.printDebugConsole(TATUWrapper.topicBase + this.fotDeviceStream.getDeviceId() + "/#");
       TStream<String> tStream = this.connector.subscribe(TATUWrapper.topicBase + this.fotDeviceStream.getDeviceId() + "/#", this.qos);
       
       //tStream.print();
       
       TStream<List<SensorData>> tempObj = tStream.map(tuple -> {
                    List<SensorData> listData = new ArrayList<SensorData>();
                    try{
                        
                        if(TATUWrapper.isValidTATUAnswer(tuple)){
                                
                                
                            JsonParser parser = new JsonParser();

                            JsonElement element = parser.parse(tuple);
                            JsonObject jObject = element.getAsJsonObject();


                            JsonObject body = jObject.getAsJsonObject("BODY");

                            JsonArray jsonArray = body.getAsJsonArray(this.Sensorid);

                            if(jsonArray != null){
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JsonElement jsonElement = jsonArray.get(i);
                                    String value = String.valueOf(jsonElement.getAsDouble());
                                    SensorData sensorData = new SensorData(value, LocalDateTime.MIN, this, fotDeviceStream);    
                                }
                            }

                            System.out.println(jsonArray.get(0));

                        }
                        
                        
                    }catch(Exception e){
                        System.out.println("Erro parser: " + e.getMessage());
                    }        
			return null;
		});
       
       tempObj.print();
       
       /*
       TStream<List<SensorData>> tStreamData = tStream.map((tuple) -> {
                                                
                                                List<SensorData> listSensorData = parseTatuMessage(tuple);
                                                return listSensorData; 
                                            });
       
       
       
       tStream.print();
       */
   }
   
   public List<SensorData> parseTatuMessage(String message){
       List<SensorData> listSensorData = new ArrayList<SensorData>();
		try{
                     JsonParser parser = new JsonParser();
                     JsonElement element = parser.parse(message);
                     
                     
                        /*
			JSONObject json = new JSONObject(message);
			JSONArray sensorValues = json.getJSONObject("BODY").getJSONArray(
					sensor.getId());
			int collectTime = json.getJSONObject("BODY").getJSONObject("FLOW")
					.getInt("collect");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(baseDate);
			for (int i = 0; i < sensorValues.length(); i++) {
				Integer valueInt = sensorValues.getInt(i);
				String value = valueInt.toString();
				SensorData sensorData = new SensorData(device, sensor, value, calendar.getTime(), calendar.getTime());
				listSensorData.add(sensorData);
				calendar.add(Calendar.MILLISECOND, collectTime);
			}
                        */
                        
		}catch(org.json.JSONException e){
                    
		}
		return listSensorData;
   }
   
   public String getDeviceTopic(){
       
       return null;
   }
	
}
