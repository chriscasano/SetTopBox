# Set Top Box Demonstration 

This demonstration simulates real time set top box data capture with simple event processing and real time search & discovery.

<i>Products used: Java, Kafka, SOLR/Banana, NiFi</i>

-----------
Setup
-----------

1) Get Hortonworks Sandbox
     Make sure Kafka port is setup in port fowarding of VM.  should be 6667
     
2) Install NiFi (https://github.com/abajwa-hw/ambari-nifi-service).  If you're using VirtualBox, make sure you port forward 9090 when your install is complete. 

3) Start SOLR from /root 

     ./start_solr.sh

4) Clone this repository in /root

     git clone https://github.com/chriscasano/SetTopBox.git

5) Change directory into the repo

     cd SetTopBox

6) If not using /root/SetTopBox as primary directory; update DemoData.properties file.

     settopboxdemo.data.input.program_guide=/root/SetTopBox/DemoData_ProgramGuide.csv
     settopboxdemo.kafka.input_file=/root/SetTopBox/input_test.txt
     settopboxdemo.data.output.events=/root/SetTopBox/input_test.txt

----------------
NIFI Setup
----------------

7) Load NiFi settopbox template into NiFi.  File is located in /root/settopbox/nifi/NiFi_SetTopBox.xml.  Load the template from the NiFi UI and then drag the SetTopBox template onto the canvas.

8) Once loaded, start NiFi processors.

----------------
SOLR Setup
----------------

9) Create settopbox SOLR index: 

     curl "http://127.0.0.1:8983/solr/admin/cores?action=CREATE&name=settopbox&instanceDir=/opt/lucidworks-hdpsearch/solr/server/solr/settopbox&configSet=data_driven_schema_configs"

10) In Banana dashboard ( http://127.0.0.1:8983/solr/banana/index.html#/dashboard ), load the "Set Top Box Events - ##########" file in /root/settopbox/banana directory

11) In banana dashboard settings (gear box in upper right), make sure SOLR config has server = “/solr/“ and collection = “settopbox"

-----------------
Kafka Setup 
-----------------     

12) Create settopbox topic

     kafka-topics.sh --create settopbox ....

-----------------
Create Data
-----------------     

13) From /root/settopbox, run the following.  This should create an input_text.txt file in /root/settopbox with some sample set top box data/

     java -cp SetTopBox.jar com.hortonworks.settopboxdemo.DataGenerator DemoData.properties

---------------
<b>Run Demo</b>
--------------

The run the demo, the MessageSender class in the SetTopBox jar will incrementatlly push records into Kafka, thru Nifi and ultimately land in SOLR.  MessageSender simulates a set top box end point, Kafka is obviously the message broker, NiFi is the router of these data streams into SOLR were real time event analytics can be visualized and searched upon.

     java -cp SetTopBox.jar com.hortonworks.settopboxdemo.MessageSender DemoData.properties

--- EXTRAS ---

If you need to delete the SOLR index, run the following commands: 

     curl "http://localhost:8983/solr/admin/cores?action=UNLOAD&core=settopbox&deleteIndex=true"
