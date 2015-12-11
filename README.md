# Set Top Box Demonstration 

This demonstration simulates real time set top box data capture with simple event processing and real time search & discovery.

<i>Products used: Java, Kafka, SOLR/Banana, NiFi</i>

-----------
Setup
-----------

1) Get Hortonworks Sandbox
     
2) Install NiFi (https://github.com/abajwa-hw/ambari-nifi-service).  If you're using VirtualBox, make sure you port forward 9090 when your install is complete. 

3) Start SOLR from /root 

     ./start_solr.sh

4) Clone this repository in /root

     git clone https://github.com/chriscasano/SetTopBox.git

5) Change directory into the cloned repo

     cd SetTopBox

6) If not using /root/SetTopBox as primary directory; update DemoData.properties file.  Namely, these settings...

     settopboxdemo.data.input.program_guide=/root/SetTopBox/DemoData_ProgramGuide.csv
     settopboxdemo.kafka.input_file=/root/SetTopBox/input_test.txt
     settopboxdemo.data.output.events=/root/SetTopBox/input_test.txt

----------------
NIFI Setup
----------------

7) Load NiFi settopbox template into NiFi.  File is located in /root/SetTopBox/nifi/NiFi_SetTopBox.xml.  Load the template from the NiFi UI and then drag the SetTopBox template onto the canvas.

8) Once loaded, start NiFi processors.

----------------
SOLR Setup
----------------

9) Create settopbox SOLR index: 

     curl "http://127.0.0.1:8983/solr/admin/cores?action=CREATE&name=settopbox&instanceDir=/opt/lucidworks-hdpsearch/solr/server/solr/settopbox&configSet=data_driven_schema_configs"

Make sure a response status of 0 is returned.

10) Copy the Set Top Box dashboard into the Solr Banana web app.

	mv /opt/lucidworks-hdpsearch/solr/server/solr-webapp/webapp/banana/app/dashboards/default.json /opt/lucidworks-hdpsearch/solr/server/solr-webapp/webapp/banana/app/dashboards/default.json.bkp
	cp /root/SetTopBox/banana/SetTopBox_Dashboard.json /opt/lucidworks-hdpsearch/solr/server/solr-webapp/webapp/banana/app/dashboards/default.json

11) Validate the dashboard renders: http://127.0.0.1:8983/solr/banana/index.html#/dashboard 

-----------------
Kafka Setup 
-----------------     

12) Create settopbox topic

	sh /usr/hdp/current/kafka-broker/bin/kafka-topics.sh --create --topic settopbox --zookeeper 127.0.0.1:2181 --partitions 1 --replication-factor 1

-----------------
Create Data
-----------------     

13) From /root/SetTopBox, run the following.  This should create an input_text.txt file in /root/settopbox with some sample set top box data/

     java -cp SetTopBox.jar com.hortonworks.settopboxdemo.DataGenerator DemoData.properties

---------------
<b>Run Demo</b>
--------------

To run the demo, the MessageSender class in the SetTopBox jar will incrementally push records into Kafka, thru Nifi and ultimately land in SOLR.  MessageSender simulates a set top box end point, Kafka is obviously the message broker, NiFi is the router of these data streams into SOLR were real time event analytics can be visualized and searched upon.

     java -cp SetTopBox.jar com.hortonworks.settopboxdemo.MessageSender DemoData.properties

--- EXTRAS ---

If you need to delete the SOLR index, run the following commands: 

     curl "http://localhost:8983/solr/admin/cores?action=UNLOAD&core=settopbox&deleteIndex=true"
