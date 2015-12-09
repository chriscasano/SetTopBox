# SetTopBox

1) Get Sandbox
     Make sure Kafka port is setup in port fowarding of VM.  should be 6667
2) Install NiFi (https://github.com/abajwa-hw/ambari-nifi-service)
3) Start SOLR from /root ./start_solr.sh
4) Create /root/settopbox directory
5) Copy Jars/Config to VM: scp SettopBoxDemo_Utils.jar, DemoData.properties and DemoData_ProgramGuide.csv to /root/settopbox
6) update DemoData.properties file:  Switch paths to /root/settopbox/
     settopboxdemo.data.input.program_guide=/root/settopbox/DemoData_ProgramGuide.csv
     settopboxdemo.kafka.input_file=/root/settopbox/input_test.txt
     settopboxdemo.data.output.events=/root/settopbox/input_test.txt
7) Create settopbox index: curl "http://127.0.0.1:8983/solr/admin/cores?action=CREATE&name=settopbox&instanceDir=/opt/lucidworks-hdpsearch/solr/server/solr/settopbox&configSet=hdp"
8) Load NiFi template (NiFi_SetTopBox.xml)
9) In banana dashboard, load the "Set Top Box Events - ##########" file
10) In banana dashboard settings (gear box in upper right), make sure SOLR config has server = “/solr/“ and collection = “settopbox"
11) Start NiFi processors
12) Run
java -cp SettopBoxDemo_Utils.jar com.hortonworks.settopboxdemo.DataGenerator DemoData.properties
java -cp SettopBoxDemo_Utils.jar com.hortonworks.settopboxdemo.MessageSender DemoData.properties
