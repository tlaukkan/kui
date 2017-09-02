# White Ice Agent Installation

Copy the agent distribution tar file to target host and execute the following commands:

tar -xvf white-ice-agent-1.0-SNAPSHOT.tar
cd white-ice-agent-1.0-SNAPSHOT

Install service scripts and start agent.

sudo ./install.sh

Configure properties using agent-ext.properties and storage.truststore file.

sudo nano /opt/white-ice-agent/agent-ext.properties

Start service

sudo service wia start





